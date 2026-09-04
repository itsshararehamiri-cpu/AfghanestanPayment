package com.danesh.api


sealed interface PspKeyLoadStep {
    data class WriteMasterKey(
        val key: ByteArray,
        val index: Int? = null,
    ) : PspKeyLoadStep

    data class WriteMacKey(
        val key: ByteArray,
        val index: Int? = null,
    ) : PspKeyLoadStep

    data class WritePinKey(
        val key: ByteArray,
    ) : PspKeyLoadStep

    data class WriteDataKey(
        val key: ByteArray,
    ) : PspKeyLoadStep

    data class LoadTmkEncryptedMacKey(
        val encryptedKey: ByteArray,
        val index: Int? = null,
    ) : PspKeyLoadStep

    data class LoadTmkEncryptedPinKey(
        val encryptedKey: ByteArray,
    ) : PspKeyLoadStep

    data class LoadTmkEncryptedDataKey(
        val encryptedKey: ByteArray,
    ) : PspKeyLoadStep
}

data class PspLogonWorkingKeys(
    val encryptedMacKey: ByteArray,
    val encryptedPinKey: ByteArray,
    val encryptedDataKey: ByteArray)


interface PspDeviceWorkflow {
    fun initPreparationSteps(input: InitInput): List<PspKeyLoadStep> = emptyList()

    fun initCompletionSteps(
        request: InitRequest,
        terminalKey: ByteArray,
    ): List<PspKeyLoadStep> = emptyList()

    fun logonPreparationSteps(): List<PspKeyLoadStep> = emptyList()

    fun logonCompletionSteps(workingKeys: PspLogonWorkingKeys): List<PspKeyLoadStep> = emptyList()
}


interface PspDeviceOperations {
    suspend fun prepareInit(input: InitInput)

    suspend fun completeInit(request: InitRequest, terminalKey: ByteArray)

    suspend fun prepareLogon()

    suspend fun completeLogon(workingKeys: PspLogonWorkingKeys)
}

data class PspDeviceMetadata(
    val serial: String,
    val appVersion: String,
    val imei: String = "",
    val simSerial: String = "",
) {

    fun toField63(): String = "$serial@$appVersion@$imei@$simSerial"

    @Deprecated(
        message = "Use com.danesh.bp.field63.toBpField63() for BP requests",
        replaceWith = ReplaceWith("toBpField63()", "com.danesh.bp.field63.toBpField63"),
    )
    fun toInitField63(): String = toField63()
}

interface PspDeviceMetadataProvider {
    suspend fun metadata(): PspDeviceMetadata

    suspend fun deviceSerial(): String = metadata().serial
}
