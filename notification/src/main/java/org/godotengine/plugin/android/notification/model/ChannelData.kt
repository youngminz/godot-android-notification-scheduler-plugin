//
// © 2024-present https://github.com/cengiz-pz
//
package org.godotengine.plugin.android.notification.model

import org.godotengine.godot.Dictionary

class ChannelData(private val data: Dictionary) {
    val id: String?
        get() = data[DATA_KEY_ID] as String?

    val name: String?
        get() = data[DATA_KEY_NAME] as String?

    val description: String?
        get() = data[DATA_KEY_DESCRIPTION] as String?

    val importance: Int
        get() = data[DATA_KEY_IMPORTANCE] as Int

    val isValid: Boolean
        get() = data.containsKey(DATA_KEY_ID) &&
                data.containsKey(DATA_KEY_NAME) &&
                data.containsKey(DATA_KEY_DESCRIPTION) &&
                data.containsKey(DATA_KEY_IMPORTANCE)

    companion object {
        private const val DATA_KEY_ID = "id"
        private const val DATA_KEY_NAME = "name"
        private const val DATA_KEY_DESCRIPTION = "description"
        private const val DATA_KEY_IMPORTANCE = "importance"
    }
}
