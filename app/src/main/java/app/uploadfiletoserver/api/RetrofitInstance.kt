package app.uploadfiletoserver.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by Mostafa Anter on 11/25/20.
 */
class RetrofitInstance {
    companion object{
        private lateinit var retrofit: Retrofit
        fun getApiService(): RestApiService {
            val  logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val client = OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
//                .addInterceptor { chain: Interceptor.Chain ->
//                    val original: Request = chain.request()
//                    val requestBuilder = original.newBuilder().method(original.method(), original.body())
//                    val request: Request = requestBuilder.build()
//                    chain.proceed(request)
//                }
                .addInterceptor(logging)
                .build()
            if (!this::retrofit.isInitialized) {
                retrofit = Retrofit.Builder()
                    .baseUrl("https://beta.klliq.com/api/v1/")
                    .client(client)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
            }
            return retrofit.create(RestApiService::class.java)
        }
    }
}