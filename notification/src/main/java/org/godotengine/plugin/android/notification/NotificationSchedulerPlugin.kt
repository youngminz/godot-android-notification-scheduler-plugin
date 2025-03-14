//
// © 2024-present https://github.com/cengiz-pz
//
package org.godotengine.plugin.android.notification

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.collection.ArraySet
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import org.godotengine.godot.Dictionary
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot
import org.godotengine.plugin.android.notification.model.ChannelData
import org.godotengine.plugin.android.notification.model.NotificationData

class NotificationSchedulerPlugin(godot: Godot?) : GodotPlugin(godot) {
    private var activity: Activity? = null

    /**
     * Creates a notification channel with given ID. If a channel already exists with the given ID,
     * then the call will be ignored.
     *
     * @param data dictionary containing channel ID, channel name, and channel description
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @UsedByGodot
    fun create_notification_channel(data: Dictionary) {
        val channelData = ChannelData(data)
        if (channelData.isValid) {
            val channel = NotificationChannel(
                channelData.id, channelData.name,
                channelData.importance
            )
            channel.description = channelData.description
            val manager =
                activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d(
                LOG_TAG, String.format(
                    "%s():: channel id: %s, name: %s, description: %s",
                    "create_notification_channel",
                    channelData.id,
                    channelData.name,
                    channelData.description
                )
            )
        } else {
            Log.e(LOG_TAG, "create_notification_channel(): invalid channel data object")
        }
    }

    /**
     * Schedule single, non-repeating notification
     *
     * @param data dictionary containing notification data, including delaySeconds that specifies
     * how many seconds from now to schedule the notification.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @UsedByGodot
    fun schedule(data: Dictionary) {
        val notificationData = NotificationData(data)
        if (notificationData.isValid) {
            val notificationId = notificationData.id

            val intent = Intent(
                activity!!.applicationContext,
                NotificationReceiver::class.java
            )
            intent.putExtra(NotificationData.Companion.DATA_KEY_ID, notificationId)
            intent.putExtra(
                NotificationData.Companion.DATA_KEY_CHANNEL_ID,
                notificationData.channelId
            )
            intent.putExtra(NotificationData.Companion.DATA_KEY_TITLE, notificationData.title)
            intent.putExtra(NotificationData.Companion.DATA_KEY_CONTENT, notificationData.content)
            intent.putExtra(
                NotificationData.Companion.DATA_KEY_SMALL_ICON_NAME,
                notificationData.smallIconName
            )

            if (notificationData.hasDeeplink()) {
                intent.putExtra(
                    NotificationData.Companion.DATA_KEY_DEEPLINK,
                    notificationData.deeplink
                )
            }

            if (notificationData.hasRestartAppOption()) {
                intent.putExtra(NotificationData.Companion.OPTION_KEY_RESTART_APP, true)
            }

            if (notificationData.hasInterval()) {
                scheduleRepeatingNotification(
                    activity!!,
                    notificationId,
                    intent,
                    notificationData.delay,
                    notificationData.interval
                )
            } else {
                scheduleNotification(activity!!, notificationId, intent, notificationData.delay)
            }
        } else {
            Log.e(LOG_TAG, "schedule(): invalid notification data object")
        }
    }

    /**
     * Cancel notification with given ID
     *
     * @param notificationId ID of notification to cancel
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @UsedByGodot
    fun cancel(notificationId: Int) {
        cancelNotification(activity!!, notificationId)
        Log.d(
            LOG_TAG,
            "cancel():: notification id: $notificationId"
        )
    }

    /**
     * Return notification ID if it exists in current intent, else return `defaultValue`
     *
     * @param defaultValue value to return if notification ID does not exist
     */
    @UsedByGodot
    fun get_notification_id(defaultValue: Int): Int {
        var notificationId = defaultValue
        val activity = getActivity()
        if (activity != null) {
            val intent = getActivity()!!.intent
            if (intent.hasExtra(NotificationData.Companion.DATA_KEY_ID)) {
                notificationId =
                    intent.getIntExtra(NotificationData.Companion.DATA_KEY_ID, defaultValue)
                Log.i(
                    LOG_TAG,
                    "get_notification_id():: intent with notification id: $notificationId"
                )
            } else {
                Log.i(LOG_TAG, "get_notification_id():: notification id not found")
            }
        }
        return notificationId
    }

