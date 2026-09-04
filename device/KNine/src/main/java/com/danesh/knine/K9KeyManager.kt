package com.danesh.knine

import android.util.Log
import com.danesh.core.DeviceTrace
import com.danesh.core.MacKeyType
import com.pos.sdk.pinpad.KeyType
import com.pos.sdk.pinpad.PinpadDevice
import com.pos.sdk.pinpad.PinPadMacInfo
import com.pos.sdk.pinpad.PinPadMacInfo.MacType
import com.pos.util.HexUtils

private const val KEY_SDK = "K9.SDK"

/**
 * مدیریت TMK/MAK/PIK/DEK روی PED K9.
 *
 * - Key-wrap (TMK/MAK/PIK/DEK load) همیشه ECB موقت روی PED
 * - مسیر داده (DEK encrypt/decrypt) از [dataCbcModeProvider] پیروی می‌کند
 * - TMK plaintext فقط در RAM و پس از logon پاک می‌شود
 */
internal class K9KeyManager(
    private val pinpadProvider: () -> PinpadDevice?,
    private val dataCbcModeProvider: () -> Boolean,
    private val indexTmk: Int,
    private val indexMac: Int,
    private val indexPin: Int,
    private val indexData: Int,
) {
    private var activeTmkIndex: Int = 1
    private var workingTmkIndex: Int = 1
    private val masterKeys = mutableMapOf<Int, ByteArray>()

    /** MacType فعال برای BP — پس از diagnose با مرجع نرم‌افزار به‌روز می‌شود. */
    @Volatile
    private var resolvedMacType: MacType = BP_MAC_TYPE_DEFAULT
    /** MAK کاری plaintext (F62 logon) برای محاسبه MAC نرم‌افزاری — جدا از TMK cache. */
    @Volatile
    private var workingMakPlain: ByteArray? = null

    fun preparePinpad(device: PinpadDevice) {
        device.setCbcMode(dataCbcModeProvider())
    }

    private inline fun <T> withEcbForKeyLoading(device: PinpadDevice, block: () -> T): T {
        device.setCbcMode(false)
        return try {
            block()
        } finally {
            device.setCbcMode(dataCbcModeProvider())
        }
    }

    suspend fun writeMasterKey(masterKey: ByteArray, index: Int) {
        val device = requirePinpad("writeMasterKey")
        preparePinpad(device)
        val key = normalizeDesKey(masterKey)
        calcKcvHex(key)?.let { kcv ->
            DeviceTrace.step(KEY_SDK, "writeMasterKey index=$index plaintextKCV=$kcv")
        }
        withEcbForKeyLoading(device) {

            val ok = device.loadTmk(index, key)
            DeviceTrace.step(KEY_SDK, "writeMasterKey inject index=$index result=$ok")
            check(ok) { "PED loadTmk failed index=$index" }
            requirePedKcvMatches(
                device = device,
                keyType = KeyType.TDKEK,
                index = index,
                expectedKey = key,
                label = "TMK",
            )
        }
        masterKeys[index]?.fill(0)
        masterKeys[index] = key.copyOf()
        activeTmkIndex = index
        if (index == indexTmk) {
            workingTmkIndex = index
            DeviceTrace.step(
                KEY_SDK,
                "writeMasterKey workingTmkIndex=$workingTmkIndex SET — TMK پایانه برای unwrap کلیدهای logon",
            )
        }
    }

    /** plaintext MAK bootstrap — نه ciphertext هاست. */
    suspend fun writePlaintextMacKey(macKey: ByteArray, index: Int) {
        val device = requirePinpad("writePlaintextMacKey")
        preparePinpad(device)
        val key = normalizeDesKey(macKey)
        val tmkIndex = activeTmkIndex
        val encrypted = encryptWithMasterKey(key, tmkIndex)
        withEcbForKeyLoading(device) {
            val ok = device.loadTmkEncryptedMak(tmkIndex, index, encrypted)
            Log.d("TAG", "writePlaintextMacKeycalled->$ok")
            DeviceTrace.step(
                KEY_SDK,
                "writePlaintextMacKey inject makIndex=$index tmkIndex=$tmkIndex " +
                        "keyLen=${key.size} (24=3-key TDES) result=$ok",
            )
            check(ok) { "PED loadTmkEncryptedMak failed tmkIndex=$tmkIndex makIndex=$index" }
            requirePedKcvMatches(device, KeyType.MAK, index, key, "MAK")
            calcKcvHex(key)?.let { kcv ->
                DeviceTrace.step(KEY_SDK, "writePlaintextMacKey plaintextKCV=$kcv")
            }
            readPedKcv(device, KeyType.MAK, index)?.let { pedKcv ->
                DeviceTrace.step(KEY_SDK, "writePlaintextMacKey PED MAK KCV@$index=$pedKcv")
            }
        }
    }

    suspend fun writePlaintextDataKey(dataKey: ByteArray) {
        val device = requirePinpad("writePlaintextDataKey")
        preparePinpad(device)
        val tmkIndex = activeTmkIndex
        val encrypted = encryptWithMasterKey(normalizeDesKey(dataKey), tmkIndex)
        withEcbForKeyLoading(device) {
            val ok = device.loadTmkEncryptedDek(tmkIndex, indexData, encrypted)
            Log.d("TAG", "writePlaintexddtDataKey: dd$ok")
            DeviceTrace.debug(KEY_SDK, "writePlaintextDataKey tmkIndex=$tmkIndex dekIndex=$indexData result=$ok")
            check(ok) { "PED loadTmkEncryptedDek failed tmkIndex=$tmkIndex dekIndex=$indexData" }
            requirePedKcvMatches(device, KeyType.DEK, indexData, dataKey, "DEK")
        }
    }

    suspend fun writePlaintextPinKey(pinKey: ByteArray) {
        val device = requirePinpad("writePlaintextPinKey")
        preparePinpad(device)
        val tmkIndex = activeTmkIndex
        val encrypted = encryptWithMasterKey(normalizeDesKey(pinKey), tmkIndex)
        withEcbForKeyLoading(device) {
            val ok = device.loadTmkEncryptedPik(tmkIndex, indexPin, encrypted)
            DeviceTrace.debug(KEY_SDK, "writePlaintextPinKey tmkIndex=$tmkIndex pikIndex=$indexPin result=$ok")
            check(ok) { "PED loadTmkEncryptedPik failed tmkIndex=$tmkIndex pikIndex=$indexPin" }
            requirePedKcvMatches(device, KeyType.PIK, indexPin, pinKey, "PIK")
        }
    }

    suspend fun loadTmkEncryptedMacKey(encryptedKey: ByteArray, index: Int) {
        val device = requirePinpad("loadTmkEncryptedMacKey")
        preparePinpad(device)
        val tmkIndex = workingTmkIndex
        check(isValidDesBlockSize(encryptedKey)) {
            "encrypted MAK length must be multiple of 8, was ${encryptedKey.size}"
        }
        clearWorkingMacKeyCache()
        logEncryptedWorkingKeyPreInject(
            operation = "loadTmkEncryptedMacKey",
            label = "MAK",
            encryptedKey = encryptedKey,
            tmkIndex = tmkIndex,
            targetIndex = index,
            device = device,
        )
        withEcbForKeyLoading(device) {
            loadHostOrPlaintextWorkingKey(
                operation = "loadTmkEncryptedMacKey",
                label = "MAK",
                encryptedKey = encryptedKey,
                tmkIndex = tmkIndex,
                targetIndex = index,
                keyType = KeyType.MAK,
                loadFn = { tmk, target, cipher -> device.loadTmkEncryptedMak(tmk, target, cipher) },
            )
        }
    }

    suspend fun loadTmkEncryptedPinKey(encryptedKey: ByteArray) {
        val device = requirePinpad("loadTmkEncryptedPinKey")
        preparePinpad(device)
        val tmkIndex = workingTmkIndex
        check(isValidDesBlockSize(encryptedKey)) {
            "encrypted PIK length must be multiple of 8, was ${encryptedKey.size}"
        }
        logEncryptedWorkingKeyPreInject(
            operation = "loadTmkEncryptedPinKey",
            label = "PIK",
            encryptedKey = encryptedKey,
            tmkIndex = tmkIndex,
            targetIndex = indexPin,
            device = device,
        )
        withEcbForKeyLoading(device) {
            loadHostOrPlaintextWorkingKey(
                operation = "loadTmkEncryptedPinKey",
                label = "PIK",
                encryptedKey = encryptedKey,
                tmkIndex = tmkIndex,
                targetIndex = indexPin,
                keyType = KeyType.PIK,
                loadFn = { tmk, _, cipher -> device.loadTmkEncryptedPik(tmk, indexPin, cipher) },
            )
        }
    }

    suspend fun loadTmkEncryptedDataKey(encryptedKey: ByteArray) {
        val device = requirePinpad("loadTmkEncryptedDataKey")
        preparePinpad(device)
        val tmkIndex = workingTmkIndex
        check(isValidDesBlockSize(encryptedKey)) {
            "encrypted DEK length must be multiple of 8, was ${encryptedKey.size}"
        }
        logEncryptedWorkingKeyPreInject(
            operation = "loadTmkEncryptedDataKey",
            label = "DEK",
            encryptedKey = encryptedKey,
            tmkIndex = tmkIndex,
            targetIndex = indexData,
            device = device,
        )
        withEcbForKeyLoading(device) {
            loadHostOrPlaintextWorkingKey(
                operation = "loadTmkEncryptedDataKey",
                label = "DEK",
                encryptedKey = encryptedKey,
                tmkIndex = tmkIndex,
                targetIndex = indexData,
                keyType = KeyType.DEK,
                loadFn = { tmk, _, cipher -> device.loadTmkEncryptedDek(tmk, indexData, cipher) },
            )
        }
    }
    fun getCheckValue(index: Int, keyType: KeyType): ByteArray{
        try {
            Log.d("TAG", "getCheckValue: index=${index},keyType=$keyType")
            val device = pinpadProvider()
            return  device!!.getCheckValue(keyType,index)
        }
        catch (e: Exception){
            Log.d("TAG", "getCheckValue: cause${e.cause}")
            Log.d("TAG", "getCheckValue: message${e.message}")
            return ByteArray(0)
        }
    }
    suspend fun getMac(data: ByteArray, index: Int, keyType: MacKeyType): ByteArray {
        Log.d("TAG", "getMac: dddddd->${HexUtils.bytesToHexString(data)}")
        DeviceTrace.step(KEY_SDK, "getMac input length=${data.size} makIndex=$index keyType=$keyType")
        val device = pinpadProvider()
        if (device == null) {
            DeviceTrace.error(KEY_SDK, "getMac pinpadDevice unavailable")
            return ByteArray(0)
        }
        if (data.isEmpty()) {
            DeviceTrace.error(KEY_SDK, "getMac data is empty")
            return ByteArray(0)
        }
        // BP host / AnsiX919Mac: zero-pad → TYPE_X919_00 (نه TYPE_X919 که معمولاً pad 0x80 است).
        // اگر diagnose MacType بهتری پیدا کرد، از همان استفاده می‌شود (MacMode همان caller می‌ماند —
        // WORKING/MAK مالی همیشه WORK_KEY است).
        DeviceTrace.step(
            KEY_SDK,
            "getMac using macType=$resolvedMacType keyType=$keyType makIndex=$index",
        )
        return calcPedMac(device, data, index, keyType, resolvedMacType)
    }

    /**
     * پروب MacType × MacMode وقتی خروجی PED با مرجع نرم‌افزاری (هاست) فرق دارد.
     * اگر تطبیق پیدا شود، برای getMacهای بعدی همان ترکیب را نگه می‌دارد.
     *
     * توجه: برای Logon BP، ارسال MAC نرم‌افزار با RC=00 کافی است؛ این پروب فقط علت
     * اختلاف PED را مشخص می‌کند و مانع موفقیت پروتکل نیست.
     */
    suspend fun diagnoseMacMismatch(
        data: ByteArray,
        index: Int,
        keyType: MacKeyType,
        referenceMac: ByteArray,
    ) {
        val device = pinpadProvider()
        if (device == null) {
            DeviceTrace.error(KEY_SDK, "diagnoseMacMismatch pinpad unavailable")
            return
        }
        val padHint = when (data.size % 8) {
            0 -> "aligned (no pad bytes)"
            else -> "needs ${8 - (data.size % 8)} pad byte(s) — padding mode matters"
        }
        DeviceTrace.step(
            KEY_SDK,
            "diagnoseMacMismatch | makIndex=$index callerKeyType=$keyType inputLen=${data.size} " +
                    "padHint=$padHint resolvedMacType=$resolvedMacType " +
                    "ref=${HexUtils.bytesToHexString(referenceMac)} " +
                    "tmkActive=$activeTmkIndex tmkWorking=$workingTmkIndex " +
                    "(برای Logon: tmkWorking باید INDEX_TMK پایانه بماند)",
        )
        readPedKcv(device, KeyType.MAK, index)?.let { makKcv ->
            DeviceTrace.step(KEY_SDK, "diagnoseMacMismatch | PED MAK KCV@$index=$makKcv")
        } ?: DeviceTrace.warn(KEY_SDK, "diagnoseMacMismatch | PED MAK KCV@$index unreadable")
        readPedKcv(device, KeyType.TDKEK, index)?.let { tmkKcv ->
            DeviceTrace.step(
                KEY_SDK,
                "diagnoseMacMismatch | PED TMK(TDKEK) KCV@$index=$tmkKcv " +
                        "(bootstrap: معمولاً همان Initial Key)",
            )
        }
        val typesToProbe = listOf(
            MacType.TYPE_X919_00,
            MacType.TYPE_X919,
            MacType.TYPE_X919_MP,
            MacType.TYPE_X9_9,
            MacType.TYPE_CUP_ECB,
        )
        val modesToProbe = listOf(MacKeyType.WORK, MacKeyType.MASTER)
        var matchedType: MacType? = null
        var matchedKeyType: MacKeyType? = null
        for (mode in modesToProbe) {
            for (type in typesToProbe) {
                val mac = runCatching {
                    calcPedMac(device, data, index, mode, type)
                }.getOrElse { error ->
                    DeviceTrace.warn(
                        KEY_SDK,
                        "diagnoseMacMismatch | type=$type mode=$mode FAILED: ${error.message}",
                    )
                    ByteArray(0)
                }
                if (mac.isEmpty()) {
                    DeviceTrace.step(KEY_SDK, "diagnoseMacMismatch | type=$type mode=$mode → empty")
                    continue
                }
                val ped8 = mac.copyOf(minOf(8, mac.size))
                val ref8 = referenceMac.copyOf(minOf(8, referenceMac.size))
                val ok = ped8.contentEquals(ref8)
                DeviceTrace.step(
                    KEY_SDK,
                    "diagnoseMacMismatch | type=$type mode=$mode " +
                            "ped=${HexUtils.bytesToHexString(ped8)} matchRef=$ok",
                )
                if (ok && matchedType == null) {
                    matchedType = type
                    matchedKeyType = mode
                }
            }
        }
        if (matchedType != null && matchedKeyType != null) {
            resolvedMacType = matchedType
            DeviceTrace.step(
                KEY_SDK,
                "diagnoseMacMismatch | MATCH → resolvedMacType=$matchedType " +
                        "bestMode=$matchedKeyType (MacType برای getMac بعدی ذخیره شد؛ " +
                        "MacMode مالی همچنان WORK_KEY می‌ماند)",
            )
            if (matchedKeyType != MacKeyType.WORK) {
                DeviceTrace.warn(
                    KEY_SDK,
                    "diagnoseMacMismatch | بهترین تطبیق با mode=$matchedKeyType بود — " +
                            "برای bootstrap Logon بررسی شود؛ تراکنش مالی از WORK_KEY استفاده می‌کند",
                )
            }
        } else {
            DeviceTrace.error(
                KEY_SDK,
                "diagnoseMacMismatch | هیچ ترکیب MacType/MacMode با مرجع یکی نشد — " +
                        "احتمال: کلید PED ≠ کلید نرم‌افزار (variant / parity / slot). " +
                        "برای Init/Logon ارسال SOFTWARE_X919 عمدی است و Host آن را با RC=00 می‌پذیرد.",
            )
        }
    }

    private fun calcPedMac(
        device: PinpadDevice,
        data: ByteArray,
        index: Int,
        keyType: MacKeyType,
        macType: MacType,
    ): ByteArray {
        Log.d(
            "TAG",
            "calcPedMac called with = , data = ${HexUtils.bytesToHexString(data)}, index = $index, keyType = $keyType, macType = $macType"
        )
        // MAC همیشه ECB — CBC مسیر DEK نباید روی getMac اثر بگذارد.
        return withEcbForKeyLoading(device) {
            val macMode = when (keyType) {
                MacKeyType.MASTER -> PinPadMacInfo.MacMode.PROCESS_KEY
                MacKeyType.WORK -> PinPadMacInfo.MacMode.WORK_KEY
            }
            DeviceTrace.step(
                KEY_SDK,
                "getMac calc PED macType=$macType makIndex=$index macMode=$macMode " +
                        "inputLen=${data.size} cbc=false",
            )
            val macInfo = PinPadMacInfo.builder(index, data)
                .setMacMode(macMode)
                .setMacType(macType)
                .build()
            val mac = device.getMac(macInfo)
            if (mac != null && mac.isNotEmpty()) {
                DeviceTrace.step(KEY_SDK, "getMac ok macType=$macType macLen=${mac.size}")
                mac
            } else {
                DeviceTrace.error(
                    KEY_SDK,
                    "getMac device returned null/empty makIndex=$index macType=$macType",
                )
                ByteArray(0)
            }
        }
    }

    fun clearMasterKeyCache() {
        masterKeys.values.forEach { cached -> cached.fill(0) }
        masterKeys.clear()
        DeviceTrace.step(KEY_SDK, "masterKeys cache cleared (TMK plaintext wiped from RAM)")
    }

    fun clearWorkingMacKeyCache() {
        workingMakPlain?.fill(0)
        workingMakPlain = null
        DeviceTrace.step(KEY_SDK, "working MAK plaintext cache cleared")
    }

    /** کپی MAK کاری برای AnsiX919 نرم‌افزاری — null اگر logon نشده. */
    fun peekWorkingMacKey(): ByteArray? = workingMakPlain?.copyOf()

    fun restoreMasterKeyCache(masterKey: ByteArray, index: Int) {
        val key = normalizeDesKey(masterKey)
        masterKeys[index] = key.copyOf()
        activeTmkIndex = index
        if (index == indexTmk) {
            workingTmkIndex = index
        }
        calcKcvHex(key)?.let { kcv ->
            DeviceTrace.step(KEY_SDK, "restoreMasterKeyCache index=$index plaintextKCV=$kcv")
        }
    }

    fun hasMasterKeyCache(index: Int): Boolean = masterKeys.containsKey(index)

    private fun encryptWithMasterKey(plainKey: ByteArray, tmkIndex: Int = activeTmkIndex): ByteArray {
        val tmk = masterKeys[tmkIndex]
            ?: error("TMK index=$tmkIndex not loaded — call writeMasterKey first")
        return try {
            DES3Utils.encrypt3DES(plainKey, tmk, /* isCbc = */ false)
        } catch (e: Exception) {
            DeviceTrace.error(KEY_SDK, "encryptWithMasterKey failed", throwable = e)
            throw IllegalStateException("encrypt3DES failed: ${e.message}", e)
        }
    }

    private fun decryptWorkingKey(encrypted: ByteArray, tmkIndex: Int): ByteArray {
        val tmk = masterKeys[tmkIndex]
            ?: error("TMK cache index=$tmkIndex missing — init/RKI کامل نشده یا TMK overwrite شده")
        return DES3Utils.decrypt3DES(encrypted, tmk, /* isCbc = */ false)
    }

    private fun injectPlaintextWorkingKey(
        label: String,
        plaintext: ByteArray,
        tmkIndex: Int,
        targetIndex: Int,
        loadFn: (tmkIndex: Int, targetIndex: Int, pedCipher: ByteArray) -> Boolean,
    ): Boolean {
        val normalized = normalizeDesKey(plaintext)
        val pedCipher = encryptWithMasterKey(normalized, tmkIndex)
        calcKcvHex(normalized)?.let { kcv ->
            DeviceTrace.step(
                KEY_SDK,
                "injectPlaintextWorkingKey $label tmkIndex=$tmkIndex targetIndex=$targetIndex plaintextKCV=$kcv",
            )
        }
        DeviceTrace.step(KEY_SDK, "injectPlaintextWorkingKey $label pedCipherLen=${pedCipher.size}")
        return loadFn(tmkIndex, targetIndex, pedCipher)
    }

    private fun loadHostOrPlaintextWorkingKey(
        operation: String,
        label: String,
        encryptedKey: ByteArray,
        tmkIndex: Int,
        targetIndex: Int,
        keyType: KeyType,
        loadFn: (tmkIndex: Int, targetIndex: Int, cipher: ByteArray) -> Boolean,
    ) {
        DeviceTrace.step(
            KEY_SDK,
            "$operation SDK call direct host ciphertext len=${encryptedKey.size} " +
                    "tmkIndex=$tmkIndex (باید Terminal TMK از Init باشد، نه bootstrap)",
        )
        val expectedPlain = runCatching { decryptWorkingKey(encryptedKey, tmkIndex) }.getOrNull()
        try {
            var ok = loadFn(tmkIndex, targetIndex, encryptedKey)
            DeviceTrace.step(KEY_SDK, "$operation direct host ciphertext result=$ok")
            if (ok && expectedPlain != null) {
                ok = pedKcvMatches(
                    device = requirePinpad(operation),
                    keyType = keyType,
                    index = targetIndex,
                    expectedKey = expectedPlain,
                )
                if (!ok) {
                    DeviceTrace.warn(
                        KEY_SDK,
                        "$operation direct load returned true but $label KCV mismatched retrying SDK-compatible wrap",
                    )
                }
            }
            if (!ok) {
                // برای BP رایج است: PED ciphertext خام هاست را رد می‌کند؛
                // decrypt نرم‌افزاری زیر TMK + wrap مجدد SDK → inject موفق.
                DeviceTrace.step(
                    KEY_SDK,
                    "$operation fallback — decrypt F62 زیر TMK + injectPlaintextWorkingKey " +
                            "(مستقیم reject شده؛ این مسیر برای BP طبیعی است اگر plaintextKCV درست باشد)",
                )
                val plain = expectedPlain
                    ?: error("$operation cannot verify or re-wrap without the current Init Terminal TMK")
                ok = injectPlaintextWorkingKey(label, plain, tmkIndex, targetIndex, loadFn)
                DeviceTrace.step(
                    KEY_SDK,
                    "$operation fallback plaintext inject result=$ok label=$label targetIndex=$targetIndex",
                )
                check(ok) { "PED $operation failed tmkIndex=$tmkIndex targetIndex=$targetIndex" }
                requirePedKcvMatches(
                    device = requirePinpad(operation),
                    keyType = keyType,
                    index = targetIndex,
                    expectedKey = plain,
                    label = label,
                )
            }
            check(ok) { "PED $operation failed tmkIndex=$tmkIndex targetIndex=$targetIndex" }
        } finally {
            expectedPlain?.fill(0)
        }
    }

    private fun logEncryptedWorkingKeyPreInject(
        operation: String,
        label: String,
        encryptedKey: ByteArray,
        tmkIndex: Int,
        targetIndex: Int,
        device: PinpadDevice,
    ) {
        DeviceTrace.step(
            KEY_SDK,
            "$operation | $label encryptedLen=${encryptedKey.size} tmkIndex=$tmkIndex targetIndex=$targetIndex",
        )
        logPedTmkState(operation, device)
        logWorkingKeyDecrypt(label, encryptedKey, tmkIndex, targetIndex)
    }

    private fun logPedTmkState(operation: String, device: PinpadDevice? = null) {
        val tmkIndex = workingTmkIndex
        DeviceTrace.step(
            KEY_SDK,
            "$operation | workingTmkIndex=$tmkIndex activeTmkIndex=$activeTmkIndex " +
                    "pikIndex=$indexPin makIndex=$indexMac dekIndex=$indexData",
        )
        val cached = masterKeys[tmkIndex]
        if (cached != null) {
            DeviceTrace.step(KEY_SDK, "$operation | TMK cache index=$tmkIndex bytes=${cached.size}")
            calcKcvHex(cached)?.let { kcv ->
                DeviceTrace.step(KEY_SDK, "$operation | TMK cache KCV=$kcv")
            }
        } else {
            DeviceTrace.error(
                KEY_SDK,
                "$operation | TMK cache index=$tmkIndex MISSING — " +
                        "loadTmkEncrypted* احتمالاً fail (init/RKI کامل نشده یا TMK overwrite شده)",
            )
        }
        device?.let { ped ->
            readPedKcv(ped, KeyType.TDKEK, tmkIndex)?.let { pedKcv ->
                DeviceTrace.step(KEY_SDK, "$operation | PED TMK(TDKEK) KCV@$tmkIndex=$pedKcv")
            } ?: DeviceTrace.warn(
                KEY_SDK,
                "$operation | PED TMK(TDKEK) KCV@$tmkIndex unreadable — slot خالی یا SDK reject",
            )
        }
    }

    private fun logWorkingKeyDecrypt(
        label: String,
        encrypted: ByteArray,
        tmkIndex: Int,
        targetIndex: Int,
    ) {
        runCatching {
            val plain = decryptWorkingKey(encrypted, tmkIndex)
            val plainKcv = calcKcvHex(plain)
            val detail = buildString {
                append("logon $label software-decrypt OK tmkIndex=$tmkIndex targetIndex=$targetIndex ")
                append("plaintextLen=${plain.size} plaintextKCV=${plainKcv.orEmpty()}")
            }
            DeviceTrace.step(KEY_SDK, detail)
            if (plain.size !in setOf(8, 16, 24)) {
                DeviceTrace.warn(
                    KEY_SDK,
                    "logon $label plaintext length=${plain.size} — SDK معمولاً 16 یا 24 انتظار دارد",
                )
            }
            if (label == "MAK") {
                cacheWorkingMak(plain)
            }
        }.onFailure { error ->
            DeviceTrace.error(
                KEY_SDK,
                "logon $label software-decrypt FAILED tmkIndex=$tmkIndex — " +
                        "PIK/MAK/DEK با TMK پایانه رمز نشده یا TMK اشتباه: ${error.message}",
            )
        }
    }

    private fun cacheWorkingMak(plain: ByteArray) {
        val normalized = runCatching { normalizeDesKey(plain) }.getOrElse {
            // اگر طول غیرعادی بود همان را نگه دار تا AnsiX919Mac require بدهد
            plain.copyOf()
        }
        workingMakPlain?.fill(0)
        workingMakPlain = normalized.copyOf()
        calcKcvHex(normalized)?.let { kcv ->
            DeviceTrace.step(
                KEY_SDK,
                "working MAK cached for software MAC len=${normalized.size} KCV=$kcv",
            )
        }
    }

    private fun requirePinpad(operation: String): PinpadDevice =
        pinpadProvider() ?: error("PED pinpad unavailable for $operation")

    private fun calcKcvHex(keyBytes: ByteArray): String? = runCatching {
        DES3Utils.getCheckValue(HexUtils.bytesToHexString(keyBytes))
    }.getOrNull()

    private fun readPedKcv(device: PinpadDevice, keyType: KeyType, index: Int): String? =
        runCatching {
            HexUtils.bytesToHexString(device.getCheckValue(keyType, index))
        }.getOrNull()

    private fun pedKcvMatches(
        device: PinpadDevice,
        keyType: KeyType,
        index: Int,
        expectedKey: ByteArray,
    ): Boolean {
        val expected = calcKcvHex(normalizeDesKey(expectedKey)) ?: return false
        val actual = readPedKcv(device, keyType, index) ?: return false
        return actual.startsWith(expected, ignoreCase = true)
    }

    private fun requirePedKcvMatches(
        device: PinpadDevice,
        keyType: KeyType,
        index: Int,
        expectedKey: ByteArray,
        label: String,
    ) {
        val expected = calcKcvHex(normalizeDesKey(expectedKey))
            ?: error("Unable to calculate expected $label KCV")
        val actual = readPedKcv(device, keyType, index)
            ?: error("Unable to read PED $label KCV at index=$index")
        check(actual.startsWith(expected, ignoreCase = true)) {
            "PED $label KCV mismatch at index=$index expected=$expected actual=${actual.take(expected.length)}"
        }
        DeviceTrace.step(KEY_SDK, "$label KCV verified at index=$index value=$expected")
    }

    private companion object {
        /**
         * به‌پرداخت: ANSI X9.19 با zero-pad (مثل AnsiX919Mac و هاست).
         * TYPE_X919 در SDK Centerm معمولاً pad 0x80 می‌زند و با هاست BP یکی نیست.
         */
        val BP_MAC_TYPE_DEFAULT: MacType = MacType.TYPE_X919_00
    }

    private fun isValidDesBlockSize(data: ByteArray): Boolean =
        data.isNotEmpty() && data.size % 8 == 0

    private fun normalizeDesKey(key: ByteArray): ByteArray = when (key.size) {
        24 -> key.copyOf()
        16 -> key + key.copyOfRange(0, 8)
        8 -> key + key + key
        else -> error("Unsupported DES key length=${key.size} (expected 8/16/24)")
    }
}
