package com.mysticraccoon.runningappyt.core.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Named

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    @FragmentScoped
    @Named("Fragment")
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ): FusedLocationProviderClient {
        return FusedLocationProviderClient(app)
    }


}