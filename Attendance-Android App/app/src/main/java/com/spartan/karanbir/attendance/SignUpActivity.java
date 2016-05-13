package com.spartan.karanbir.attendance;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.spartan.karanbir.attendance.util.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class SignUpActivity extends AppCompatActivity implements Validator.ValidationListener, View.OnClickListener {

    private String[] SPINNERCATEGORY = {"Student", "Teacher"};
    private Button mSignUpButton;
    private Validator mValidator;
    private String FIRSTNAME, LASTNAME, EMAIL, PASSWORD, REPASSWORD, USERTYPE = null;
    private Spinner mCategorySpinner;
    private SignUpTask mSignupTask;

    @NotEmpty
    private EditText mFirstName, mLastName;
    @Password(min = 4, scheme = Password.Scheme.NUMERIC, message = "Must be 4 characters and Numeric")
    private EditText mPassword, mRePassword;
    @NotEmpty
    @Email
    private EditText mEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
        //References
        mCategorySpinner = (Spinner) findViewById(R.id.category_spinner);
        mFirstName = (EditText) findViewById(R.id.firstName_editText);
        mLastName = (EditText) findViewById(R.id.lastName_editText);
        mEmail = (EditText) findViewById(R.id.email_editText);
        mPassword = (EditText) findViewById(R.id.password_editText);
        mRePassword = (EditText)findViewById(R.id.rePassword_editText);
        mSignUpButton = (Button) findViewById(R.id.signup_button);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,SPINNERCATEGORY);
        mCategorySpinner.setAdapter(adapter);
        mSignUpButton.setOnClickListener(this);

    }
    protected void startSignupTask() {
        FIRSTNAME = mFirstName.getText().toString();
        LASTNAME = mLastName.getText().toString();
        EMAIL = mEmail.getText().toString();
        PASSWORD = mPassword.getText().toString();
        USERTYPE = mCategorySpinner.getSelectedItem().toString();
        if(USERTYPE.equals(SPINNERCATEGORY[0])) {
            Log.d("Category", SPINNERCATEGORY[0]);
            //USERTYPE is student
            USERTYPE = "0";
        }else {
            Log.d("Category", SPINNERCATEGORY[1]);
            //USERTYPE is teacher
            USERTYPE = "1";
        }
        mSignupTask = new SignUpTask();
        mSignupTask.execute(new String[]{FIRSTNAME,LASTNAME,EMAIL,PASSWORD,USERTYPE});
    }


    @Override
    public void onValidationSucceeded() {
        startSignupTask();
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

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.signup_button){
            mValidator.validate();
        }

    }



    private class SignUpTask extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog mProgressDialog;
        private final String TAG = SignUpTask.class.getSimpleName();
        private Uri uri;
        private Boolean successful = false;
        private final String AUTHORITY = "ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000";
        private final String AUTH = "auth";
        private final String OPERATION = "register";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(SignUpActivity.this, "Loging in",
                    "Please Wait", true);
        }

        @Override
        protected Boolean doInBackground(String ...params) {
                Log.d(TAG,"doInBackground");
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .encodedAuthority(AUTHORITY)
                        .appendPath(AUTH)
                        .appendPath(OPERATION);
                uri = builder.build();
                URL url = null;
                try {
                    url = new URL(uri.toString());
                    Log.d(TAG, url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    Uri.Builder queryBuilder = new Uri.Builder()
                            .appendQueryParameter("email", params[2])
                            .appendQueryParameter("firstName", params[0])
                            .appendQueryParameter("lastName", params[1])
                            .appendQueryParameter("password", params[3])
                            .appendQueryParameter("userType", params[4]);
                    String query = queryBuilder.build().getEncodedQuery();
                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    Log.d(TAG,Integer.toString(urlConnection.getResponseCode()));
                    if (urlConnection.getResponseCode() == 201) {
                        successful = true;
                    } else {
                        successful = false;
                        Toast.makeText(SignUpActivity.this, "Unable to Register", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {

                }
                return successful;
        }
        @Override
        protected void onPostExecute(Boolean successful) {
            mProgressDialog.dismiss();
            Log.d(TAG,"onPost");
            if(successful) {
                if(USERTYPE.equals("0")){
                    Intent it = new Intent(SignUpActivity.this,SignupAddCoursesActivity.class);
                    it.putExtra("email",EMAIL);
                    startActivity(it);
                    finish();
                }
                if(USERTYPE.equals("1")){
                    Intent it = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(it);
                    finish();
                }

            }
        }

    }
}
