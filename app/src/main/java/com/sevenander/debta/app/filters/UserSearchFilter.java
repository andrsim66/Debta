package com.sevenander.debta.app.filters;

import android.widget.Filter;

import com.sevenander.debta.app.adapters.UserSearchListAdapter;
import com.sevenander.debta.app.pojo.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrii on 03.03.15.
 */
public class UserSearchFilter extends Filter {

    private List<User> mOriginal;
    private UserSearchListAdapter mAdapter;

    public UserSearchFilter(UserSearchListAdapter adapter, List<User> original) {
        this.mOriginal = original;
        this.mAdapter = adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        constraint = constraint.toString().toLowerCase();
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length() > 0) {
            List<User> foundedUsers = new ArrayList<User>();
            for (User user : mOriginal) {
                if (user.getFName().toLowerCase().contains(constraint)) {
                    if (!foundedUsers.contains(user)) foundedUsers.add(user);
                }
            }
            results.values = foundedUsers;
            results.count = foundedUsers.size();
        } else {
            results.values = mOriginal;
            results.count = mOriginal.size();
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        mAdapter.clear();
        for (User user : (List<User>) results.values) {
            mAdapter.add(user);
        }
        mAdapter.notifyDataSetChanged();
    }
}
