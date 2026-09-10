package com.danesh.hp

import com.danesh.api.BalanceInput
import com.danesh.api.BalanceOutput
import com.danesh.api.BalanceUserInput
import com.danesh.api.BillInput
import com.danesh.api.BillInquiryInput
import com.danesh.api.BillInquiryOutput
import com.danesh.api.BillInquiryRequest
import com.danesh.api.BillOutput
import com.danesh.api.BillUserInput
import com.danesh.api.CardToCardInput
import com.danesh.api.CardToCardOutput
import com.danesh.api.CardToCardUserInput
import com.danesh.api.CardToWalletInput
import com.danesh.api.CardToWalletOutput
import com.danesh.api.CardToWalletUserInput
import com.danesh.api.WalletToWalletInput
import com.danesh.api.WalletToWalletOutput
import com.danesh.api.WalletToWalletUserInput
import com.danesh.api.CashDepositInput
import com.danesh.api.CashDepositOutput
import com.danesh.api.CashDepositUserInput
import com.danesh.api.CashOutInput
import com.danesh.api.CashOutOutput
import com.danesh.api.CashOutUserInput
import com.danesh.api.InitOutput
import com.danesh.api.LogonOutput
import com.danesh.api.LogonRequest
import com.danesh.api.InitInput
import com.danesh.api.InitRequest
import com.danesh.api.NameInquiryInput
import com.danesh.api.NameInquiryOutput
import com.danesh.api.NameInquiryRequest
import com.danesh.api.PspDeviceOperations
import com.danesh.api.PspGateway
import com.danesh.api.PurchaseInput
import com.danesh.api.PurchaseOutput
import com.danesh.api.PurchaseUserInput
import com.danesh.api.SignOnInput
import com.danesh.api.SignOnOutput
import com.danesh.api.SignOnUserInput
import com.danesh.api.SupportInput
import com.danesh.api.SupportOutput
import com.danesh.api.TerminalConfigInput
import com.danesh.api.TerminalConfigOutput
import com.danesh.api.TerminalConfigUserInput
import com.danesh.api.TopUpInput
import com.danesh.api.TopUpOutput
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.api.VoucherInput
import com.danesh.api.VoucherOutput
import com.danesh.common.menu.MenuFeaturePreferences
import com.danesh.common.menu.MenuFlavorFeatures
import com.danesh.engine.TransactionExecutor
import com.danesh.hp.balance.BalanceHandler
import com.danesh.hp.balance.HpBalanceRequest
import com.danesh.hp.bill.BillInquiryHandler
import com.danesh.hp.bill.BillPaymentHandler
import com.danesh.hp.card_to_card.CardToCardHandler
import com.danesh.hp.card_to_card.HpCardToCardRequest
import com.danesh.hp.card_to_wallet.CardToWalletHandler
import com.danesh.hp.card_to_wallet.HpCardToWalletRequest
import com.danesh.hp.wallet_to_wallet.HpWalletToWalletRequest
import com.danesh.hp.wallet_to_wallet.WalletToWalletHandler
import com.danesh.hp.cash_deposit.CashDepositHandler
import com.danesh.hp.cash_deposit.HpCashDepositRequest
import com.danesh.hp.cash_out.CashOutHandler
import com.danesh.hp.cash_out.HpCashOutRequest
import com.danesh.hp.config.HpTerminalConfigRequest
import com.danesh.hp.config.TerminalConfigHandler
import com.danesh.hp.init.InitHandler
import com.danesh.hp.logon.LogonHandler
import com.danesh.hp.name_inquiry.NameInquiryHandler
import com.danesh.hp.purchase.HpPurchaseRequest
import com.danesh.hp.purchase.PurchaseHandler
import com.danesh.hp.signon.HpSignOnRequest
import com.danesh.hp.signon.SignOnHandler
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpGateway @Inject constructor(
    private val executor: TransactionExecutor<IsoMessage>,
    private val balanceHandler: BalanceHandler,
    private val purchaseHandler: PurchaseHandler,
    private val billInquiryHandler: BillInquiryHandler,
    private val billPaymentHandler: BillPaymentHandler,
    private val cashDepositHandler: CashDepositHandler,
    private val cashOutHandler: CashOutHandler,
    private val cardToCardHandler: CardToCardHandler,
    private val cardToWalletHandler: CardToWalletHandler,
    private val walletToWalletHandler: WalletToWalletHandler,
    private val nameInquiryHandler: NameInquiryHandler,
    private val logonHandler: LogonHandler,
    private val initHandler: InitHandler,
    private val signOnHandler: SignOnHandler,
    private val terminalConfigHandler: TerminalConfigHandler,
    private val deviceOperations: PspDeviceOperations,
    private val menuFlavorFeatures: MenuFlavorFeatures,
    private val menuFeaturePreferences: MenuFeaturePreferences,
) : PspGateway {

    /**
     * دروازهٔ نهاییِ ارسال: حتی اگر مسیری غیر از منوی اصلی (deep link، وضعیت ناوبری بازیابی‌شده،
     * صفحهٔ جدید) به این متدها برسد، یک تراکنش غیرفعال‌شده توسط پیکربندی PSP یا تنظیمات کاربر
     * هرگز به سوییچ ارسال نمی‌شود. [featureName] باید دقیقاً با نام [com.danesh.menu.model.MenuItemType]
     * یکی باشد که در `BuildConfig.ENABLED_FEATURES` استفاده می‌شود.
     */
    private fun isFeatureDispatchable(featureName: String): Boolean {
        val flavorAllowed = menuFlavorFeatures.enabledFeatures().let { allowed ->
            allowed.isEmpty() || featureName in allowed
        }
        return flavorAllowed && menuFeaturePreferences.isFeatureEnabled(featureName)
    }

    private fun disabledFeatureResult(type: TransactionType): TransactionResultDetail =
        TransactionResultDetail(
            isSuccess = false,
            transactionType = type,
            responseCode = FEATURE_DISABLED_CODE,
            responseMessage = FEATURE_DISABLED_MESSAGE,
        )

    override suspend fun bill(input: BillInput): BillOutput {
        if (!isFeatureDispatchable(FEATURE_BILL)) return disabledFeatureResult(TransactionType.BILL)
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toPaymentUserInput(),
                handler = billPaymentHandler,
            ).detail
        }
    }

    override suspend fun billInquiry(input: BillInquiryInput): BillInquiryOutput {
        if (!isFeatureDispatchable(FEATURE_BILL)) return disabledFeatureResult(TransactionType.BILL)
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = BillInquiryRequest(
                    billId = input.billId,
                    payId = input.payId,
                    track2 = input.track2,
                    pan = input.pan,
                ),
                handler = billInquiryHandler,
            ).inquiry!!
        }
    }

    override suspend fun balance(input: BalanceInput): BalanceOutput {
        if (!isFeatureDispatchable(FEATURE_BALANCE)) return disabledFeatureResult(TransactionType.BALANCE)
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = balanceHandler,
            ).detail
        }
    }

    override suspend fun purchase(input: PurchaseInput): PurchaseOutput {
        if (!isFeatureDispatchable(FEATURE_PURCHASE)) return disabledFeatureResult(TransactionType.PURCHASE)
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = purchaseHandler,
            ).detail
        }
    }

    override suspend fun voucher(input: VoucherInput): VoucherOutput {
        if (!isFeatureDispatchable(FEATURE_VOUCHER)) return disabledFeatureResult(TransactionType.VOUCHER)
        return purchase(
            PurchaseInput(
                track2 = input.track2,
                pinBlock = input.pinBlock,
                amount = input.amount,
                pan = input.pan,
            ),
        )
    }

    override suspend fun topUp(input: TopUpInput): TopUpOutput {
        TODO("Not yet implemented")
    }
    override suspend fun terminalConfig(input: TerminalConfigInput): TerminalConfigOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = terminalConfigHandler,
            ).detail
        }
    }
    override suspend fun support(input: SupportInput): SupportOutput {
        return TransactionResultDetail(
            isSuccess = false,
            responseCode = "40",
            responseMessage = "Support transaction is not available for this PSP",
            transactionType = TransactionType.SUPPORT,
        )
    }

    override suspend fun logon(masterKey: String): LogonOutput {
        deviceOperations.prepareLogon()
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = LogonRequest(masterKey),
                handler = logonHandler,
            ).detail
        }
    }

    override suspend fun cashDeposit(input: CashDepositInput): CashDepositOutput {
        if (!isFeatureDispatchable(FEATURE_CASH_DEPOSIT)) {
            return disabledFeatureResult(TransactionType.CASH_DEPOSIT)
        }
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = cashDepositHandler,
            ).detail
        }
    }

    override suspend fun cashOut(input: CashOutInput): CashOutOutput {
        if (!isFeatureDispatchable(FEATURE_CASH_OUT)) {
            return disabledFeatureResult(TransactionType.CASH_OUT)
        }
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = cashOutHandler,
            ).detail
        }
    }

    override suspend fun cardToCard(input: CardToCardInput): CardToCardOutput {
        if (!isFeatureDispatchable(FEATURE_TRANSFER)) {
            return disabledFeatureResult(TransactionType.CARD_TO_CARD)
        }
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = cardToCardHandler,
            ).detail
        }
    }

    override suspend fun cardToWallet(input: CardToWalletInput): CardToWalletOutput {
        if (!isFeatureDispatchable(FEATURE_TRANSFER)) {
            return disabledFeatureResult(TransactionType.CARD_TO_WALLET)
        }
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = cardToWalletHandler,
            ).detail
        }
    }

    override suspend fun walletToWallet(input: WalletToWalletInput): WalletToWalletOutput {
        if (!isFeatureDispatchable(FEATURE_WALLET_TO_WALLET)) {
            return disabledFeatureResult(TransactionType.WALLET_TO_WALLET)
        }
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = walletToWalletHandler,
            ).detail
        }
    }

    override suspend fun nameInquiry(input: NameInquiryInput): NameInquiryOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = NameInquiryRequest(
                    forWallet = input.forWallet,
                    forWalletToWallet = input.forWalletToWallet,
                    destination = input.destination,
                    pan = input.pan,
                    sourceWallet = input.sourceWallet,
                    track2 = input.track2,
                ),
                handler = nameInquiryHandler,
            ).inquiry
        }
    }
    override suspend fun init(input: InitInput): InitOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = InitRequest(
                    firstBallotTicket = input.firstBallotTicket,
                    secondBallotTicket = input.secondBallotTicket,
                ),
                handler = initHandler,
            ).detail
        }
    }

    private fun BalanceInput.toUserInput(): HpBalanceRequest =
        BalanceUserInput(pinBlock = pinBlock, track2 = track2, pan = pan)

    private fun PurchaseInput.toUserInput(): HpPurchaseRequest =
        PurchaseUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount.toString(),
            pan = pan,
        )

    private fun TerminalConfigInput.toUserInput(): HpTerminalConfigRequest =
        TerminalConfigUserInput(
            pinBlock = "",
            track2 = "",
            pan = "",
        )
    override suspend fun signOn(input: SignOnInput): SignOnOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = signOnHandler,
            ).detail
        }
    }

    private fun SignOnInput.toUserInput(): HpSignOnRequest =
        SignOnUserInput(
            pinBlock = "",
            track2 = "",
            pan = "",
        )

    private fun CashDepositInput.toUserInput(): HpCashDepositRequest =
        CashDepositUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount.toString(),
            destinationAccount = destinationAccount,
            pan = "",
        )

    private fun CashOutInput.toUserInput(): HpCashOutRequest =
        CashOutUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount.toString(),
            destinationAccount = destinationAccount,
            pan = "",
        )

    private fun CardToCardInput.toUserInput(): HpCardToCardRequest =
        CardToCardUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            pan = pan,
            amount = amount.toString(),
            destinationPan = destinationPan,
            rrn = rrn,
            holderName = holderName,
        )

    private fun CardToWalletInput.toUserInput(): HpCardToWalletRequest =
        CardToWalletUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            pan = pan,
            amount = amount.toString(),
            walletCode = walletCode,
            rrn = rrn,
            holderName = holderName,
        )

    private fun WalletToWalletInput.toUserInput(): HpWalletToWalletRequest =
        WalletToWalletUserInput(
            pinBlock = pinBlock,
            pan = sourceWallet,
            amount = amount.toString(),
            destinationWallet = destinationWallet,
            rrn = rrn,
            holderName = holderName,
        )

    private fun BillInput.toPaymentUserInput(): BillUserInput =
        BillUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            pan = pan,
            billId = billId,
            payId = payId,
            amount = amount,
            requestId = requestId,
        )

    companion object {
        // نام‌های زیر باید دقیقاً با نام enum های com.danesh.menu.model.MenuItemType و
        // توکن‌های BuildConfig.ENABLED_FEATURES یکی باشند.
        private const val FEATURE_PURCHASE = "PURCHASE"
        private const val FEATURE_BALANCE = "BALANCE"
        private const val FEATURE_BILL = "BILL"
        private const val FEATURE_CASH_DEPOSIT = "CASH_DEPOSIT"
        private const val FEATURE_CASH_OUT = "CASH_OUT"
        private const val FEATURE_TRANSFER = "TRANSFER"
        private const val FEATURE_WALLET_TO_WALLET = "WALLET_TO_WALLET"
        private const val FEATURE_VOUCHER = "VOUCHER"

        /** کدهای محلی -1..-6 توسط TransactionTransportCodes استفاده شده‌اند؛ این کد صرفاً داخلی HamrahPay است. */
        private const val FEATURE_DISABLED_CODE = "-8"
        private const val FEATURE_DISABLED_MESSAGE = "این نوع تراکنش برای این پایانه غیرفعال است"
    }
}
