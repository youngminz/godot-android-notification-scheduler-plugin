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

        val thisIntent = intent
        val godotIntent = Intent(applicationContext, godotAppMainActivityClass)
        godotIntent.putExtras(thisIntent)
        val notificationData = NotificationData.from(thisIntent)

        if (notificationData.restartApp != null) {
            godotIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        } else {
            godotIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (godotIntent.hasExtra(NotificationData.DATA_KEY_DEEPLINK)) {
            godotIntent.setData(godotIntent.getStringExtra(NotificationData.DATA_KEY_DEEPLINK)!!.toUri())
        }
        Log.i(LOG_TAG, "Starting activity with intent: $godotIntent")
        startActivity(godotIntent)

        val bundle = intent.extras
        val pluginInstance = NotificationSchedulerPlugin.instance

        if (pluginInstance != null && bundle != null && bundle.containsKey(NotificationData.DATA_KEY_ID)) {
            // TODO: Handle in Godot app (check data on app resume/restart)
            pluginInstance.handleNotificationOpened(bundle.getInt(NotificationData.DATA_KEY_ID))
        } else {
            Log.w(LOG_TAG, """Ignoring notification. Reason: ${if (NotificationSchedulerPlugin.instance == null) "instance null" else if (bundle == null) "bundle null" else "bundle empty"}""")
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
