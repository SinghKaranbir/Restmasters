package com.spartan.karanbir.attendance.util;

/**
 * Created by karanbir on 5/11/16.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;



/**
 * Created by karanbir on 5/3/16.
 */
public class AddCourseAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Course> mCourses = new ArrayList<Course>();

    public AddCourseAdapter(Context context) {
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
            convertView = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            viewHolder.courseNameTextView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.courseNameTextView.setText(mCourses.get(position).getCourseName());
        // Return the completed view to render on screen
        return convertView;
    }

    private static class ViewHolder {
        TextView courseNameTextView;
    }

    public void updateEntries(ArrayList<Course> entries) {
        mCourses = entries;
        notifyDataSetChanged();
    }
}



