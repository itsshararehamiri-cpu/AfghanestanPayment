package com.danesh.iso

import org.jpos.iso.ISOPackager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IsoMessageProvider @Inject constructor(
    packagerProvider: IsoPackagerProvider,
    private val messageCreator: IsoMessageCreator,
) {
    private val packager: ISOPackager = packagerProvider.create()

    fun create(): IsoMessage = messageCreator.create(packager)

    fun packager(): ISOPackager = packager
}
