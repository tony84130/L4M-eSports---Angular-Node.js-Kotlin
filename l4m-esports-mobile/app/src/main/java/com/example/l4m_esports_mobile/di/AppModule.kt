package com.example.l4m_esports_mobile.di

import android.content.Context
import com.example.l4m_esports_mobile.data.local.PreferencesManager
import com.example.l4m_esports_mobile.network.ApiService
import com.example.l4m_esports_mobile.network.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        preferencesManager: PreferencesManager
    ): AuthInterceptor {
        return AuthInterceptor(preferencesManager)
    }

    @Provides
    @Singleton
    fun provideApiService(
        authInterceptor: AuthInterceptor
    ): ApiService {
        return ApiService(authInterceptor)
    }
}

