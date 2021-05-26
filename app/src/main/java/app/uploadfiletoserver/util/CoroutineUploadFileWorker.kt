package app.uploadfiletoserver.util

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.uploadfiletoserver.R
import app.uploadfiletoserver.api.RetrofitInstance
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


/**
 * Created by Mostafa Anter on 11/26/20.
 */
@FlowPreview
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class CoroutineUploadFileWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), CoroutineScope {

    // for log
    private val tag = "upload work manager"

    // init broad cast channel to hold progress
    private val progressChannel = MutableSharedFlow<Double>()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {

            //show notification on foreground
            setForeground(createForegroundInfo())

            /**
             * consume progress
             * convert to flow so we avoiding back pressure
             * */
            launch {
                progressChannel
                    .buffer()
                    .collect {
                        Log.e(tag, "success upload: $it")
                        delay(100)
                    }
            }

            // get file file here
            val mFilePath: String? = inputData.getString("mFilePath")
            if (mFilePath == null) {
                Log.e(tag, "onHandleWork: Invalid file URI")
                Result.failure()
            }

            // init api service
            val apiService = RetrofitInstance.getApiService()

            //start call api to upload file here
            apiService.onFileUpload(
                createMultipartBody(
                    mFilePath!!,
                    progressChannel,
                    this
                )
            )

            Result.success()
        } catch (e: Throwable) {
            Result.failure()
        }

    }

    private fun createMultipartBody(
        filePath: String,
        channel: MutableSharedFlow<Double>,
        coroutineScope: CoroutineScope
    ): MultipartBody.Part {
        val file = File(filePath)
        return MultipartBody.Part.createFormData(
            "photos", file.name,
            createCountingRequestBody(file, MIMEType.IMAGE.value, channel, coroutineScope)
        )
    }

    private fun createCountingRequestBody(
        file: File,
        mimeType: String,
        channel: MutableSharedFlow<Double>,
        coroutineScope: CoroutineScope
    ): RequestBody {
        val requestBody: RequestBody = createRequestBodyFromFile(file, mimeType)
        return CountingRequestBody(
            requestBody,
            object : CountingRequestBody.Listener {
                override fun onRequestProgress(bytesWritten: Long, contentLength: Long) {
                    val progress = 1.0 * bytesWritten / contentLength
                    coroutineScope.launch {
                        channel.emit(progress)
                    }
                }

            }
        )
    }

    private fun createRequestBodyFromFile(file: File, mimeType: String): RequestBody {
        return RequestBody.create(MediaType.parse(mimeType), file)
    }

    private fun createRequestBodyFromText(mText: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), mText)
    }

    //region notification
    /**
     * Create ForegroundInfo required to run a Worker in a foreground service.
     */
    private fun createForegroundInfo(): ForegroundInfo {
        // Use a different id for each Notification.
        val notificationId = 1
        return ForegroundInfo(notificationId, createNotification())
    }

    /**
     * Create the notification and required channel (O+) for running work
     * in a foreground service.
     */
    private fun createNotification(): Notification {
        val channelId = "channel-01"
        val channelName = "Channel Name"

        // This PendingIntent can be used to cancel the Worker.
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("upload file")
            .setTicker("upload in progress")
            .setSmallIcon(R.drawable.pencil)
            .setOngoing(true)
            .addAction(R.drawable.ic_baseline_close_24, "cancel", intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId, channelName).also {
                builder.setChannelId(it.id)
            }
        }
        return builder.build()
    }

    /**
     * Create the required notification channel for O+ devices.
     */
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        channelId: String,
        name: String
    ): NotificationChannel {
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }
    //endregion


}