package com.pasiflonet.mobile.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Temporary stub: keeps CI buildable.
 * Replace with real export/send pipeline later.
 */
class SendWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // TODO: implement real sending
        return Result.success()
    }

    companion object {
        const val KEY_IN_PATH = "in_path"
        const val KEY_OUT_PATH = "out_path"
        const val KEY_CAPTION = "caption"
        const val KEY_ERROR = "error"
    }
}
