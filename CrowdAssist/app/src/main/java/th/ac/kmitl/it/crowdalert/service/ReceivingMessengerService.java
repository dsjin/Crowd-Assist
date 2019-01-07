package th.ac.kmitl.it.crowdalert.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import th.ac.kmitl.it.crowdalert.R;

public class ReceivingMessengerService extends FirebaseMessagingService {
    public static final String REQUEST_ACCEPT = "REQUEST_ACCEPT";
    private SharedPreferences SP_REQUEST;
    public ReceivingMessengerService() {
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        if(data.get("verify") != null) {
            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getBaseContext());
            Intent intent = new Intent(REQUEST_ACCEPT);
            intent.putExtra("verify", true);
            broadcaster.sendBroadcast(intent);
        }else if (data.get("request") != null){
            SP_REQUEST = getSharedPreferences("request_information", Context.MODE_PRIVATE);
            if (SP_REQUEST.getString("mode", null) != null){
                return;
            }
            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getBaseContext());
            Intent intent = new Intent(REQUEST_ACCEPT);
            intent.putExtra("request", true);
            intent.putExtra("request_uid", data.get("request_uid"));
            broadcaster.sendBroadcast(intent);
        }
        if (!(FirebaseInstanceId.getInstance().getToken().equals(data.get("token")))){
            sendNotification(notification, data);
        }
    }

    private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_launche_logo_black);

        //Intent intent = new Intent(this, MainActivity.class);
        Intent intent = new Intent();
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(notification.getTitle())
                .setLargeIcon(icon)
                .setColor(Color.RED)
                .setSmallIcon(R.drawable.ic_stat_launche_logo_black);

        notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        notificationBuilder.setLights(Color.RED, 1000, 300);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d("Background Task Removed", "onTaskRemoved: "+rootIntent.getDataString());
    }
}
