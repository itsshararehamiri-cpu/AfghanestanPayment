package com.danesh.bp.support

import com.danesh.iso.IsoMessage

fun interface SupportMenuResponsePersister {
    fun persistFromResponse(response: IsoMessage)
}
