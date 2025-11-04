package com.lw.top.lib_core.di

import com.lw.top.lib_core.BuildConfig
import com.lw.top.lib_core.data.network.ApiService
import com.lw.top.lib_core.data.network.MockApiService
import com.lw.top.lib_core.data.network.interceptor.CustomLoggingInterceptor
import com.lw.top.lib_core.data.network.interceptor.HeaderInterceptor
import com.lw.top.lib_core.utils.TokenManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TAG = "OkHttp"
    private const val BASE_URL = "http://47.130.5.95:9091"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
    @Provides
    @Singleton
    fun provideCustomLoggingInterceptor(): CustomLoggingInterceptor {
        return CustomLoggingInterceptor()
    }

    @Provides
    @Singleton
    fun provideHeaderInterceptor(
        tokenManager: TokenManager
    ): HeaderInterceptor {
        return HeaderInterceptor(tokenManager)
    }


    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: CustomLoggingInterceptor,
        headerInterceptor: HeaderInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)//添加header
//            .addInterceptor(businessErrorAuthenticatorInterceptor)//业务异常处理
//            .authenticator(tokenRefreshAuthenticator)//Http 状态异常处理
            .addInterceptor(loggingInterceptor) // 添加日志拦截器
            .connectTimeout(30, TimeUnit.SECONDS) // 可选：设置超时
            .readTimeout(30, TimeUnit.SECONDS)    // 可选：设置超时
            .writeTimeout(30, TimeUnit.SECONDS)   // 可选：设置超时
            // .addInterceptor(AuthInterceptor()) // 如果您有其他拦截器，比如认证拦截器
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(
        okHttpClient: OkHttpClient // 真实 ApiService 依赖 OkHttpClient
    ): ApiService { // 提供 ApiService 接口
        return if (BuildConfig.USE_MOCK_API) {
            MockApiService() // <--- 直接实例化 MockApiService 并作为 ApiService 返回
        } else {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}