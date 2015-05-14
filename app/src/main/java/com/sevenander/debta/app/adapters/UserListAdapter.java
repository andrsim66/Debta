package com.sevenander.debta.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sevenander.debta.app.R;
import com.sevenander.debta.app.pojo.User;
import com.sevenander.debta.app.utils.Utils;

import java.util.List;

/**
 * Created by andrii on 06.03.15.
 */
public class UserListAdapter extends ArrayAdapter<User> {

    private Context mContext;
    private int mLayoutResourceId;
    private List<User> mUsers;

    public UserListAdapter(Context context, int layoutResourceId, List<User> users) {
        super(context, layoutResourceId, users);
        this.mContext = context;
        this.mLayoutResourceId = layoutResourceId;
        this.mUsers = users;
    }

    public static class ViewHolder {
        public final TextView tvName;
        public final TextView tvDebt;

        public ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tv_user_name);
            tvDebt = (TextView) view.findViewById(R.id.tv_user_debt);
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
        holder.tvDebt.setText(Utils.formatMoney(user.getDebt()));

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
    public User getItem(int position) {
        return mUsers.get(position);
    }
}
