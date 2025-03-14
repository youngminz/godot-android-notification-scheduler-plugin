//
// © 2024-present https://github.com/cengiz-pz
//
package org.godotengine.plugin.android.notification.model

import android.content.Intent
import org.godotengine.godot.Dictionary

class NotificationData {
    private var data: Dictionary

    constructor(data: Dictionary) {
        this.data = data
    }

    constructor(intent: Intent) {
        this.data = Dictionary()
        if (intent.hasExtra(DATA_KEY_ID)) {
            data[DATA_KEY_ID] =
                intent.getIntExtra(DATA_KEY_ID, -1)
        }
        if (intent.hasExtra(DATA_KEY_CHANNEL_ID)) {
            data[DATA_KEY_CHANNEL_ID] =
                intent.getStringExtra(DATA_KEY_CHANNEL_ID)
        }
        if (intent.hasExtra(DATA_KEY_TITLE)) {
            data[DATA_KEY_TITLE] =
                intent.getStringExtra(DATA_KEY_TITLE)
        }
        if (intent.hasExtra(DATA_KEY_CONTENT)) {
            data[DATA_KEY_CONTENT] =
                intent.getStringExtra(DATA_KEY_CONTENT)
        }
        if (intent.hasExtra(DATA_KEY_SMALL_ICON_NAME)) {
            data[DATA_KEY_SMALL_ICON_NAME] =
                intent.getStringExtra(DATA_KEY_SMALL_ICON_NAME)
        }
        if (intent.hasExtra(DATA_KEY_DELAY)) {
            data[DATA_KEY_DELAY] =
                intent.getIntExtra(DATA_KEY_DELAY, -1)
        }
        if (intent.hasExtra(DATA_KEY_DEEPLINK)) {
            data[DATA_KEY_DEEPLINK] =
                intent.getStringExtra(DATA_KEY_DEEPLINK)
        }
        if (intent.hasExtra(DATA_KEY_INTERVAL)) {
            data[DATA_KEY_INTERVAL] =
                intent.getIntExtra(DATA_KEY_INTERVAL, -1)
        }
        if (intent.hasExtra(OPTION_KEY_RESTART_APP)) {
            data[OPTION_KEY_RESTART_APP] =
                intent.getBooleanExtra(OPTION_KEY_RESTART_APP, true)
        }
    }

    val id: Int?
        get() = data[DATA_KEY_ID] as Int?

    val channelId: String?
        get() = data[DATA_KEY_CHANNEL_ID] as String?

    val title: String?
        get() = data[DATA_KEY_TITLE] as String?

    val content: String?
        get() = data[DATA_KEY_CONTENT] as String?

    val smallIconName: String?
        get() = data[DATA_KEY_SMALL_ICON_NAME] as String?

    val delay: Int?
        /**
         * How many seconds from now to schedule first notification
         */
        get() = data[DATA_KEY_DELAY] as Int?

    fun hasDeeplink(): Boolean {
        return data.containsKey(DATA_KEY_DEEPLINK)
    }

    val deeplink: String?
        /**
         * URI to process as app link when notification opened
         */
        get() = data[DATA_KEY_DEEPLINK] as String?

    fun hasInterval(): Boolean {
        return data.containsKey(DATA_KEY_INTERVAL)
    }

    val interval: Int?
        /**
         * Interval in seconds between each repeating notification
         */
        get() = data[DATA_KEY_INTERVAL] as Int?

    /**
     * If enabled, app will be restarted when notification is opened
     */
    fun hasRestartAppOption(): Boolean {
        return data.containsKey(OPTION_KEY_RESTART_APP)
    }

    val isValid: Boolean
        get() = data.containsKey(DATA_KEY_ID) &&
                data.containsKey(DATA_KEY_CHANNEL_ID) &&
                data.containsKey(DATA_KEY_TITLE) &&
                data.containsKey(DATA_KEY_CONTENT) &&
                data.containsKey(DATA_KEY_SMALL_ICON_NAME) &&
                data.containsKey(DATA_KEY_DELAY)

    companion object {
        var DATA_KEY_ID: String = "notification_id"
        var DATA_KEY_CHANNEL_ID: String = "channel_id"
        var DATA_KEY_TITLE: String = "title"
        var DATA_KEY_CONTENT: String = "content"
        var DATA_KEY_SMALL_ICON_NAME: String = "small_icon_name"
        var DATA_KEY_DELAY: String = "delay"
        var DATA_KEY_DEEPLINK: String = "deeplink"
        var DATA_KEY_INTERVAL: String = "interval"
        var OPTION_KEY_RESTART_APP: String = "restart_app"
    }
}
