//
// © 2024-present https://github.com/cengiz-pz
//
package org.godotengine.plugin.android.notification.model

import android.content.Context
import android.content.Intent
import org.godotengine.godot.Dictionary
import org.godotengine.plugin.android.notification.NotificationReceiver

data class NotificationData(
    val id: Int,
    val channelId: String,
    val title: String,
    val content: String,
    val smallIconName: String,
    /** How many seconds from now to schedule first notification */
    val delay: Int,
    /** URI to process as app link when notification opened */
    val deeplink: String?,
    /** Interval in seconds between each repeating notification */
    val interval: Int?,
    /** If enabled, app will be restarted when notification is opened */
    val restartApp: Boolean
) {
    companion object {
        const val DATA_KEY_ID: String = "notification_id"
        const val DATA_KEY_CHANNEL_ID: String = "channel_id"
        const val DATA_KEY_TITLE: String = "title"
        const val DATA_KEY_CONTENT: String = "content"
        const val DATA_KEY_SMALL_ICON_NAME: String = "small_icon_name"
        const val DATA_KEY_DELAY: String = "delay"
        const val DATA_KEY_DEEPLINK: String = "deeplink"
        const val DATA_KEY_INTERVAL: String = "interval"
        const val OPTION_KEY_RESTART_APP: String = "restart_app"

        fun from(data: Dictionary): NotificationData {
            val id = data[DATA_KEY_ID] as Int
            val channelId = data[DATA_KEY_CHANNEL_ID] as String
            val title = data[DATA_KEY_TITLE] as String
            val content = data[DATA_KEY_CONTENT] as String
            val smallIconName = data[DATA_KEY_SMALL_ICON_NAME] as String
            val delay = data[DATA_KEY_DELAY] as Int
            val deeplink = data[DATA_KEY_DEEPLINK] as? String
            val interval = data[DATA_KEY_INTERVAL] as? Int
            val restartApp = data[OPTION_KEY_RESTART_APP] as? Boolean ?: false

            return NotificationData(id, channelId, title, content, smallIconName, delay, deeplink, interval, restartApp)
        }

        fun from(intent: Intent): NotificationData {
            val id = intent.getIntExtra(DATA_KEY_ID, -1)
            val channelId = intent.getStringExtra(DATA_KEY_CHANNEL_ID) ?: ""
            val title = intent.getStringExtra(DATA_KEY_TITLE) ?: ""
            val content = intent.getStringExtra(DATA_KEY_CONTENT) ?: ""
            val smallIconName = intent.getStringExtra(DATA_KEY_SMALL_ICON_NAME) ?: ""
            val delay = intent.getIntExtra(DATA_KEY_DELAY, -1)
            val deeplink = intent.getStringExtra(DATA_KEY_DEEPLINK)
            val interval = intent.getIntExtra(DATA_KEY_INTERVAL, -1)
            val restartApp = intent.getBooleanExtra(OPTION_KEY_RESTART_APP, false)

            return NotificationData(id, channelId, title, content, smallIconName, delay, deeplink, interval, restartApp)
        }
    }

    fun toIntent(context: Context): Intent {
        return Intent(context, NotificationReceiver::class.java).apply {
            putExtra(DATA_KEY_ID, id)
            putExtra(DATA_KEY_CHANNEL_ID, channelId)
            putExtra(DATA_KEY_TITLE, title)
            putExtra(DATA_KEY_CONTENT, content)
            putExtra(DATA_KEY_SMALL_ICON_NAME, smallIconName)
            putExtra(DATA_KEY_DELAY, delay)
            deeplink?.let { putExtra(DATA_KEY_DEEPLINK, it) }
            interval?.let { putExtra(DATA_KEY_INTERVAL, it) }
            putExtra(OPTION_KEY_RESTART_APP, restartApp)
        }
    }
}
