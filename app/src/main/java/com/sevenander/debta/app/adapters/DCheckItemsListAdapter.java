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
import com.sevenander.debta.app.activities.DCheckEditActivity;
import com.sevenander.debta.app.pojo.DCheckItem;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrii on 08.03.15.
 */
public class DCheckItemsListAdapter extends ArrayAdapter<DCheckItem> {

    private Context mContext;
    private int mLayoutResourceId;
    private List<DCheckItem> mDCheckItems;
    private boolean mIsEditing;

    public DCheckItemsListAdapter(Context context, int layoutResourceId,
                                  ArrayList<DCheckItem> dCheckItems, boolean isEditing) {
        super(context, layoutResourceId, dCheckItems);
        this.mLayoutResourceId = layoutResourceId;
        this.mContext = context;
        this.mDCheckItems = dCheckItems;
        this.mIsEditing = isEditing;
    }

    public static class ViewHolder {
        TextView tvName;
        TextView tvPrice;
        TextView tvDebt;
        ImageView ivRemove;

        public ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tv_cheque_item_name);
            tvPrice = (TextView) view.findViewById(R.id.tv_cheque_item_price);
            tvDebt = (TextView) view.findViewById(R.id.tv_cheque_item_debt);
            ivRemove = (ImageView) view.findViewById(R.id.iv_cheque_item_delete);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DCheckItem dCheckItem = getItem(position);

        holder.tvName.setText(dCheckItem.getName());
        holder.tvDebt.setText(Utils.formatMoney(dCheckItem.getDebt()));
        holder.tvPrice.setText(Utils.formatMoney(dCheckItem.getPrice()));

        if (mIsEditing) {
            holder.ivRemove.setVisibility(View.VISIBLE);
            holder.ivRemove.setOnClickListener(listener(position));
        } else {
            holder.ivRemove.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public void setData(List<DCheckItem> dCheckItems) {
        this.mDCheckItems.clear();
        if (dCheckItems != null)
            this.mDCheckItems.addAll(dCheckItems);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public DCheckItem getItem(int position) {
        return mDCheckItems.get(position);
    }

    @Override
    public void remove(DCheckItem dCheckItem) {
        super.remove(dCheckItem);
        ((DCheckEditActivity) mContext).removeItem(dCheckItem);
    }

    private View.OnClickListener listener(final int pos) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.iv_cheque_item_delete:
                        remove(getItem(pos));
                        if (getCount() == 0)
                            ((DCheckEditActivity) mContext).hideDate();
                        break;
                }
            }
        };
    }
}
