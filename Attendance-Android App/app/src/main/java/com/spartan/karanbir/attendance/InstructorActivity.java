package com.spartan.karanbir.attendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.ListView;

import com.spartan.karanbir.attendance.util.Course;
import com.spartan.karanbir.attendance.util.CourseAdapter;

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


public class InstructorActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = InstructorActivity.class.getSimpleName();
    private ListView mListView;
    private SharedPreferences sharedPreferences;
    private CourseAdapter mCourseAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        sharedPreferences = getSharedPreferences(LoginActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        setContentView(R.layout.activity_instructor);
        mListView = (ListView) findViewById(R.id.courses);
        GetCoursesTask getCoursesTask = new GetCoursesTask();
        getCoursesTask.execute();
        mCourseAdapter = new CourseAdapter(this);
        mListView.setAdapter(mCourseAdapter);
        mListView.setOnItemClickListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_instructor, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signout:
                sharedPreferences.edit().remove("UserObject").commit();
                Intent i = new Intent(InstructorActivity.this,LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Course course = (Course) mCourseAdapter.getItem(position);
        Intent intent = new Intent(this,CourseStudentsActivity.class);
        intent.putExtra("Id",course.getId());
        startActivity(intent);
    }


    private class GetCoursesTask extends AsyncTask<Void,Void,ArrayList<Course>>{
        private Uri uri = null;
        private  final String TAG = GetCoursesTask.class.getSimpleName();
        private  final String AUTHORITY = "ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000";
        private  final String COURSES = "courses";
        private  final String GETCOURSES = "getCourses";
        @Override
        protected ArrayList<Course> doInBackground(Void... params) {
            ArrayList<Course> coursesList = new ArrayList<>();
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority(AUTHORITY)
                    .appendPath(COURSES)
                    .appendPath(GETCOURSES);
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
                JSONArray  jsonCourses = jsonObject.getJSONArray("courses");
                for(int i=0; i< jsonCourses.length();i++){
                    JSONObject courseObject = jsonCourses.getJSONObject(i);
                    Course course = new Course(courseObject.getString("_id"),courseObject.getString("courseName"),courseObject.getString("courseId"));
                    coursesList.add(course);
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
            return coursesList;
        }

        @Override
        protected void onPostExecute(ArrayList<Course> courses) {
            super.onPostExecute(courses);
            if(!courses.isEmpty()){
                mCourseAdapter.updateEntries(courses);
            }
        }
    }
}
