package com.danesh.sadad.config

import com.danesh.api.PspConfigurationChecker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadConfigurationChecker @Inject constructor() : PspConfigurationChecker {
    override fun isConfigured(): Boolean = true
}
