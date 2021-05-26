package app.uploadfiletoserver.util

import okhttp3.MediaType
import okio.Buffer
import okhttp3.RequestBody
import okio.BufferedSink
import okio.ForwardingSink
import okio.Okio
import okio.Sink
import java.io.IOException


/**
 * Created by Mostafa Anter on 11/25/20.
 */
class CountingRequestBody(
    private val delegate: RequestBody,
    private val listener: Listener
) : RequestBody() {

    override fun contentType(): MediaType? {
        return delegate.contentType()
    }

    override fun contentLength(): Long {
        try {
            return delegate.contentLength()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return -1
    }

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = Okio.buffer(countingSink)
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    internal inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
        private var bytesWritten: Long = 0

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            listener.onRequestProgress(bytesWritten, contentLength())
        }
    }

    interface Listener {
        fun onRequestProgress(bytesWritten: Long, contentLength: Long)
    }
}