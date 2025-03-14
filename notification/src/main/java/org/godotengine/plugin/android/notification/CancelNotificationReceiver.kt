//
// © 2024-present https://github.com/cengiz-pz
//
package org.godotengine.plugin.android.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.godotengine.plugin.android.notification.model.NotificationData

class CancelNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) {
            Log.e(
                LOG_TAG, String.format(
                    "%s():: Received intent is null. Unable to generate notification.",
                    "onReceive"
                )
            )
        } else if (intent.hasExtra(NotificationData.Companion.DATA_KEY_ID)) {
            val notificationId = intent.getIntExtra(NotificationData.Companion.DATA_KEY_ID, 0)
            if (NotificationSchedulerPlugin.Companion.instance == null) {
                Log.e(LOG_TAG, String.format("%s():: Plugin instance not found!.", "onReceive"))
            } else {
                NotificationSchedulerPlugin.Companion.instance.handleNotificationDismissed(
                    notificationId
                )
            }
        } else {
            Log.e(
                LOG_TAG, String.format(
                    "%s():: %s extra not found in intent. Unable to generate notification.",
                    "onReceive", NotificationData.Companion.DATA_KEY_ID
                )
            )
        }
    }

    companion object {
        private val LOG_TAG = "godot::" + CancelNotificationReceiver::class.java.simpleName

        private const val ICON_RESOURCE_TYPE = "drawable"
    }
}
