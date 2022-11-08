package com.todaliveryph.todaliverymarketdeliveryapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.OrderDetailsSellerActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.OrderDetailsUsersActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.RiderQueue;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID"; //required for andropid 0 above
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        //all notif will be receive here

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //get data from notif
        String notificationType = message.getData().get("notificationType");
        if (notificationType.equals("NewOrder")) {
            String buyerUid = message.getData().get("buyerUid");
            String sellerUid = message.getData().get("sellerUid");
            String orderId = message.getData().get("orderId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationDescription = message.getData().get("notificationDescription");

            if (firebaseUser != null && firebaseAuth.getUid().equals(sellerUid)) {
                //user is signed in and in same user which notif is sent
                showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationDescription, notificationType);
            }
        }
        if (notificationType.equals("OrderStatusChanged")) {
                String buyerUid = message.getData().get("buyerUid");
                String sellerUid = message.getData().get("sellerUid");
                String orderId = message.getData().get("orderId");
                String notificationTitle = message.getData().get("notificationTitle");
                String notificationDescription = message.getData().get("notificationMessage"); // or known as notif message

                if (firebaseUser != null && firebaseAuth.getUid().equals(buyerUid)) {
                    //user is signed in and in same user which notif is sent
                    showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationDescription, notificationType);
                }
        }
        if (notificationType.equals("Chat")) {
            String buyerUid = message.getData().get("buyerUid");
            String sellerUid = message.getData().get("sellerUid");
            String orderId = message.getData().get("orderId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationDescription = message.getData().get("notificationMessage"); // or known as notif message

            if (firebaseUser != null && firebaseAuth.getUid().equals(buyerUid)) {
                //user is signed in and in same user which notif is sent
                showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationDescription, notificationType);
            }
        }
        if (notificationType.equals("Rider")) {
            String riderUid = message.getData().get("riderUid");
            String sellerUid = message.getData().get("sellerUid");
            String orderId = message.getData().get("orderId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationDescription = message.getData().get("notificationDescription");

            if (firebaseUser != null && firebaseAuth.getUid().equals(riderUid)) {
                //user is signed in and in same user which notif is sent
                showNotification(orderId, sellerUid, riderUid, notificationTitle, notificationDescription, notificationType);
            }
        }
    }

    private void  showNotification (String orderId, String sellerUid, String buyerUid, String notificationTitle, String notificationDescription, String notificationType){

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //setup notif id
        int notificationID = new Random().nextInt(3000);
        //check android version if it is oreo / above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setupNotificationChannel(notificationManager);
        }
        //handle notif clicks, start order activity
        Intent intent = null;
        if (notificationType.equals("NewOrder")){
            //open order details seller
            intent = new Intent(this, OrderDetailsSellerActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderBy", buyerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        }else if(notificationType.equals("OrderStatusChanged")){
            //open order details user
            intent = new Intent(this, OrderDetailsUsersActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderTo", sellerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        else if(notificationType.equals("Chat")){
            intent = new Intent(this, MessagesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
            else if(notificationType.equals("Rider")){
            intent = new Intent(this, RiderQueue.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //Large Icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.todalogo_icon);
        //sound when notif comes
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.todalogo_icon)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setSound(notificationSoundUri)
                .setAutoCancel(true) //cancel / dismissed when clicked
                .setContentIntent(pendingIntent); // add intent

        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager notificationManager) {

        CharSequence channelName = "Some Sample Text";
        String channelDescription = "Channel Description here";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        if (notificationManager != null){
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


}//closing public class
