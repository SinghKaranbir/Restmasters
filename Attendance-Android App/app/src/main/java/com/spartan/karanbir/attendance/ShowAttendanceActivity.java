package com.spartan.karanbir.attendance;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ShowAttendanceActivity extends AppCompatActivity {
    private String mEmail, mCourseId;
    private GetAttendancesTask mGetAttendancesTask;
    private ListView mAttendancesList;
    private ArrayAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_attendance);
        if(getIntent().hasExtra("email")){
            mEmail = getIntent().getStringExtra("email");
            mCourseId = getIntent().getStringExtra("courseId");
        }
        mGetAttendancesTask = new GetAttendancesTask(mEmail,mCourseId);
        mGetAttendancesTask.execute();

        mAttendancesList = (ListView) findViewById(R.id.attendance_list);
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1);
        mAttendancesList.setAdapter(arrayAdapter);
        mAttendancesList.setEmptyView(findViewById(R.id.empty_view));
    }



    private class GetAttendancesTask extends AsyncTask<Void,Void,ArrayList<String>>{
        private ProgressDialog mProgressDialog;
        private final String  TAG = GetAttendancesTask.class.getSimpleName();
        private String email, courseId;
        private Uri uri;
        private final String AUTHORITY = "ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000";
        private final String ATTENDANCE = "attendance";
        private final String GETATTENDANCE = "getAttendance";
        private final String COURSE_ID_KEY = "course_id";
        private final String EMAIL_KEY = "email";

        public  GetAttendancesTask(String email, String courseId){
                this.email = email;
                this.courseId = courseId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(ShowAttendanceActivity.this, "Getting Attendance",
                    "Please Wait", true);
        }

        @Override
            protected ArrayList<String> doInBackground(Void... params) {
                ArrayList<String> arrayList = new ArrayList<>();
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .encodedAuthority(AUTHORITY)
                        .appendPath(ATTENDANCE)
                        .appendPath(GETATTENDANCE)
                        .appendQueryParameter(EMAIL_KEY,email)
                        .appendQueryParameter(COURSE_ID_KEY, courseId);
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


                    JSONArray attendences = jsonObject.getJSONArray("attendance");
                    for(int i =0; i<attendences.length(); i++){
                        JSONObject attendanceObj = attendences.getJSONObject(i);
                        String attendedOn = attendanceObj.getString("attended_on");
                        /*SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-ddTHH:mm:ss");
                        try {
                            Date date = format.parse(attendedOn);
                            System.out.println(date);
                            arrayList.add(date.toString());
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }*/

                        arrayList.add(attendedOn);

                    }
                    //jsonObject.toString();
                    Log.d(TAG, jsonObject.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return arrayList;
            }

            @Override
            protected void onPostExecute(ArrayList<String> attendances) {
                super.onPostExecute(attendances);
                mProgressDialog.dismiss();
                if(!attendances.isEmpty()){
                   arrayAdapter.addAll(attendances);
                }
            }




    }
}
