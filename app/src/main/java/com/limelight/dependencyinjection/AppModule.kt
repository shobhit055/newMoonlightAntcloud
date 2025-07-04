package com.limelight.dependencyinjection

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
//    private const val BASE_URL = "https://api.antcloud.co/api/"
    private const val BASE_URL = "https://ocelot-fitting-treefrog.ngrok-free.app/api/"
    @Singleton
    @Provides
    fun apiRepository(api: ApiService) = AuthRepository(api) as AuthRepositoryInterface
    @Singleton
    @Provides
    fun injectBackendRetrofitApi() : ApiService {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor)

        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build()).build()
            .create(ApiService::class.java)
    }
}