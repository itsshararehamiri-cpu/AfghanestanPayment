package com.danesh.afghanestanpayment
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
class BootLaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       BootForegroundHelper.apply(this)
        bringAppToForeground()
        window.decorView.postDelayed({
            bringAppToForeground()
            finish()
        }, BRING_TO_FRONT_RETRY_MS)
    }

    override fun onResume() {
        super.onResume()
        BootForegroundHelper.apply(this)
    }

    private fun bringAppToForeground() {
        startActivity(mainIntent())
    }

    private fun mainIntent(): Intent =
        Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
            )
            putExtra(BootLaunchService.EXTRA_LAUNCHED_FROM_BOOT, true)
        }

    companion object {
        private const val BRING_TO_FRONT_RETRY_MS = 1_500L
    }
}