    /**
     * Returns true if app has already been granted POST_NOTIFICATIONS permissions
     */
    @UsedByGodot
    fun has_post_notifications_permission(): Boolean {
        var result = false
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            if (NotificationManagerCompat.from(activity!!.applicationContext)
                    .areNotificationsEnabled()
            ) {
                result = true
            }
        } else {
            result = true
            Log.d(
                LOG_TAG,
                "has_post_notifications_permission():: API level is " + Build.VERSION.SDK_INT
            )
        }
        return result
    }

    /**
     * Sends a request to acquire POST_NOTIFICATIONS permission for the app
     */
    @UsedByGodot
    fun request_post_notifications_permission() {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                ActivityCompat.requestPermissions(
                    activity!!, arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE
                )
            } else {
                Log.i(
                    LOG_TAG,
                    "request_post_notifications_permission():: can't request permission, because SDK version is " + Build.VERSION.SDK_INT
                )
            }
        } catch (e: Exception) {
            Log.e(
                LOG_TAG,
                "request_post_notifications_permission():: Failed to request permission due to " + e.message
            )
        }
    }

    /**
     * Opens APP INFO settings screen
     */
    @UsedByGodot
    fun open_app_info_settings() {
        Log.d(LOG_TAG, "open_app_info_settings()")

        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.fromParts("package", activity!!.packageName, null)
            intent.setData(uri)
            activity!!.startActivity(intent)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "open_app_info_settings():: Failed due to " + e.message)
        }
    }

    override fun getPluginName(): String {
        return javaClass.simpleName
    }

    override fun getPluginSignals(): Set<SignalInfo> {
        val signals: MutableSet<SignalInfo> = ArraySet()
        signals.add(NOTIFICATION_OPENED_SIGNAL)
        signals.add(NOTIFICATION_DISMISSED_SIGNAL)
        signals.add(PERMISSION_GRANTED_SIGNAL)
        signals.add(PERMISSION_DENIED_SIGNAL)
        return signals
    }

    override fun onMainCreate(activity: Activity): View? {
        this.activity = activity
        instance = this
        return super.onMainCreate(activity)
    }

    override fun onGodotSetupCompleted() {
        super.onGodotSetupCompleted()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            val activity = getActivity()
            if (activity != null) {
                if (NotificationManagerCompat.from(activity.applicationContext)
                        .areNotificationsEnabled()
                ) {
                    Log.i(
                        LOG_TAG,
                        "onGodotSetupCompleted():: POST_NOTIFICATIONS permission has already been granted"
                    )
                }
            } else {
                Log.e(
                    LOG_TAG,
                    "onGodotSetupCompleted():: can't check permission status due to null activity"
                )
            }
        }
    }

    override fun onMainDestroy() {
        instance = null
        super.onMainDestroy()
    }

    override fun onMainRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onMainRequestPermissionsResult(requestCode, permissions, grantResults)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            if (requestCode == POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "onMainRequestPermissionsResult():: permission request granted")
                    emitSignal(
                        godot,
                        pluginName,
                        PERMISSION_GRANTED_SIGNAL,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    Log.d(LOG_TAG, "onMainRequestPermissionsResult():: permission request denied")
                    emitSignal(
                        godot,
                        pluginName, PERMISSION_DENIED_SIGNAL, Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        } else {
            Log.e(
                LOG_TAG,
                "onMainRequestPermissionsResult():: can't check permission result, because SDK version is " + Build.VERSION.SDK_INT
            )
        }
    }

    fun handleNotificationOpened(notificationId: Int) {
        emitSignal(
            godot,
            pluginName, NOTIFICATION_OPENED_SIGNAL, notificationId
        )
    }

    fun handleNotificationDismissed(notificationId: Int) {
        emitSignal(
            godot,
            pluginName, NOTIFICATION_DISMISSED_SIGNAL, notificationId
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun calculateTimeAfterDelay(delaySeconds: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, delaySeconds)
        return calendar.timeInMillis
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun scheduleNotification(
        activity: Activity,
        notificationId: Int,
        intent: Intent,
        delaySeconds: Int
    ) {
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val timeAfterDelay = calculateTimeAfterDelay(delaySeconds)
        alarmManager[AlarmManager.RTC_WAKEUP, timeAfterDelay] = PendingIntent.getBroadcast(
            activity.applicationContext, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.i(
            LOG_TAG,
            String.format(
                "Scheduled notification '%d' to be delivered at %d.",
                notificationId,
                timeAfterDelay
            )
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun scheduleRepeatingNotification(
        activity: Activity,
        notificationId: Int,
        intent: Intent,
        delaySeconds: Int,
        intervalSeconds: Int
    ) {
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val timeAfterDelay = calculateTimeAfterDelay(delaySeconds)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP, timeAfterDelay, intervalSeconds * 1000L,
            PendingIntent.getBroadcast(
                activity.applicationContext, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        Log.i(
            LOG_TAG,
            String.format(
                "Scheduled notification '%d' to be delivered at %d with %ds interval.",
                notificationId,
                timeAfterDelay,
                intervalSeconds
            )
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun cancelNotification(activity: Activity, notificationId: Int) {
        val context = activity.applicationContext

        // cancel alarm
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra(NotificationData.Companion.DATA_KEY_ID, notificationId)
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                activity.applicationContext, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        // cancel notification
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    companion object {
        private val LOG_TAG = "godot::" + NotificationSchedulerPlugin::class.java.simpleName

        var instance: NotificationSchedulerPlugin? = null

        private val PERMISSION_GRANTED_SIGNAL = SignalInfo(
            "permission_granted",
            String::class.java
        )
        private val PERMISSION_DENIED_SIGNAL = SignalInfo(
            "permission_denied",
            String::class.java
        )
        private val NOTIFICATION_OPENED_SIGNAL = SignalInfo(
            "notification_opened",
            Int::class.java
        )
        private val NOTIFICATION_DISMISSED_SIGNAL = SignalInfo(
            "notification_dismissed",
            Int::class.java
        )

        private const val POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE = 11803
    }
}
