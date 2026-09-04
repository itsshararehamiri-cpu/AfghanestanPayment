package com.danesh.hp.support

import com.danesh.api.SupportCatalog
import com.danesh.api.SupportMenuItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmptySupportCatalog @Inject constructor() : SupportCatalog {
    override fun items(): List<SupportMenuItem> = emptyList()
}
