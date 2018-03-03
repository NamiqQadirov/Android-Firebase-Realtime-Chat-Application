package com.example.namiq.egisterpp.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.example.namiq.egisterpp.R;
import com.example.namiq.egisterpp.activities.MainActivity;
import com.example.namiq.egisterpp.dbo.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BackgroundService extends Service {

    private DatabaseReference mUserRefDatabase;
    private boolean curStatus = false;
    private String username;
    private Bundle extras;
    ArrayList<String> unReadedList = new ArrayList<>();
    NotificationManager mNotificationManager;

    public BackgroundService() {
    }

    ValueEventListener listener = new ValueEventListener() { //attach listener

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) { //something changed!

            if (curStatus) {
                unReadedList.clear();
                long count=0;
                String name="";
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    name = messageSnapshot.getKey();
                    count = messageSnapshot.getChildrenCount();
                    String messageForList = name + " Unreaded=" + count;
                    unReadedList.add(messageForList);
                    Toast.makeText(getApplicationContext(), messageForList, Toast.LENGTH_SHORT).show();
                }
                mNotificationManager.cancel(55);
                if(unReadedList.size()==1){
                    if(count>1){
                        displayNotificationOneMessage(name+" Count="+count);
                    }else{
                        displayNotificationOneMessage(name);
                    }

                }else{
                    displayNotification(unReadedList);
                }
            } else {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String name = messageSnapshot.getKey();
                    long count = messageSnapshot.getChildrenCount();
                    String messageForList = name + " Unreaded=" + count;
                    unReadedList.add(messageForList);
                    Toast.makeText(getApplicationContext(), messageForList, Toast.LENGTH_SHORT).show();
                }
                curStatus = true;
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) { //update UI here if error occurred.

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        extras = intent.getExtras();
        username = extras.getString("username");
        Toast.makeText(getApplicationContext(), username, Toast.LENGTH_SHORT).show();
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference().child("unreaded").child(username);
        mUserRefDatabase.addValueEventListener(listener);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Toast.makeText(this, "The new Service was Created", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // For time consuming an long tasks you can launch a new thread here...
        Toast.makeText(this, " Service Started", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        mNotificationManager.cancel(55);
        mUserRefDatabase.removeEventListener(listener);
        curStatus = false;

    }


    protected void displayNotification(List unReadedList) {


   /* Invoking the default notification service */
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setContentTitle("New Message");
        mBuilder.setContentText("You've received new message.");
        mBuilder.setTicker("New message from Chat");
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

   /* Add Big View Specific Configuration */
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        // Sets a title for the Inbox style big view
        inboxStyle.setBigContentTitle("New Messages");

        // Moves events into the big view
        for (Object s : unReadedList) {
            inboxStyle.addLine(s.toString());
        }

        mBuilder.setStyle(inboxStyle);

   /* Creates an explicit intent for an Activity in your app */
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

   /* Adds the Intent that starts the Activity to the top of the stack */
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);


   /* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(55, mBuilder.build());
    }

    protected void displayNotificationOneMessage(String name) {


   /* Invoking the default notification service */
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("New Message from name");
        mBuilder.setContentText("Message from " + name);
        mBuilder.setTicker("New Message from " + name);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

   /* Creates an explicit intent for an Activity in your app */
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

   /* Adds the Intent that starts the Activity to the top of the stack */
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

   /* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(55, mBuilder.build());
    }
}