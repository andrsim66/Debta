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
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.utils.Utils;

import java.util.List;

/**
 * Created by andrii on 07.03.15.
 */
public class DCheckListAdapter extends ArrayAdapter<DCheck> {

    private Context mContext;
    private int mLayoutResourceId;
    private List<DCheck> mDChecks;

    public DCheckListAdapter(Context context, int layoutResourceId, List<DCheck> dChecks) {
        super(context, layoutResourceId, dChecks);
        this.mContext = context;
        this.mLayoutResourceId = layoutResourceId;
        this.mDChecks = dChecks;
    }

    public static class ViewHolder {
        public final ImageView ivIcon;
        public final TextView tvName;
        public final TextView tvDebt;

        public ViewHolder(View view) {
            ivIcon = (ImageView) view.findViewById(R.id.iv_dgroup_icon);
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
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DCheck dCheck = getItem(position);

        holder.ivIcon.setImageDrawable(mContext.getResources()
                .getDrawable(R.drawable.ic_receipt_grey600_36dp));
        holder.tvName.setText(Utils.formatDate(dCheck.getDate()));
        holder.tvDebt.setText(Utils.formatMoney(dCheck.getDebt()));

        if (dCheck.isConfirmed()) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.white_alpha));
        } else {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.blue_grey_alpha));
        }

        return convertView;
    }

    public void setData(List<DCheck> cheques) {
        this.mDChecks = cheques;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public DCheck getItem(int position) {
        return mDChecks.get(position);
    }
}
