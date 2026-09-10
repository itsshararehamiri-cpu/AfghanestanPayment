package com.danesh.hp

import com.danesh.api.PspGateway
import com.danesh.api.TerminalConfigInput
import com.danesh.common.startup.AppStartupTask
import javax.inject.Inject

/** استارتاپ همراه‌پی نزد کارن: به‌جای Network Sign-On (1804)، پیکربندی اختیاری پایانه را ارسال می‌کند. */
class KarenSignOnStartupTask @Inject constructor(
    private val pspGateway: PspGateway,
) : AppStartupTask {

    override suspend fun run() {
        pspGateway.terminalConfig(TerminalConfigInput(""))
    }
}

