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
            Log.e(LOG_TAG, "onReceive():: Received intent is null. Unable to generate notification.")
            return
        }
        if (!intent.hasExtra(NotificationData.DATA_KEY_ID)) {
            Log.e(LOG_TAG, "onReceive():: ${NotificationData.DATA_KEY_ID} extra not found in intent. Unable to generate notification.")
            return
        }
        val pluginInstance = NotificationSchedulerPlugin.instance
        if (pluginInstance == null) {
            Log.e(LOG_TAG, "onReceive():: Plugin instance not found!.")
            return
        }

        val notificationData = NotificationData.from(intent)
        pluginInstance.handleNotificationDismissed(notificationData.id)
    }

    companion object {
        private val LOG_TAG = "godot::${CancelNotificationReceiver::class.java.simpleName}"
    }
}
