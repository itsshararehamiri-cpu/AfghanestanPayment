package com.danesh.afghanestanpayment

import android.app.Application
import android.util.Log
import com.danesh.api.PspGateway
import com.danesh.common.locale.LocaleManager
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.startup.AppStartupTask
import com.danesh.settings.locale.SettingsLanguageOptions
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApp : Application() {

    @Inject
    lateinit var localePreferences: LocalePreferences

    @Inject
    lateinit var languageOptions: SettingsLanguageOptions

    @Inject
    lateinit var startupTasks: Set<@JvmSuppressWildcards AppStartupTask>



    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val saved = localePreferences.getLanguage()
        val coerced = languageOptions.coerceCoreLanguage(saved)
        if (coerced != saved) {
            localePreferences.setLanguage(coerced)
        }
        LocaleManager.apply(coerced)
        runStartupTasks()
        //runStartupTasks2()

    }

    override fun onLowMemory() {
        super.onLowMemory()
    }
//    private fun runStartupTasks2() {
//        appScope.launch {
//            for (task in startupTasks) {
//                runCatching {
//                    task.run()
//                }.onFailure { error ->
//                    Log.e(
//                        "MyApp",
//                        "Startup task failed: ${error.message}",
//                        error
//                    )
//                }
//            }
//        }
//    }
    private fun runStartupTasks() {
        if (startupTasks.isEmpty()) return
        appScope.launch {
            for (task in startupTasks) {
                runCatching {
                    Log.d("TAG", "runStartupTasks: hhhhhhjhgvfghj")

                    task.run() }
                    .onFailure { error ->
                        android.util.Log.e("MyApp", "Startup task failed: ${error.message}", error)
                    }
            }
        }
    }
}
