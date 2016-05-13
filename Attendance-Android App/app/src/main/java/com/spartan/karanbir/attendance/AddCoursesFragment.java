package com.spartan.karanbir.attendance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.spartan.karanbir.attendance.util.AddCourseAdapter;
import com.spartan.karanbir.attendance.util.Course;
import com.spartan.karanbir.attendance.util.User;

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



/**
 * Created by ranaf on 5/4/2016.
 */
public class AddCoursesFragment extends Fragment {

    private ListView mCoursesListView;
    private static String AUTHORITY = "ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000";
    private static String AUTH = "courses";
    private static String OPERATION = "getCourses";
    private ProgressDialog mProgressDialog ;
    private AddCourseAdapter courseListAdapter;
    private SharedPreferences sharedPreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_courses, viewGroup, false);
        Activity a = getActivity();
        sharedPreferences = a.getSharedPreferences(LoginActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mCoursesListView = (ListView) view.findViewById(R.id.course_list);
        mCoursesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        courseListAdapter = new AddCourseAdapter(getActivity());
        mCoursesListView.setAdapter(courseListAdapter);
        GetCoursesTask getCourse = new GetCoursesTask();
        getCourse.execute();
        return view;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_add_courses, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if(item.getItemId() == R.id.action_add_courses){
                JSONObject jsonObject = new JSONObject();
                JSONArray coursesArray = new JSONArray();
                SparseBooleanArray checked = mCoursesListView.getCheckedItemPositions();
                int size = checked.size();
                for (int i = 0; i < size; i++) {
                    int key = checked.keyAt(i);
                    boolean value = checked.get(key);
                    if (value){
                        Course course = (Course)courseListAdapter.getItem(key);

                        JSONObject courseID = new JSONObject();
                        courseID.put("_id",course.getId());
                        coursesArray.put(courseID);
                    }
                }
                Gson gson = new Gson();
                String json = sharedPreferences.getString("UserObject", "");
                User obj = gson.fromJson(json, User.class);
                jsonObject.put("email",obj.getEmail());
                jsonObject.put("courses",coursesArray);
                //Log.d(TAG,jsonObject.toString());
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
                courseListAdapter.updateEntries(courses);
            }
        }
    }


    private class AddCoursesTask extends AsyncTask<Void,Void,Boolean>{
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
            super.onPostExecute(successful);
            if(successful){
                Toast.makeText(getActivity(),"You have added Courses",Toast.LENGTH_LONG).show();
            }
        }
    }
}


