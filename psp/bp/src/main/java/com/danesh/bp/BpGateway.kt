package com.danesh.bp

import android.util.Log
import com.danesh.api.BalanceInput
import com.danesh.api.BalanceOutput
import com.danesh.api.BalanceUserInput
import com.danesh.api.BillInput
import com.danesh.api.BillInquiryInput
import com.danesh.api.BillInquiryOutput
import com.danesh.api.BillOutput
import com.danesh.api.BillUserInput
import com.danesh.api.CardToCardInput
import com.danesh.api.CardToCardOutput
import com.danesh.api.CardToWalletInput
import com.danesh.api.CardToWalletOutput
import com.danesh.api.WalletToWalletInput
import com.danesh.api.WalletToWalletOutput
import com.danesh.api.CashDepositInput
import com.danesh.api.NameInquiryInput
import com.danesh.api.NameInquiryOutput
import com.danesh.api.CashDepositOutput
import com.danesh.api.CashDepositUserInput
import com.danesh.api.CashOutInput
import com.danesh.api.CashOutOutput
import com.danesh.api.CashOutUserInput
import com.danesh.api.InitOutput
import com.danesh.api.InitInput
import com.danesh.api.InitRequest
import com.danesh.api.LogonOutput
import com.danesh.api.LogonRequest
import com.danesh.api.PspDeviceOperations
import com.danesh.api.PspGateway
import com.danesh.api.PurchaseInput
import com.danesh.api.PurchaseOutput
import com.danesh.api.PurchaseUserInput
import com.danesh.api.SignOnInput
import com.danesh.api.SignOnOutput
import com.danesh.api.SupportInput
import com.danesh.api.SupportOutput
import com.danesh.api.SupportUserInput
import com.danesh.api.TerminalConfigInput
import com.danesh.api.TerminalConfigOutput
import com.danesh.api.TopUpInput
import com.danesh.api.TopUpOutput
import com.danesh.api.TopUpUserInput
import com.danesh.api.TransactionType
import com.danesh.api.VoucherInput
import com.danesh.api.VoucherOutput
import com.danesh.api.VoucherUserInput
import com.danesh.engine.TransactionExecutor
import com.danesh.bp.balance.BalanceHandler
import com.danesh.bp.balance.BpBalanceRequest
import com.danesh.bp.bill.BillHandler
import com.danesh.bp.bill.BpBillRequest
import com.danesh.bp.cash_deposit.CashDepositHandler
import com.danesh.bp.cash_deposit.BpCashDepositRequest
import com.danesh.bp.cash_out.CashOutHandler
import com.danesh.bp.cash_out.BpCashOutRequest
import com.danesh.bp.init.InitHandler
import com.danesh.bp.init.BpInitTrace
import com.danesh.bp.logon.BpLogonTrace
import com.danesh.bp.logon.LogonHandler
import com.danesh.bp.purchase.BpPurchaseRequest
import com.danesh.bp.purchase.PurchaseHandler
import com.danesh.bp.support.BpSupportRequest
import com.danesh.bp.support.SupportHandler
import com.danesh.bp.voucher.BpTopUpRequest
import com.danesh.bp.voucher.BpVoucherRequest
import com.danesh.bp.voucher.TopUpHandler
import com.danesh.bp.voucher.VoucherHandler
import com.danesh.core.Device
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpGateway @Inject constructor(
    private val executor: TransactionExecutor<IsoMessage>,
    private val balanceHandler: BalanceHandler,
    private val purchaseHandler: PurchaseHandler,
    private val voucherHandler: VoucherHandler,
    private val topUpHandler: TopUpHandler,

    private val supportHandler: SupportHandler,
    private val cashDepositHandler: CashDepositHandler,
    private val cashOutHandler: CashOutHandler,
    private val logonHandler: LogonHandler,
    private val initHandler: InitHandler,
    private val deviceOperations: PspDeviceOperations,
    private val billHandler: BillHandler,
    private val device: Device
) : PspGateway {

    override suspend fun balance(input: BalanceInput): BalanceOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = balanceHandler,
            ).detail
        }
    }

    override suspend fun bill(input: BillInput): BillOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = billHandler,
            ).detail
        }
    }

    override suspend fun billInquiry(input: BillInquiryInput): BillInquiryOutput {
        return BillInquiryOutput(
            isSuccess = false,
            responseCode = "40",
            responseMessage = "Bill inquiry is not available for this PSP",
            billId = input.billId,
        )
    }

    override suspend fun purchase(input: PurchaseInput): PurchaseOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = purchaseHandler,
            ).detail
        }
    }

    override suspend fun voucher(input: VoucherInput): VoucherOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = voucherHandler,
            ).detail
        }
    }

    override suspend fun topUp(input: TopUpInput): TopUpOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = topUpHandler,
            ).detail
        }
    }

    override suspend fun support(input: SupportInput): SupportOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = supportHandler,
            ).detail
        }
    }

    override suspend fun cashDeposit(input: CashDepositInput): CashDepositOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = cashDepositHandler,
            ).detail
        }
    }

    override suspend fun cashOut(input: CashOutInput): CashOutOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = cashOutHandler,
            ).detail
        }
    }

    override suspend fun cardToCard(input: CardToCardInput): CardToCardOutput {
        return com.danesh.api.TransactionResultDetail(
            isSuccess = false,
            responseCode = "40",
            responseMessage = "Card-to-card is not available for this PSP",
            transactionType = TransactionType.CARD_TO_CARD,
        )
    }

    override suspend fun cardToWallet(input: CardToWalletInput): CardToWalletOutput {
        return com.danesh.api.TransactionResultDetail(
            isSuccess = false,
            responseCode = "40",
            responseMessage = "Card-to-wallet is not available for this PSP",
            transactionType = TransactionType.CARD_TO_WALLET,
        )
    }

    override suspend fun walletToWallet(input: WalletToWalletInput): WalletToWalletOutput {
        return com.danesh.api.TransactionResultDetail(
            isSuccess = false,
            responseCode = "40",
            responseMessage = "Wallet-to-wallet is not available for this PSP",
            transactionType = TransactionType.WALLET_TO_WALLET,
        )
    }

    override suspend fun nameInquiry(input: NameInquiryInput): NameInquiryOutput {
        return NameInquiryOutput(
            isSuccess = false,
            responseCode = "40",
            responseMessage = "Name inquiry is not available for this PSP",
        )
    }

    override suspend fun logon(masterKey: String): LogonOutput {
        BpLogonTrace.step(
            "Gateway",
            "prepareLogon — inject همان Initial Master/MAC قبل از Init (bootstrap=2)؛ " +
                "TMK پایانه از Init روی working=1",
        )
        device.getCheckValue("maqz")

        deviceOperations.prepareLogon()
        device.getCheckValue("motafaker")

        return withContext(Dispatchers.IO) {
            BpLogonTrace.step("Gateway", "اجرای تراکنش ISO logon")
            executor.execute(
                request = LogonRequest(masterKey),
                handler = logonHandler,
            ).detail.also { result ->
                BpLogonTrace.step(
                    "Gateway",
                    "پایان logon success=${result.isSuccess} rc=${result.responseCode}",
                )
            }
        }
    }

    override suspend fun init(input: InitInput): InitOutput {
        Log.d("TAG", "init: dddddddddddddnmnmf")
        BpInitTrace.step("Gateway", "شروع init ballot=")
        BpInitTrace.step("Gateway", "prepareInit — نوشتن کلید اولیه")
        device.getCheckValue("bara")
        deviceOperations.prepareInit(input)
        device.getCheckValue("hamin")
        return withContext(Dispatchers.IO) {
            BpInitTrace.step("Gateway", "اجرای تراکنش ISO")
            executor.execute(
                request = InitRequest(
                    firstBallotTicket = input.firstBallotTicket,
                    secondBallotTicket = input.secondBallotTicket,
                ),
                handler = initHandler,
            ).detail.also { result ->
                BpInitTrace.step(
                    "Gateway",
                    "پایان init ballot= success=${result.isSuccess} rc=${result.responseCode}",
                )
            }
        }
    }

    override suspend fun signOn(input: SignOnInput): SignOnOutput {
        return SignOnOutput()
    }

    override suspend fun terminalConfig(input: TerminalConfigInput): TerminalConfigOutput {
        return TerminalConfigOutput()
    }

    private fun BalanceInput.toUserInput(): BpBalanceRequest =
        BalanceUserInput(pinBlock = pinBlock, track2 = track2,pan=pan)

    private fun BillInput.toUserInput(): BpBillRequest =
        BillUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            pan = pan,
            billId = billId,
            payId = payId,
            amount = amount,
            requestId = requestId,
        )

    private fun PurchaseInput.toUserInput(): BpPurchaseRequest =
        PurchaseUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount.toString(), pan = ""
        )

    private fun VoucherInput.toUserInput(): BpVoucherRequest =
        VoucherUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount.toString(),
            pan = pan,
            operatorCode = operatorCode,
        )

    private fun TopUpInput.toUserInput(): BpTopUpRequest =
        TopUpUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount.toString(),
            pan = pan,
            mobileNumber = mobileNumber,
            operatorCode = operatorCode,
        )

    private fun SupportInput.toUserInput(): BpSupportRequest =
        SupportUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount.toString(),
            pan = pan,
            serviceId = serviceId,
        )

    private fun CashDepositInput.toUserInput(): BpCashDepositRequest =
        CashDepositUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount.toString(), destinationAccount = destinationAccount, pan = ""
        )

    private fun CashOutInput.toUserInput(): BpCashOutRequest =
        CashOutUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount.toString(), destinationAccount = destinationAccount, pan = ""
        )
}
