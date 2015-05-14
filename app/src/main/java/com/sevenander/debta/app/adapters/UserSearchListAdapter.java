package com.sevenander.debta.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.sevenander.debta.app.R;
import com.sevenander.debta.app.filters.UserSearchFilter;
import com.sevenander.debta.app.pojo.User;

import java.util.List;

/**
 * Created by andrii on 03.03.15.
 */
public class UserSearchListAdapter extends ArrayAdapter<User> {

    private Context mContext;
    private int mLayoutResourceId;
    private List<User> mUsers;
    private Filter mFilter;

    public UserSearchListAdapter(Context context, int layoutResourceId, List<User> users) {
        super(context, layoutResourceId, users);
        this.mContext = context;
        this.mLayoutResourceId = layoutResourceId;
        this.mUsers = users;
        this.mFilter = new UserSearchFilter(this, this.mUsers);
    }

    public static class ViewHolder {
        public final TextView tvName;
        public final TextView tvUserName;

        public ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tv_user_item_name);
            tvUserName = (TextView) view.findViewById(R.id.tv_item_username);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, null, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = getItem(position);

        holder.tvName.setText(user.getFName() + " " + user.getLName());
        holder.tvUserName.setText(user.getUsername());

        return convertView;
    }

    public void setData(List<User> users) {
        this.mUsers = users;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public User getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public Filter getFilter() {
        if (mFilter != null)
            mFilter = new UserSearchFilter(this, mUsers);
        return mFilter;
    }
}
