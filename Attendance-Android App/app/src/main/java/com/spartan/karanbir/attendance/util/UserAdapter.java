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
public class UserAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<User> mUsers = new ArrayList<>();

    public UserAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return mUsers.get(position);
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
            convertView = inflater.inflate(R.layout.user_list_item, parent, false);
            viewHolder.firstNameTextView = (TextView) convertView.findViewById(R.id.first_name);
            viewHolder.emailTextView = (TextView) convertView.findViewById(R.id.email);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.firstNameTextView.setText(mUsers.get(position).getFirstName());
        viewHolder.emailTextView.setText(mUsers.get(position).getEmail());
        // Return the completed view to render on screen
        return convertView;
    }

    private static class ViewHolder {
        TextView firstNameTextView;
        TextView emailTextView;
    }

    public void updateEntries(ArrayList<User> entries) {
        mUsers = entries;
        notifyDataSetChanged();
    }
}



