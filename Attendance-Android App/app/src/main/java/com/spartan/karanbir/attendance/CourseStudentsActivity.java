package com.spartan.karanbir.attendance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.spartan.karanbir.attendance.util.User;
import com.spartan.karanbir.attendance.util.UserAdapter;

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

public class CourseStudentsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String mCourseId;
    private ListView mUserListView;
    private UserAdapter userAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_students);
        if(getIntent() != null){
           mCourseId = getIntent().getStringExtra("Id");
        }
        mUserListView = (ListView)  findViewById(R.id.user_list_view);
        mUserListView.setEmptyView(findViewById(R.id.empty_view));
        GetEnrolledStudentsTask getEnrolledStudentsTask = new GetEnrolledStudentsTask(mCourseId);
        getEnrolledStudentsTask.execute();
        userAdapter = new UserAdapter(this);
        mUserListView.setAdapter(userAdapter);
        mUserListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User user = (User)userAdapter.getItem(position);
        Intent i = new Intent(CourseStudentsActivity.this,ShowAttendanceActivity.class);
        i.putExtra("email",user.getEmail());
        i.putExtra("courseId",mCourseId);
        startActivity(i);
    }


    private class GetEnrolledStudentsTask extends AsyncTask<Void,Void,ArrayList<User>>{
        private ProgressDialog mProgressDialog;
        private Uri uri = null;
        private  final String TAG = GetEnrolledStudentsTask.class.getSimpleName();
        private  final String AUTHORITY = "ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000";
        private  final String COURSES = "courses";
        private  final String GETUSERS= "getUsers";
        private  final String COURSE_ID_KEY = "courseId";
        private  String courseId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(CourseStudentsActivity.this, "Getting Enrolled Students",
                    "Please Wait", true);

        }

        public GetEnrolledStudentsTask(String courseId){
            this.courseId = courseId;
        }
        @Override
        protected ArrayList<User> doInBackground(Void... params) {
            ArrayList<User> usersList = new ArrayList<>();
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority(AUTHORITY)
                    .appendPath(COURSES)
                    .appendPath(GETUSERS)
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
                JSONArray jsonCourses = jsonObject.getJSONArray("users");
                for(int i=0; i< jsonCourses.length();i++){
                    JSONObject courseObject = jsonCourses.getJSONObject(i);
                    User user  = new User(courseObject.getString("userType"),courseObject.getString("firstName"),courseObject.getString("lastName"),courseObject.getString("email"));
                    usersList.add(user);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return usersList;
        }

        @Override
        protected void onPostExecute(ArrayList<User> users) {
            super.onPostExecute(users);
            mProgressDialog.dismiss();
            if(!users.isEmpty()){
                userAdapter.updateEntries(users);
            }
        }
    }
}
