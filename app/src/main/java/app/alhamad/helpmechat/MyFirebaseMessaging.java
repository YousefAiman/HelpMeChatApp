package app.alhamad.helpmechat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.ExecutionException;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    NotificationManager notificationManager;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        SharedPreferences sharedPreferences = getSharedPreferences("help", Context.MODE_PRIVATE);
        String sent = remoteMessage.getData().get("sent");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            try {
                if (sharedPreferences.contains("messagingscreen")) {
                    Log.d("ttt", sharedPreferences.getString("messagingscreen", "") + "-" + sent);
                    if (!sharedPreferences.getString("messagingscreen", "").equals(sent)) {
                        sendNotification(remoteMessage);
                    }
                } else {
                    sendNotification(remoteMessage);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendNotification(RemoteMessage remoteMessage) throws ExecutionException, InterruptedException {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel();
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String username = remoteMessage.getData().get("username");
        String body = remoteMessage.getData().get("body");
        String title = remoteMessage.getData().get("title");
        String imageUrl = remoteMessage.getData().get("imageUrl");
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent newIntent = new Intent(this, MessagingActivity.class);

        newIntent.putExtra("chatterId", user);
        newIntent.putExtra("chatterName", username);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, newIntent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSound(defaultUri)
                .setContentText(body)
                .setAutoCancel(true);
        if (imageUrl != null) {
            builder.setLargeIcon(Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .submit().get());
            int i = 0;
            if (j > 0) {
                i = j;
            }
            notificationManager.notify(i, builder.build());
        } else {
            int i = 0;
            if (j > 0) {
                i = j;
            }
            notificationManager.notify(i, builder.build());
        }
    }

    public void createChannel() {

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("channel1", "notifChannel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("notifications");
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
