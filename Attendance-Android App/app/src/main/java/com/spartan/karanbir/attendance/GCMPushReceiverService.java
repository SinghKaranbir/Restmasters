package com.spartan.karanbir.attendance;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.spartan.karanbir.attendance.util.User;

/**
 * Created by karanbir on 5/9/16.
 */
public class GCMPushReceiverService extends GcmListenerService {
    private static final String TAG = GCMRegistrationIntentService.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG,data.toString());

        String message = data.getString("message");
        String title = data.getString("title");
        Log.d("TAG", message);
        sendNotification(message,title);
    }
    private void sendNotification(String message,String title) {
        Intent intent = null;
        sharedPreferences = getSharedPreferences(LoginActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        if(!sharedPreferences.getString("UserObject","").equals("")){
            Gson gson = new Gson();
            String json = sharedPreferences.getString("UserObject", "");
            User obj = gson.fromJson(json, User.class);
            if(obj.getUserType().equals("1")){
                intent = new Intent(this,InstructorActivity.class);


            }else{
                intent = new Intent(this,DashboardActivity.class);
            }

        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = 0;//Your request code
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        //Setup notification
        //Sound
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Build notification
        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("My GCM message :X:X")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build()); //0 = ID of notification
    }
}