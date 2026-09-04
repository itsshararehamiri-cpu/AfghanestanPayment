package com.danesh.iso

import org.jpos.iso.ISOPackager

fun interface IsoMessageCreator {
    fun create(packager: ISOPackager): IsoMessage
}
