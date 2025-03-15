//
// © 2024-present https://github.com/cengiz-pz
//
package org.godotengine.plugin.android.notification

import android.Manifest
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
            Log.e(LOG_TAG, "onReceive():: Received intent is null. Unable to generate notification.")
            return
        }
        if (!intent.hasExtra(NotificationData.DATA_KEY_ID)) {
            Log.e(LOG_TAG, "onReceive():: ${NotificationData.DATA_KEY_ID} extra not found in intent. Unable to generate notification.")
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(LOG_TAG, "onReceive():: unable to process notification as current SDK is ${Build.VERSION.SDK_INT} and required SDK is ${Build.VERSION_CODES.M}")
            return
        }

        val notificationData = NotificationData.from(intent)
        Log.i(LOG_TAG, "onReceive():: received notification id:'${notificationData.id}' - channel id:${notificationData.channelId} - title:'${notificationData.title}' - content:'${notificationData.content}' - small icon name:'${notificationData.smallIconName}")

        val notificationClickIntent = Intent(context, ResultActivity::class.java).apply {
            putExtras(intent)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        val clickPendingIntent = PendingIntent.getActivity(context, 0, notificationClickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationDismissIntent = Intent(context, CancelNotificationReceiver::class.java).apply {
            putExtras(intent)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(context, 0, notificationDismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(context, notificationData.channelId)
            .setSmallIcon(
                context.resources.getIdentifier(
                    notificationData.smallIconName,
                    ICON_RESOURCE_TYPE,
                    context.packageName
                )
            )
            /* TODO: large icon not working. It needs to be tested again in future versions.
            .setLargeIcon(BitmapFactory.decodeResource(r, r.getIdentifier(LARGE_ICON_LABEL, LARGE_ICON_RESOURCE_TYPE, context.getPackageName())))
            .setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(BitmapFactory.decodeResource(r, r.getIdentifier(LARGE_ICON_LABEL, LARGE_ICON_RESOURCE_TYPE, context.getPackageName())))
                    .bigLargeIcon(BitmapFactory.decodeResource(r, r.getIdentifier(LARGE_ICON_LABEL, LARGE_ICON_RESOURCE_TYPE, context.getPackageName()))))
             */
            .setContentTitle(notificationData.title)
            .setContentText(notificationData.content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(clickPendingIntent)
            .setDeleteIntent(dismissPendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val notification = notificationBuilder.build()
            NotificationManagerCompat.from(context).notify(notificationData.id, notification)
        } else {
            Log.w(LOG_TAG, "onReceive():: unable to process notification as ${Manifest.permission.POST_NOTIFICATIONS} permission is not granted")
        }
    }

    companion object {
        private val LOG_TAG = "godot::${NotificationReceiver::class.java.simpleName}"
        private const val ICON_RESOURCE_TYPE = "drawable"
    }
}
