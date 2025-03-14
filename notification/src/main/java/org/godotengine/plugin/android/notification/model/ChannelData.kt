//
// © 2024-present https://github.com/cengiz-pz
//
package org.godotengine.plugin.android.notification.model

import org.godotengine.godot.Dictionary

data class ChannelData(
    val id: String,
    val name: String,
    val description: String,
    val importance: Int,
) {
    companion object {
        const val DATA_KEY_ID = "id"
        const val DATA_KEY_NAME = "name"
        const val DATA_KEY_DESCRIPTION = "description"
        const val DATA_KEY_IMPORTANCE = "importance"

        fun from(data: Dictionary): ChannelData {
            val id = data[DATA_KEY_ID] as String
            val name = data[DATA_KEY_NAME] as String
            val description = data[DATA_KEY_DESCRIPTION] as String
            val importance = data[DATA_KEY_IMPORTANCE] as Int

            return ChannelData(id, name, description, importance)
        }
    }
}
