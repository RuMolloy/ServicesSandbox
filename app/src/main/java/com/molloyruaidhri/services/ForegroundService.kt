package com.molloyruaidhri.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.molloyruaidhri.services.Constants.ACTION_PAUSE_SERVICE
import com.molloyruaidhri.services.Constants.ACTION_START_OR_RESUME_SERVICE
import com.molloyruaidhri.services.Constants.ACTION_STOP_SERVICE
import com.molloyruaidhri.services.Constants.NOTIFICATION_CHANNEL_ID
import com.molloyruaidhri.services.Constants.NOTIFICATION_CHANNEL_NAME
import com.molloyruaidhri.services.Constants.NOTIFICATION_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForegroundService: LifecycleService() {

    var isFirstRun = true
    var isTimerRunning = false

    private val timeRunInSeconds = MutableLiveData<Long>()

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
    }

    private fun postInitialValues() {
        timeRunInSeconds.postValue(0L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun) {
                        println("Starting Service")
                        startForegroundService()
                        isFirstRun = false
                    }
                    else {
                        println("Resuming Service")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    println("Paused Service")
                    pauseTimer()
                }
                ACTION_STOP_SERVICE -> {
                    println("Stopped Service")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        startTimer()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val intent = packageManager.getLaunchIntentForPackage("com.molloyruaidhri.services")
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentTitle("Foreground Service Timer")
            .setContentText("00:00:00")
            .setContentIntent(PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                FLAG_IMMUTABLE
            ))

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timeRunInSeconds.observe(this) {
            notificationBuilder.setContentText(Util.getFormattedStopWatchTime(it * 1000L))
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())


        }
    }

    private var timer = 0L
    private var timeDiff = 0L
    private var timeStarted = 0L
    private var timeRunInMillis = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        isTimerRunning = true
        timeStarted = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (isTimerRunning) {
                timeDiff = System.currentTimeMillis() - timeStarted
                timeRunInMillis = timer + timeDiff
                if (timeRunInMillis >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(50L)
            }
            timer += timeDiff
        }
    }

    private fun pauseTimer() {
        isTimerRunning = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, ForegroundService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, ForegroundService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

}