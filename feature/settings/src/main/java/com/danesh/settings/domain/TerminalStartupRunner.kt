package com.danesh.settings.domain

import android.content.Context
import android.util.Log
import com.danesh.api.InitInput
import com.danesh.api.PspGateway
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionTransportCodes
import com.danesh.settings.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

enum class StartupOperation { INIT, LOGON }

data class StartupStepResult(
    val operation: StartupOperation,
    val isSuccess: Boolean,
    val message: String,
)

/**
 * سداد: «پیکربندی» = INIT و «شروع به کار» = LOGON.
 * هیچ استثنایی بیرون نمی‌دهد؛ نتیجه همیشه با پیام قابل‌نمایش برمی‌گردد.
 */
@Singleton
class TerminalStartupRunner @Inject constructor(
    private val pspGateway: PspGateway,
    @ApplicationContext private val appContext: Context,
) {

    suspend fun runInit(): StartupStepResult =
        run(StartupOperation.INIT) { pspGateway.init(InitInput()) }

    suspend fun runLogon(): StartupStepResult =
        run(StartupOperation.LOGON) { pspGateway.logon("") }

    private suspend fun run(
        operation: StartupOperation,
        call: suspend () -> TransactionResultDetail,
    ): StartupStepResult {
        val detail = try {
            call()
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (error: Exception) {
            Log.e(TAG, "$operation failed with exception", error)
            return StartupStepResult(
                operation = operation,
                isSuccess = false,
                message = failedMessage(operation, appContext.getString(R.string.settings_sadad_failure_unknown)),
            )
        }
        Log.i(TAG, "$operation result success=${detail.isSuccess} code=${detail.responseCode}")
        if (detail.isSuccess) {
            return StartupStepResult(
                operation = operation,
                isSuccess = true,
                message = appContext.getString(
                    when (operation) {
                        StartupOperation.INIT -> R.string.settings_sadad_init_success
                        StartupOperation.LOGON -> R.string.settings_sadad_logon_success
                    },
                ),
            )
        }
        return StartupStepResult(
            operation = operation,
            isSuccess = false,
            message = failedMessage(operation, describeFailure(detail)),
        )
    }

    private fun describeFailure(detail: TransactionResultDetail): String {
        val code = detail.responseCode.trim()
        // کد خالی یا منفی = پاسخی از سوئیچ نرسید (اتصال/ارسال/دریافت).
        if (TransactionTransportCodes.isTransientFailure(code)) {
            return appContext.getString(R.string.settings_sadad_failure_connection)
        }
        val message = detail.responseMessage.ifBlank {
            appContext.getString(R.string.settings_sadad_failure_unknown)
        }
        return appContext.getString(R.string.settings_sadad_failure_with_code, message, code)
    }

    private fun failedMessage(operation: StartupOperation, reason: String): String =
        appContext.getString(
            when (operation) {
                StartupOperation.INIT -> R.string.settings_sadad_init_failed
                StartupOperation.LOGON -> R.string.settings_sadad_logon_failed
            },
            reason,
        )

    private companion object {
        const val TAG = "SadadStartup"
    }
}
