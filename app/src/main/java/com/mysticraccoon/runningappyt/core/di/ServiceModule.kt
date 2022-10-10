package com.mysticraccoon.runningappyt.core.di

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.mysticraccoon.runningappyt.R
import com.mysticraccoon.runningappyt.core.utils.Constants
import com.mysticraccoon.runningappyt.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Named

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    @ServiceScoped
    @Named("Service")
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ): FusedLocationProviderClient {
        return FusedLocationProviderClient(app)
    }


    @Provides
    @ServiceScoped
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )

    @Provides
    @ServiceScoped
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent,
    ) =  NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false) //prevent the notification disappearing if user clicks on it
        //   .setOngoing(true) // cant be swiped away
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle("Running App")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)
    //pending intent is used here to open the main activity when clicking in the notification

}