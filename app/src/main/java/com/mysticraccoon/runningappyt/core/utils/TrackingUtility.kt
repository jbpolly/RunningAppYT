package com.mysticraccoon.runningappyt.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.mysticraccoon.runningappyt.core.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hasLocationPermissions(context: Context): Boolean {

        val foregroundLocationApproved = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val backgroundLocationApproved = if (runningAndroidQOrLater()) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return foregroundLocationApproved && backgroundLocationApproved
    }

    fun runningAndroidQOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String{
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if(!includeMillis){
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds:" +
                "${if(milliseconds < 10) "0" else ""}$milliseconds"
    }

    fun calculatePolylineLength(polyline: Polyline): Float {
        var distance = 0f
        //loop the distances of our polylines
        //-2 because now we want to get the first position and one position after that | so it wont reach invalid index at the end
        for(i in 0..polyline.size - 2) {
            val pos1 = polyline[i]
            val pos2 = polyline[i + 1]

            //the result will be outputted in this array
            val result = FloatArray(1)
            //calculate the distance between two latlng positions
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            distance += result[0]
        }
        return distance
    }

}