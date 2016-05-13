package com.spartan.karanbir.attendance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.spartan.karanbir.attendance.util.Course;
import com.spartan.karanbir.attendance.util.CourseAdapter;
import com.spartan.karanbir.attendance.util.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;



/**
 * Created by ranaf on 5/4/2016.
 */
public class HomeFragment extends Fragment {

    private BluetoothAdapter mBluetoothAdapter;
    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    private ListView mCourseList;
    private CourseAdapter listAdapter;
    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    private TextView mBluetoothStatus;
    private static String AUTHORITY = "ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000";
    private static String AUTH = "courses";
    private static String OPERATION = "getCourses";
    private ProgressDialog mProgressDialog ;
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadSendData;
    private static final int REQUEST_ENABLE_BT = 1;
    private SharedPreferences sharedPreferences;
    private String mEmail;
    private String mCourseId;
    private String payload;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, viewGroup, false);
        mCourseList = (ListView) view.findViewById(R.id.course_list);
        sharedPreferences = getActivity().getSharedPreferences(LoginActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        listAdapter = new CourseAdapter(getActivity());
        mCourseList.setAdapter(listAdapter);
        if(isNetworkConnected()) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Loading .......");
            mProgressDialog.show();
            Gson gson = new Gson();
            String json = sharedPreferences.getString("UserObject", "");
            User obj = gson.fromJson(json, User.class);
            mEmail = obj.getEmail();

            Log.d("Email", mEmail);
            GetCoursesTask courseTask = new GetCoursesTask(mEmail);
            courseTask.execute();
        } else {
            Toast.makeText(getActivity(), "No Internet Connection !!", Toast.LENGTH_SHORT).show();
        }

        mCourseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Course course  = (Course)listAdapter.getItem(position);
                mCourseId = course.getId();
                payload = mEmail+"~"+mCourseId+"~";
                Log.d("payload", payload);
                //setup();
                if(myThreadSendData !=null){
                    byte[] bytesToSend = payload.getBytes();
                    myThreadSendData.write(bytesToSend);
                }
                Toast.makeText(getActivity(), "Attendance Request Sent for: " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
            }
        });



        //mBluetoothStatus = (TextView) view.findViewById(R.id.bluetooth_status);
        Activity a = getActivity();
        if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null){
            Toast.makeText(getActivity(), "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
        }else {
            if(!mBluetoothAdapter.isEnabled()) {
                //mBluetoothStatus.setText("Status: Disabled");
            }else {
                setup();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        myThreadConnectBTdevice.cancel();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_home, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            sharedPreferences.edit().remove("UserObject").commit();
            Intent i = new Intent(getActivity(),LoginActivity.class);
            startActivity(i);
            getActivity().finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private class LogoutTask extends AsyncTask<String, Void, String> {
        private String uri;
        @Override
        protected String doInBackground(String... params) {
            String result = null;
            HttpURLConnection urlConnection = null;
            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .encodedAuthority(AUTHORITY)
                        .appendPath(AUTH)
                        .appendPath(OPERATION);
                uri = builder.build().toString();
                URL url = new URL(uri);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int status = urlConnection.getResponseCode();
                BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String webPage = "",data="";
                while ((data = bufferedReader.readLine()) != null){
                    webPage += data + "\n";
                }
                bufferedReader.close();
                if(status == 200) {
                    result = "done";
                }

            } catch (IOException e) {

            }
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            if(result.equals("done")) {
                Intent it = new Intent(getActivity(), LoginActivity.class);
                startActivity(it);
                Toast.makeText(getActivity(), "You have been Logged Out !", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private class GetCoursesTask extends AsyncTask<Void,Void,ArrayList<Course>> {
        private Uri uri = null;
        private  final String TAG = GetCoursesTask.class.getSimpleName();
        private  final String AUTHORITY = "ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000";
        private  final String COURSES = "courses";
        private  final String GETCOURSES = "getCourses";
        private String email;

        public GetCoursesTask(String email){
            this.email = email;
        }
        @Override
        protected ArrayList<Course> doInBackground(Void... params) {
            ArrayList<Course> coursesList = new ArrayList<>();
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority(AUTHORITY)
                    .appendPath(COURSES)
                    .appendPath(GETCOURSES)
                    .appendQueryParameter("email",email);
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
            mProgressDialog.dismiss();
            if(!courses.isEmpty()){
                listAdapter.updateEntries(courses);
            }
            if(!mBluetoothAdapter.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, REQUEST_ENABLE_BT);
                Toast.makeText(getActivity(), "Bluetooth Turned On", Toast.LENGTH_SHORT).show();
            }else {
                //mBluetoothStatus.setText("Status: Enabled");
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                setup();
            }else{
                Toast.makeText(getActivity(),
                        "BlueTooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startThreadConnected(BluetoothSocket socket){
        myThreadSendData = new ThreadConnected(socket);
        myThreadSendData.start();
    }
    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);
                    final String msgReceived = String.valueOf(bytes) +
                            " bytes received:\n"
                            + strReceived;

                    /*getActivity().runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            mBluetoothStatus.setText(msgReceived);
                        }});*/

                } catch (IOException e) {
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    /*getContext().runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            mBluetoothStatus.setText(msgConnectionLost);
                        }});*/
                }
            }
        }
        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void setup() {
        if(mBluetoothAdapter.isEnabled()){
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                pairedDeviceArrayList = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice device : pairedDevices) {
                    pairedDeviceArrayList.add(device);
                }
                BluetoothDevice device = pairedDeviceArrayList.get(0);
                Log.d("Device", device.toString());
                myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                myThreadConnectBTdevice.start();
            }
        }else {
            Toast.makeText(getActivity(), "Enable Bluetooth !!", Toast.LENGTH_SHORT).show();
        }
    }
    private class ThreadConnectBTdevice extends Thread {
        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;
        public ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;
            try {
                Method method;
                method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );
                bluetoothSocket = (BluetoothSocket) method.invoke(device, 1);
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
                final String eMessage = e.getMessage();
                /*getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        mBluetoothStatus.setText("something wrong bluetoothSocket.connect(): \n" + eMessage);
                        Log.d("error", eMessage);
                    }});*/
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if(success){
                final String msgconnected = "connect successful:\n"
                        + "BluetoothSocket: " + bluetoothSocket + "\n"
                        + "BluetoothDevice: " + bluetoothDevice;
                Log.d("Message: ", msgconnected);
                /*getActivity().runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        mBluetoothStatus.setText(msgconnected);
                    }});*/

                startThreadConnected(bluetoothSocket);
            }else{

            }
        }
        public void cancel() {
            Toast.makeText(getActivity(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }




}