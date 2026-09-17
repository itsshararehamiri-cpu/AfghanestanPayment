package com.danesh.sadad.card_to_card

import com.danesh.api.CardToCardResult
import com.danesh.api.TransactionResultDetail

class SadadCardToCardResult(
    val detail: TransactionResultDetail,
) : CardToCardResult(detail.isSuccess)