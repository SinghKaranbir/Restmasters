package com.spartan.karanbir.attendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.spartan.karanbir.attendance.util.User;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mLoginButton;
    private Button mSignUpButton;
    private SharedPreferences sharedpreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedpreferences = getSharedPreferences(LoginActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        if(!sharedpreferences.getString("UserObject","").equals("")){
            Gson gson = new Gson();
            String json = sharedpreferences.getString("UserObject", "");
            User obj = gson.fromJson(json, User.class);
            if(obj.getUserType().equals("1")){
                Intent i = new Intent(this,InstructorActivity.class);
                startActivity(i);
                finish();
            }else{
                Intent i = new Intent(this,DashboardActivity.class);
                startActivity(i);
                finish();
            }

        }
        mLoginButton = (Button) findViewById(R.id.login);
        mSignUpButton = (Button) findViewById(R.id.signup);
        mSignUpButton.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()){
            case R.id.login:
                i = new Intent(this,LoginActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.signup:
                i = new Intent(this, SignUpActivity.class);
                startActivity(i);
                finish();
                break;
        }
    }
}
