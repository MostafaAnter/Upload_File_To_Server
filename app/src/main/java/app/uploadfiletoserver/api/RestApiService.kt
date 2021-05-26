package app.uploadfiletoserver.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Created by Mostafa Anter on 11/25/20.
 */
interface RestApiService {
    // you can replace response body with your custom response ;)
    @Multipart
    @POST("media/upload/photos")
    suspend fun onFileUpload(
        @Part file: MultipartBody.Part,
        @Header("Authorization") token: String = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1vc3RhZmEzbnRlckBnbWFpbC5jb20iLCJpZCI6Mzg3LCJ1c2VybmFtZSI6IjIwMTExNzgzMzMwMyIsInBhcnNlZE1vYmlsZU51bWJlciI6IjIwMTExNzgzMzMwMyIsInJvbGVzIjpbIlVQREFURV9VU0VSX1NFTEYiXSwiaWF0IjoxNjAzMjg4OTc0LCJleHAiOjE2MzQ4MjQ5NzQsImp0aSI6Im15Y29tbXVpbnR5X3NoZWtvaGV4In0.FA7tvp1xZmLAZCJBNdt2_MmVh4xZRTUle9J4DDxuBmY"
    ): Response<ResponseBody>
}