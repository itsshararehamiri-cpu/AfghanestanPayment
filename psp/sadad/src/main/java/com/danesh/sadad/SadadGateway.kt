package com.danesh.sadad

import android.util.Log
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
import com.danesh.api.CashDepositInput
import com.danesh.api.CashDepositOutput
import com.danesh.api.CashDepositUserInput
import com.danesh.api.CashOutInput
import com.danesh.api.CashOutOutput
import com.danesh.api.CashOutUserInput
import com.danesh.api.InitInput
import com.danesh.api.InitOutput
import com.danesh.api.InitRequest
import com.danesh.api.LogonOutput
import com.danesh.api.LogonRequest
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
import com.danesh.api.SupportInput
import com.danesh.api.SupportOutput
import com.danesh.api.SupportUserInput
import com.danesh.api.TerminalConfigInput
import com.danesh.api.TerminalConfigOutput
import com.danesh.api.TopUpInput
import com.danesh.api.TopUpOutput
import com.danesh.api.TopUpUserInput
import com.danesh.api.TransactionResultDetail
import com.danesh.api.VoucherInput
import com.danesh.api.VoucherOutput
import com.danesh.api.VoucherUserInput
import com.danesh.api.WalletToWalletInput
import com.danesh.api.WalletToWalletOutput
import com.danesh.api.WalletToWalletUserInput
import com.danesh.engine.TransactionExecutor
import com.danesh.iso.IsoMessage
import com.danesh.sadad.balance.BalanceHandler
import com.danesh.sadad.bill.BillInquiryHandler
import com.danesh.sadad.bill.BillPaymentHandler
import com.danesh.sadad.card_to_card.CardToCardHandler
import com.danesh.sadad.card_to_wallet.CardToWalletHandler
import com.danesh.sadad.cash_deposit.CashDepositHandler
import com.danesh.sadad.cash_out.CashOutHandler
import com.danesh.sadad.init.InitHandler
import com.danesh.sadad.logon.LogonHandler
import com.danesh.sadad.name_inquiry.NameInquiryHandler
import com.danesh.sadad.purchase.PurchaseHandler
import com.danesh.sadad.support.SupportHandler
import com.danesh.sadad.topup.TopUpHandler
import com.danesh.sadad.voucher.VoucherHandler
import com.danesh.sadad.wallet_to_wallet.WalletToWalletHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadGateway @Inject constructor(
    private val executor: TransactionExecutor<IsoMessage>,
    private val balanceHandler: BalanceHandler,
    private val purchaseHandler: PurchaseHandler,
    private val voucherHandler: VoucherHandler,
    private val topUpHandler: TopUpHandler,
    private val supportHandler: SupportHandler,
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
    private val deviceOperations: PspDeviceOperations,
) : PspGateway {

    override suspend fun bill(input: BillInput): BillOutput = withContext(Dispatchers.IO) {
        executor.execute(
            request = input.toPaymentUserInput(),
            handler = billPaymentHandler,
        ).detail
    }

    override suspend fun billInquiry(input: BillInquiryInput): BillInquiryOutput =
        withContext(Dispatchers.IO) {
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

    override suspend fun balance(input: BalanceInput): BalanceOutput = withContext(Dispatchers.IO) {
        executor.execute(
            request = BalanceUserInput(pinBlock = input.pinBlock, track2 = input.track2, pan = input.pan),
            handler = balanceHandler,
        ).detail
    }

    override suspend fun purchase(input: PurchaseInput): PurchaseOutput = withContext(Dispatchers.IO) {
        executor.execute(
            request = PurchaseUserInput(
                pinBlock = input.pinBlock,
                track2 = input.track2,
                amount = input.amount.toString(),
                pan = input.pan,
            ),
            handler = purchaseHandler,
        ).detail
    }

    override suspend fun voucher(input: VoucherInput): VoucherOutput = withContext(Dispatchers.IO) {
        executor.execute(
            request = VoucherUserInput(
                pinBlock = input.pinBlock,
                track2 = input.track2,
                amount = input.amount.toString(),
                pan = input.pan,
                operatorCode = input.operatorCode,
            ),
            handler = voucherHandler,
        ).detail
    }

    override suspend fun topUp(input: TopUpInput): TopUpOutput = withContext(Dispatchers.IO) {
        executor.execute(
            request = TopUpUserInput(
                pinBlock = input.pinBlock,
                track2 = input.track2,
                amount = input.amount.toString(),
                pan = input.pan,
                mobileNumber = input.mobileNumber,
                operatorCode = input.operatorCode,
            ),
            handler = topUpHandler,
        ).detail
    }

    override suspend fun support(input: SupportInput): SupportOutput = withContext(Dispatchers.IO) {
        executor.execute(
            request = SupportUserInput(
                pinBlock = input.pinBlock,
                track2 = input.track2,
                amount = input.amount.toString(),
                pan = input.pan,
                serviceId = input.serviceId,
            ),
            handler = supportHandler,
        ).detail
    }
    override suspend fun signOn(input: SignOnInput): SignOnOutput {
        return SignOnOutput()
    }
    override suspend fun cashDeposit(input: CashDepositInput): CashDepositOutput =
        withContext(Dispatchers.IO) {
            executor.execute(
                request = CashDepositUserInput(
                    pinBlock = input.pinBlock,
                    track2 = input.track2,
                    amount = input.amount.toString(),
                    destinationAccount = input.destinationAccount,
                    pan = input.pan,
                ),
                handler = cashDepositHandler,
            ).detail
        }

    override suspend fun cashOut(input: CashOutInput): CashOutOutput = withContext(Dispatchers.IO) {
        executor.execute(
            request = CashOutUserInput(
                pinBlock = input.pinBlock,
                track2 = input.track2,
                amount = input.amount.toString(),
                destinationAccount = input.destinationAccount,
                pan = input.pan,
            ),
            handler = cashOutHandler,
        ).detail
    }

    override suspend fun cardToCard(input: CardToCardInput): CardToCardOutput =
        withContext(Dispatchers.IO) {
            executor.execute(
                request = CardToCardUserInput(
                    pinBlock = input.pinBlock,
                    track2 = input.track2,
                    pan = input.pan,
                    amount = input.amount.toString(),
                    destinationPan = input.destinationPan,
                    rrn = input.rrn,
                    holderName = input.holderName,
                ),
                handler = cardToCardHandler,
            ).detail
        }
    override suspend fun terminalConfig(input: TerminalConfigInput): TerminalConfigOutput {
        return TerminalConfigOutput()
    }
    override suspend fun cardToWallet(input: CardToWalletInput): CardToWalletOutput =
        withContext(Dispatchers.IO) {
            executor.execute(
                request = CardToWalletUserInput(
                    pinBlock = input.pinBlock,
                    track2 = input.track2,
                    pan = input.pan,
                    amount = input.amount.toString(),
                    walletCode = input.walletCode,
                    rrn = input.rrn,
                    holderName = input.holderName,
                ),
                handler = cardToWalletHandler,
            ).detail
        }

    override suspend fun walletToWallet(input: WalletToWalletInput): WalletToWalletOutput =
        withContext(Dispatchers.IO) {
            executor.execute(
                request = WalletToWalletUserInput(
                    pinBlock = input.pinBlock,
                    pan = input.sourceWallet,
                    amount = input.amount.toString(),
                    destinationWallet = input.destinationWallet,
                    rrn = input.rrn,
                    holderName = input.holderName,
                ),
                handler = walletToWalletHandler,
            ).detail
        }

    override suspend fun nameInquiry(input: NameInquiryInput): NameInquiryOutput =
        withContext(Dispatchers.IO) {
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

    override suspend fun logon(masterKey: String): LogonOutput {
        deviceOperations.prepareLogon()
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = LogonRequest(masterKey),
                handler = logonHandler,
            ).detail
        }
    }

    override suspend fun init(input: InitInput): InitOutput = withContext(Dispatchers.IO) {
        Log.d("TAG", "init: ddddddddddddddnmnmf")

        executor.execute(
            request = InitRequest(
                firstBallotTicket = input.firstBallotTicket,
                secondBallotTicket = input.secondBallotTicket,
            ),
            handler = initHandler,
        ).detail
    }

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
}
