package com.danesh.afghanestanpayment.app

import com.danesh.afghanestanpayment.BuildConfig
import com.danesh.common.app.AppVersionProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildConfigAppVersionProvider @Inject constructor() : AppVersionProvider {
    override fun versionName(): String = BuildConfig.VERSION_NAME
}
