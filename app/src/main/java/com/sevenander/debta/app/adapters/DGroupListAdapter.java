package com.sevenander.debta.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sevenander.debta.app.R;
import com.sevenander.debta.app.pojo.DGroup;
import com.sevenander.debta.app.utils.Utils;

import java.util.List;

/**
 * Created by andrii on 06.03.15.
 */
public class DGroupListAdapter extends ArrayAdapter<DGroup> {

    private Context mContext;
    private int mLayoutResourceId;
    private List<DGroup> mDGroups;

    public DGroupListAdapter(Context context, int layoutResourceId, List<DGroup> dGroups) {
        super(context, layoutResourceId, dGroups);
        this.mContext = context;
        this.mLayoutResourceId = layoutResourceId;
        this.mDGroups = dGroups;
    }

    public static class ViewHolder {
        public final TextView tvName;
        public final TextView tvDebt;

        public ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tv_group_name);
            tvDebt = (TextView) view.findViewById(R.id.tv_group_debt);
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
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        DGroup dGroup = getItem(position);

        holder.tvName.setText(dGroup.getName());
        holder.tvDebt.setText(Utils.formatMoney(dGroup.getDebt()));

        return convertView;
    }

    public void setData(List<DGroup> dGroups) {
        this.mDGroups = dGroups;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public DGroup getItem(int position) {
        return mDGroups.get(position);
    }
}
