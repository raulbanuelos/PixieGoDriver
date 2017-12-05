package com.pixielab.pixiegodriver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pixielab.pixiegodriver.model.PixieNotificacion;
import com.pixielab.pixiegodriver.view.DriverMapActivity;

/**
 * Created by raulb on 25/11/2017.
 */

public class PixieFirebaseMessaginService extends FirebaseMessagingService {

    private static final String TAG = "PixieMessaginService";
    private static final String KEY_DESCOUNT = "descount_key";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        PixieNotificacion pixieNotificacion = new PixieNotificacion();
        pixieNotificacion.setId(remoteMessage.getFrom());
        pixieNotificacion.setTitle(remoteMessage.getNotification().getTitle());
        pixieNotificacion.setDescription(remoteMessage.getNotification().getBody());
        pixieNotificacion.setDescount(remoteMessage.getData().get(KEY_DESCOUNT));
        
        showNotification(pixieNotificacion);
    }

    private  void showNotification(PixieNotificacion pixieNotificacion){
        Intent intent = new Intent(this, DriverMapActivity.class);
        intent.putExtra(KEY_DESCOUNT, pixieNotificacion.getDescount());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defalultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.conductor)
                .setContentTitle(pixieNotificacion.getTitle())
                .setContentText(pixieNotificacion.getDescription())
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(defalultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());


    }
}
