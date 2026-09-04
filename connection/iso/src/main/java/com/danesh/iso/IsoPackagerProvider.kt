package com.danesh.iso

import org.jpos.iso.ISOPackager


fun interface IsoPackagerProvider {
    fun create(): ISOPackager
}
