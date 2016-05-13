package com.spartan.karanbir.attendance;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;
import com.spartan.karanbir.attendance.util.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by karanbir on 5/9/16.
 */
public class GCMRegistrationIntentService extends IntentService {

    private static final String TAG = GCMRegistrationIntentService.class.getSimpleName();
    private String mEmail;
    private SharedPreferences sharedPreferences;
    public static final String REGISTRATION_SUCCESS = "com.spartan.karanbir.attendance.registrationsuccess";
    public static final String REGISTRATION_ERROR = "com.spartan.karanbir.attendance.registrationerror";
    public GCMRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"onHandleIntent");
        Intent registrationComplete = null;
        sharedPreferences = getSharedPreferences(LoginActivity.MyPREFERENCES, Context.MODE_PRIVATE);

        if(intent.hasExtra("email")){
            mEmail = intent.getStringExtra("email");
        }else{

            Gson gson = new Gson();
            String json = sharedPreferences.getString("UserObject", "");
            User obj = gson.fromJson(json, User.class);
            mEmail = obj.getEmail();
        }


        String token = null;
        try{
            InstanceID instanceID = InstanceID.getInstance(this);
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendTokenToServer(mEmail,token);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("token",token);
            editor.apply();
            registrationComplete = new Intent(REGISTRATION_SUCCESS);

        }catch (Exception e){
            Log.w("GCMRegIntentService", "Registration error");
            registrationComplete = new Intent(REGISTRATION_ERROR);
        }

        //Send broadcast
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);

    }





    private void sendTokenToServer(String email,String token){
        Uri uri;
        final String AUTHORITY = "ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000";
        final String AUTH = "auth";
        final String ADDTOKEN = "addToken";
        Log.d(TAG,email);
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority(AUTHORITY)
                    .appendPath(AUTH)
                    .appendPath(ADDTOKEN);
            uri = builder.build();
            URL url = null;
            try {
                url = new URL(uri.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setDoOutput(true);
                Uri.Builder queryBuilder = new Uri.Builder()
                        .appendQueryParameter("email",email)
                        .appendQueryParameter("regId", token);

                String query = queryBuilder.build().getEncodedQuery();
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();

                // wrap the urlconnection in a bufferedreader
                BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String webPage = "",data="";

                while ((data = bufferedReader.readLine()) != null){
                    webPage += data + "\n";
                }
                bufferedReader.close();
                if(urlConnection.getResponseCode() == 204){
                    Log.d(TAG,"OK");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


    }
}
