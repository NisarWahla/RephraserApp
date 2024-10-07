package dzm.dzmedia.repheraserapp.authentication_api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitAuthenticationClient {
    companion object {
        private lateinit var retrofit: Retrofit

        fun AuthClient(): AuthApi {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(interceptor).build()

            retrofit = Retrofit.Builder()
                .baseUrl("https://www.rephraser.co/api/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client).build()
            return retrofit.create(AuthApi::class.java)
        }
    }
}