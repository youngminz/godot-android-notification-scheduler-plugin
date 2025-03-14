//
// © 2024-present https://github.com/cengiz-pz
//
package org.godotengine.plugin.android.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.godotengine.plugin.android.notification.model.NotificationData

class NotificationReceiver : BroadcastReceiver() {
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
            val channelId = intent.getStringExtra(NotificationData.Companion.DATA_KEY_CHANNEL_ID)
            val title = intent.getStringExtra(NotificationData.Companion.DATA_KEY_TITLE)
            val content = intent.getStringExtra(NotificationData.Companion.DATA_KEY_CONTENT)
            val smallIconName =
                intent.getStringExtra(NotificationData.Companion.DATA_KEY_SMALL_ICON_NAME)

            val notificationActionIntent = Intent(
                context,
                ResultActivity::class.java
            )
            notificationActionIntent.putExtra(
                NotificationData.Companion.DATA_KEY_ID,
                notificationId
            )

            if (intent.hasExtra(NotificationData.Companion.DATA_KEY_DEEPLINK)) {
                notificationActionIntent.putExtra(
                    NotificationData.Companion.DATA_KEY_DEEPLINK,
                    intent.getStringExtra(NotificationData.Companion.DATA_KEY_DEEPLINK)
                )
            }

            if (intent.hasExtra(NotificationData.Companion.OPTION_KEY_RESTART_APP)) {
                notificationActionIntent.putExtra(
                    NotificationData.Companion.OPTION_KEY_RESTART_APP,
                    true
                )
            }

            notificationActionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)

            val onCancelIntent = Intent(
                context,
                CancelNotificationReceiver::class.java
            )
            onCancelIntent.putExtra(NotificationData.Companion.DATA_KEY_ID, notificationId)
            val onDismissPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                onCancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            Log.i(
                LOG_TAG, String.format(
                    "%s():: received notification id:'%d' - channel id:%s - title:'%s' - content:'%s' - small icon name:'%s",
                    "onReceive", notificationId, channelId, title, content, smallIconName
                )
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    notificationActionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val resources = context.resources
                @SuppressLint("DiscouragedApi") val notificationBuilder =
                    NotificationCompat.Builder(
                        context,
                        channelId!!
                    )
                        .setSmallIcon(
                            resources.getIdentifier(
                                smallIconName,
                                ICON_RESOURCE_TYPE,
                                context.packageName
                            )
                        ) /* TODO: large icon not working. It needs to be tested again in future versions.
						.setLargeIcon(BitmapFactory.decodeResource(r, r.getIdentifier(LARGE_ICON_LABEL, LARGE_ICON_RESOURCE_TYPE, context.getPackageName())))
						.setStyle(new NotificationCompat.BigPictureStyle()
								.bigPicture(BitmapFactory.decodeResource(r, r.getIdentifier(LARGE_ICON_LABEL, LARGE_ICON_RESOURCE_TYPE, context.getPackageName())))
								.bigLargeIcon(BitmapFactory.decodeResource(r, r.getIdentifier(LARGE_ICON_LABEL, LARGE_ICON_RESOURCE_TYPE, context.getPackageName()))))
						 */
                        .setContentTitle(title)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(pendingIntent)
                        .setDeleteIntent(onDismissPendingIntent)
                        .setAutoCancel(true)

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val notification = notificationBuilder.build()
                    NotificationManagerCompat.from(context).notify(notificationId, notification)
                } else {
                    Log.w(
                        LOG_TAG, String.format(
                            "%s():: unable to process notification as %s permission is not granted",
                            "onReceive", Manifest.permission.POST_NOTIFICATIONS
                        )
                    )
                }
            } else {
                Log.w(
                    LOG_TAG, String.format(
                        "%s():: unable to process notification as current SDK is %d and required SDK is %d",
                        "onReceive", Build.VERSION.SDK_INT, Build.VERSION_CODES.M
                    )
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
        private val LOG_TAG = "godot::" + NotificationReceiver::class.java.simpleName

        private const val ICON_RESOURCE_TYPE = "drawable"
    }
}
