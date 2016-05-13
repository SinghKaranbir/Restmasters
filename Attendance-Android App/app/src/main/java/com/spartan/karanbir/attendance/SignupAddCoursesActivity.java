package com.spartan.karanbir.attendance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.spartan.karanbir.attendance.util.AddCourseAdapter;
import com.spartan.karanbir.attendance.util.Course;
import com.spartan.karanbir.attendance.util.CourseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class SignupAddCoursesActivity extends AppCompatActivity {
    private static final String TAG = SignupAddCoursesActivity.class.getSimpleName();
    private ListView mListView;
    private AddCourseAdapter mCourseAdapter;
    private String mEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra("email")){
            mEmail = getIntent().getStringExtra("email");
        }else{
            finish();
        }
        setContentView(R.layout.activity_signup_add_courses);
        mListView = (ListView) findViewById(R.id.courses);
        GetCoursesTask getCoursesTask = new GetCoursesTask();
        getCoursesTask.execute();
        mCourseAdapter = new AddCourseAdapter(this);
        mListView.setAdapter(mCourseAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_courses_signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
        if(item.getItemId() == R.id.add_courses){
            JSONObject jsonObject = new JSONObject();
            JSONArray coursesArray = new JSONArray();
            SparseBooleanArray checked = mListView.getCheckedItemPositions();
            int size = checked.size();
            for (int i = 0; i < size; i++) {
                int key = checked.keyAt(i);
                boolean value = checked.get(key);
                if (value){
                    Course course = (Course)mCourseAdapter.getItem(key);

                        JSONObject courseID = new JSONObject();
                        courseID.put("_id",course.getId());
                        coursesArray.put(courseID);
                }
            }
            jsonObject.put("email",mEmail);
            jsonObject.put("courses",coursesArray);
            Log.d(TAG,jsonObject.toString());
            AddCoursesTask addCoursesTask = new AddCoursesTask(jsonObject);
            addCoursesTask.execute();
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }




    private class GetCoursesTask extends AsyncTask<Void,Void,ArrayList<Course>> {
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
                JSONArray jsonCourses = jsonObject.getJSONArray("courses");
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


    private class AddCoursesTask extends AsyncTask<Void,Void,Boolean>{

        private ProgressDialog mProgressDialog;
        private final String AUTHORITY = "ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000";
        private final String TAG = AddCoursesTask.class.getSimpleName();
        private Uri uri;
        private static final String COURSES = "courses";
        private static final String ADDCOURSES = "addCourses";
        private JSONObject object;

        public AddCoursesTask(JSONObject object){
            this.object = object;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(SignupAddCoursesActivity.this, "Loging in",
                    "Please Wait", true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean successful = false;
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority(AUTHORITY)
                    .appendPath(COURSES)
                    .appendPath(ADDCOURSES);
            uri = builder.build();
            URL url = null;
            try {
                url = new URL(uri.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty( "Content-Type", "application/json" );
                urlConnection.setRequestProperty("Accept", "application/json");
                OutputStreamWriter wr= new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write(object.toString()); //Writes out the string to the underlying output stream as a sequence of bytes
                wr.flush(); // Flushes the data output stream.
                wr.close();
                // wrap the urlconnection in a bufferedreader
                if(urlConnection.getResponseCode() == 204){
                    successful = true;
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return  successful;
        }

        @Override
        protected void onPostExecute(Boolean successful) {
            mProgressDialog.dismiss();
            super.onPostExecute(successful);
            if(successful){
                Intent intent = new Intent(SignupAddCoursesActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
