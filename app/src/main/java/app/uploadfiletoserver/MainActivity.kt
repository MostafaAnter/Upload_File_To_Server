package app.uploadfiletoserver

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.uploadfiletoserver.util.CoroutineUploadFileWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.io.File
import java.io.IOException


@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonUpload: Button  = findViewById(R.id.buttonUpload)
        buttonUpload.setOnClickListener {
            callWorkManagerToUploadImage()
        }

    }

    private fun callWorkManagerToUploadImage(){
        /***  Logic to set Data while creating worker **/
        val uploadFileWorker = OneTimeWorkRequest.Builder(CoroutineUploadFileWorker::class.java)
        val data = Data.Builder()
        //Add parameter in Data class. just like bundle. You can also add Boolean and Number in parameter.
        data.putString(
                "mFilePath",
                getFileFromAssets(this, "photo.jpg").absolutePath)
        //Set Input Data
        uploadFileWorker.setInputData(data.build())
        //enqueue worker
        WorkManager.getInstance(this).enqueue(uploadFileWorker.build())
    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File = File(context.cacheDir, fileName)
            .also {
                if (!it.exists()) {
                    it.outputStream().use { cache ->
                        context.assets.open(fileName).use { inputStream ->
                            inputStream.copyTo(cache)
                        }
                    }
                }
            }
}