package com.danesh.hp

import android.util.Log
import com.danesh.api.PspGateway
import com.danesh.api.SignOnInput
import com.danesh.api.TerminalConfigInput
import com.danesh.common.startup.AppStartupTask
import javax.inject.Inject

class KarenSignOnStartupTask @Inject constructor(
    private val pspGateway: PspGateway
) : AppStartupTask {

    override suspend fun run() {
        Log.d("TAG", "run: KarenSignOnStartupTaskKarenSignOnStartupTask")
       // pspGateway.signOn(SignOnInput(""))
        pspGateway.terminalConfig(TerminalConfigInput(""))
    }
}


