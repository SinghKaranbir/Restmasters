package com.spartan.karanbir.attendance;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.spartan.karanbir.attendance.util.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, Validator.ValidationListener {
    private static final String TAG = LoginActivity.class.getSimpleName();
    @Password(min = 4, scheme = Password.Scheme.NUMERIC, message = "Must be 4 chars and Numeric")
    private  EditText mPasswordEditText;
    @NotEmpty
    @Email
    private  EditText mEmailEditText;

    private Button mLogInButton;
    public  static final String MyPREFERENCES = "MyPrefs";
    private SharedPreferences sharedPreferences;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Validator validator;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailEditText = (EditText) findViewById(R.id.email);
        mPasswordEditText = (EditText) findViewById(R.id.password);
        mLogInButton = (Button) findViewById(R.id.login_button);
        mLogInButton.setOnClickListener(this);
        validator = new Validator(this);
        validator.setValidationListener(this);
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check type of intent filter
                if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)){
                    //Registration success
                    mProgressDialog.dismiss();
                    LoginTask loginTask = new LoginTask(mEmailEditText.getText().toString(),mPasswordEditText.getText().toString());
                    loginTask.execute();
                } else if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)){
                    //Registration error
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "GCM registration error!!!", Toast.LENGTH_LONG).show();
                } else {
                    //Tobe define
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(TAG, "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }


    private void isGooglePlayAvailable(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if(ConnectionResult.SUCCESS != resultCode) {
            //Check type of error
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                //So notification
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }
        } else {
            //Start service
            Intent intent = new Intent(this, GCMRegistrationIntentService.class);
            intent.putExtra("email",mEmailEditText.getText().toString());
            startService(intent);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.login_button){
            validator.validate();
        }
    }

    @Override
    public void onValidationSucceeded() {
        mProgressDialog = ProgressDialog.show(this, "Loging in",
                "Please Wait", true);
            //Check status of Google play service in device
            isGooglePlayAvailable();


    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }


    private class LoginTask extends AsyncTask<Void,Void,User>{

        private Uri uri = null;
        private  final String TAG = LoginTask.class.getSimpleName();
        private  final String AUTHORITY = "ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000";
        private  final String AUTH = "auth";
        private  final String LOGIN= "login";
        private  String email;
        private String password;

        public LoginTask(String email, String password){
            this.email = email;
            this.password = password;
        }


        @Override
        protected User doInBackground(Void... params) {
            User user = null;
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority(AUTHORITY)
                    .appendPath(AUTH)
                    .appendPath(LOGIN)
                    .encodedQuery("email="+ email)
                    .appendQueryParameter("password", password);

            uri = builder.build();
            URL url = null;
            try {
                url = new URL(uri.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                // wrap the urlconnection in a bufferedreader
                BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String webPage = "",data="";

                while ((data = bufferedReader.readLine()) != null){
                    webPage += data + "\n";
                }
                bufferedReader.close();
                JSONObject jsonObject = new JSONObject(webPage);
                JSONObject jsonUser = jsonObject.getJSONObject("user");
                user  = new User(jsonUser.getString("userType"),jsonUser.getString("firstName"),jsonUser.getString("lastName"),jsonUser.getString("email"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            if(user != null){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                String json = gson.toJson(user);
                editor.putString("UserObject", json);
                editor.apply();
                if(user.getUserType().equals("1")) {
                    Intent it = new Intent(LoginActivity.this, InstructorActivity.class);
                    startActivity(it);
                    finish();
                }else{
                    Intent it = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(it);
                    finish();
                }
            }

        }
    }
}
