package com.spartan.karanbir.attendance.util;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.spartan.karanbir.attendance.R;


import java.util.ArrayList;

/**
 * Created by karanbir on 5/3/16.
 */
public class CourseAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Course> mCourses = new ArrayList<Course>();

    public CourseAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mCourses.size();
    }

    @Override
    public Object getItem(int position) {
        return mCourses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.course_list_item, parent, false);
            viewHolder.courseIdTextView = (TextView) convertView.findViewById(R.id.course_id);
            viewHolder.courseNameTextView = (TextView) convertView.findViewById(R.id.course_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.courseIdTextView.setText(mCourses.get(position).getCourseId());
        viewHolder.courseNameTextView.setText(mCourses.get(position).getCourseName());
        // Return the completed view to render on screen
        return convertView;
    }

    private static class ViewHolder {
        TextView courseIdTextView;
        TextView courseNameTextView;
    }

    public void updateEntries(ArrayList<Course> entries) {
        mCourses = entries;
        notifyDataSetChanged();
    }
}



