package ru.sign.conditional.diplomanework.api

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.sign.conditional.diplomanework.BuildConfig
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.auth.AppAuth
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiServiceModule {
    companion object {
        private const val WORK_URL = "${BuildConfig.BASE_URL}/api/"
    }

    @Singleton
    @Provides
    fun provideClient(
        appAuth: AppAuth,
        @ApplicationContext
        context: Context
    ) : OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val newRequest =
                appAuth.data.value?.token?.let { token ->
                    chain.request().newBuilder()
                        .addHeader(context.getString(R.string.auth_header), token)
                        .build()
                } ?: chain.request()
            chain.proceed(newRequest)
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        client: OkHttpClient
    ) : Retrofit = Retrofit.Builder()
        .baseUrl(WORK_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    @Singleton
    @Provides
    fun userApiService(
        retrofit: Retrofit
    ) : UserApiService =
        retrofit.create()

    @Singleton
    @Provides
    fun auxApiService(
        retrofit: Retrofit
    ) : AuxApiService =
        retrofit.create()

    @Singleton
    @Provides
    fun postApiService(
        retrofit: Retrofit
    ) : PostApiService =
        retrofit.create()

    @Singleton
    @Provides
    fun eventApiService(
        retrofit: Retrofit
    ) : EventApiService =
        retrofit.create()

    @Singleton
    @Provides
    fun jobApiService(
        retrofit: Retrofit
    ) : JobApiService =
        retrofit.create()
}