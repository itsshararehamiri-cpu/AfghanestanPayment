package com.danesh.hp

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
    private val  terminalConfigHandler: TerminalConfigHandler,
    private val deviceOperations: PspDeviceOperations,
) : PspGateway {
    override suspend fun bill(input: BillInput): BillOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toPaymentUserInput(),
                handler = billPaymentHandler,
            ).detail
        }
    }

    override suspend fun billInquiry(input: BillInquiryInput): BillInquiryOutput {
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
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = balanceHandler,
            ).detail
        }
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
        return com.danesh.api.TransactionResultDetail(
            isSuccess = false,
            responseCode = "40",
            responseMessage = "Support transaction is not available for this PSP",
            transactionType = TransactionType.SUPPORT,
        )
    }

    override suspend fun logon(masterKey: String): LogonOutput {
        Log.d("TAG", "logon: dddhhddddddddddddddddddddd")
        deviceOperations.prepareLogon()
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = LogonRequest(masterKey),
                handler = logonHandler,
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
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = cardToCardHandler,
            ).detail
        }
    }

    override suspend fun cardToWallet(input: CardToWalletInput): CardToWalletOutput {
        return withContext(Dispatchers.IO) {
            executor.execute(
                request = input.toUserInput(),
                handler = cardToWalletHandler,
            ).detail
        }
    }

    override suspend fun walletToWallet(input: WalletToWalletInput): WalletToWalletOutput {
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
            Log.d("TAG", "init: ddddddddddddddrr")
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
        BalanceUserInput(pinBlock = pinBlock, track2 = track2,pan=pan)

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
      //  return SignOnOutput()
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
            amount = amount.toString(), destinationAccount = destinationAccount, pan = ""
        )

    private fun CashOutInput.toUserInput(): HpCashOutRequest =
        CashOutUserInput(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount.toString(), destinationAccount = destinationAccount, pan = ""
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
}
