//
// © 2024-present https://github.com/cengiz-pz
//
package org.godotengine.plugin.android.notification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.godotengine.plugin.android.notification.model.NotificationData
import androidx.core.net.toUri

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationData = NotificationData.from(intent)
        val godotIntent = Intent(applicationContext, godotAppMainActivityClass).apply {
            putExtras(intent)

            Log.i(LOG_TAG, "restartApp: ${notificationData.restartApp}")

            flags = if (notificationData.restartApp) {
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            } else {
                Intent.FLAG_ACTIVITY_NEW_TASK
            }

            notificationData.deeplink?.let { deeplink ->
                data = deeplink.toUri()
            }
        }

        Log.i(LOG_TAG, "Starting activity with intent: $godotIntent")
        startActivity(godotIntent)

        val pluginInstance = NotificationSchedulerPlugin.instance
        if (pluginInstance != null && notificationData.id != -1) {
            Log.e(LOG_TAG, "Handling notification opened. Plugin instance: $pluginInstance, notification ID: ${notificationData.id}")
            // TODO: Handle in Godot app (check data on app resume/restart)
            pluginInstance.handleNotificationOpened(notificationData.id.toInt())
        } else {
            Log.w(LOG_TAG, "Ignoring notification. Plugin instance: $pluginInstance, notification ID: ${notificationData.id}")
        }
    }

    companion object {
        private val LOG_TAG = "godot::${ResultActivity::class.java.simpleName}"

        private const val GODOT_APP_MAIN_ACTIVITY_CLASSPATH = "com.godot.game.GodotApp"
        private var godotAppMainActivityClass: Class<*>? = null

        init {
            try {
                godotAppMainActivityClass = Class.forName(GODOT_APP_MAIN_ACTIVITY_CLASSPATH)
            } catch (e: ClassNotFoundException) {
                Log.e(LOG_TAG, "could not find $GODOT_APP_MAIN_ACTIVITY_CLASSPATH")
            }
        }
    }
}
