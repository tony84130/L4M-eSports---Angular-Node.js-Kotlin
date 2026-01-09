package com.example.l4m_esports_mobile.di

import com.example.l4m_esports_mobile.data.remote.AuthApiService
import com.example.l4m_esports_mobile.data.remote.EventApiService
import com.example.l4m_esports_mobile.data.remote.EventRegistrationApiService
import com.example.l4m_esports_mobile.data.remote.GameApiService
import com.example.l4m_esports_mobile.data.remote.MatchApiService
import com.example.l4m_esports_mobile.data.remote.AiApiService
import com.example.l4m_esports_mobile.data.remote.NotificationApiService
import com.example.l4m_esports_mobile.data.remote.TeamApiService
import com.example.l4m_esports_mobile.data.remote.TeamRequestApiService
import com.example.l4m_esports_mobile.data.remote.TwitchApiService
import com.example.l4m_esports_mobile.data.remote.UserApiService
import com.example.l4m_esports_mobile.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthApiService(apiService: ApiService): AuthApiService {
        return apiService.create()
    }

    @Provides
    @Singleton
    fun provideUserApiService(apiService: ApiService): UserApiService {
        return apiService.create()
    }

    @Provides
    @Singleton
    fun provideGameApiService(apiService: ApiService): GameApiService {
        return apiService.create()
    }

    @Provides
    @Singleton
    fun provideTeamApiService(apiService: ApiService): TeamApiService {
        return apiService.create()
    }

    @Provides
    @Singleton
    fun provideTeamRequestApiService(apiService: ApiService): TeamRequestApiService {
        return apiService.create()
    }

    @Provides
    @Singleton
    fun provideNotificationApiService(apiService: ApiService): NotificationApiService {
        return apiService.create()
    }

    @Provides
    @Singleton
    fun provideEventApiService(apiService: ApiService): EventApiService {
        return apiService.create()
    }

    @Provides
    @Singleton
    fun provideEventRegistrationApiService(apiService: ApiService): EventRegistrationApiService {
        return apiService.create()
    }

    @Provides
    @Singleton
    fun provideMatchApiService(apiService: ApiService): MatchApiService {
        return apiService.create()
    }

    @Provides
    @Singleton
    fun provideTwitchApiService(apiService: ApiService): TwitchApiService {
        return apiService.create()
    }

    @Provides
    @Singleton
    fun provideAiApiService(apiService: ApiService): AiApiService {
        return apiService.create()
    }
}

