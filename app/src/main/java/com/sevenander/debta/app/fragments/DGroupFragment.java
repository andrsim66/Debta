package com.sevenander.debta.app.fragments;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.sevenander.debta.app.R;
import com.sevenander.debta.app.activities.DCheckListActivity;
import com.sevenander.debta.app.activities.DGroupEditActivity;
import com.sevenander.debta.app.activities.UserListActivity;
import com.sevenander.debta.app.adapters.DGroupListAdapter;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.loaders.DGroupLoader;
import com.sevenander.debta.app.pojo.DGroup;
import com.sevenander.debta.app.pojo.User;
import com.sevenander.debta.app.utils.Const;
import com.sevenander.debta.app.utils.Logger;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DGroupFragment extends Fragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<DGroup>>, View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private FrameLayout mProgressBar;
    private ListView mLvDGroups;
    private TextView mTvTotal;
    private SwipeRefreshLayout mSrlDGroups;
    private FloatingActionButton mFabCreateDGroup;

    private DGroupListAdapter mAdapter;

    private List<DGroup> mDGroups;

    private int mTabPos;

    private ContentObserver mObserver;

    private boolean isLoading;

    public static DGroupFragment newInstance(int tabPos) {
        DGroupFragment fragment = new DGroupFragment();
        Bundle args = new Bundle();
        args.putInt(Const.KEY_TAB_POS, tabPos);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DGroupFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTabPos = getArguments().getInt(Const.KEY_TAB_POS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dgroup, container, false);

        initViews(view);
        setupViews();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initLoader();
        registerObserver();

        super.onActivityCreated(savedInstanceState);
    }

    private void initLoader() {
        Bundle args = new Bundle();
        args.putInt(Const.KEY_TAB_POS, mTabPos);
        args.putString(Const.KEY_STRING_URI, DebtaContract.DGroupEntry.buildDGroupUri().toString());
        args.putString(Const.KEY_ADMIN_ID, User.getCurrentUser().getObjectId());
        getLoaderManager().initLoader(Const.DGROUP_LOADER, args, this);
    }

    private void registerObserver() {
        mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            public void onChange(boolean selfChange) {
                if (!isLoading) {
                    Logger.d("onChange");
                    onRefresh();
                }
            }
        };
        getActivity().getContentResolver().registerContentObserver(
                DebtaContract.DGroupEntry.CONTENT_URI, false, mObserver);
    }

    private void initViews(View view) {
        mLvDGroups = (ListView) view.findViewById(R.id.lv_dgroups);
        mTvTotal = (TextView) view.findViewById(R.id.tv_total_dgroups);
        mProgressBar = (FrameLayout) view.findViewById(R.id.fl_progress_dgroup_container);
        mSrlDGroups = (SwipeRefreshLayout) view.findViewById(R.id.srl_dgroup);
        mFabCreateDGroup = (FloatingActionButton) view.findViewById(R.id.fab_add_group);
    }

    private void setupViews() {
        if (mTabPos == 0) {
            mFabCreateDGroup.setVisibility(View.GONE);
        } else {
            mFabCreateDGroup.setVisibility(View.VISIBLE);
            mFabCreateDGroup.setOnClickListener(this);
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mLvDGroups.setOnItemClickListener(this);
        mSrlDGroups.setColorSchemeResources(R.color.colorPrimary);
        mSrlDGroups.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        if (!isDetached()) {
            Bundle args = new Bundle();
            args.putInt(Const.KEY_TAB_POS, mTabPos);
            args.putString(Const.KEY_STRING_URI, null);
            args.putString(DebtaContract.DGroupEntry.COLUMN_ADMIN_ID,
                    User.getCurrentUser().getObjectId());
            getLoaderManager().restartLoader(Const.DGROUP_LOADER, args, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mTabPos == 1) {
            Intent intent = new Intent(getActivity(), UserListActivity.class);
            intent.putExtra(Const.KEY_DGROUP_NAME, mDGroups.get(position).getName());
            intent.putExtra(Const.KEY_DGROUP_ID, mDGroups.get(position).getObjectId());
            intent.putStringArrayListExtra(Const.KEY_USER_IDS,
                    new ArrayList<>(mDGroups.get(position).getMembers()));
            startActivityForResult(intent, Const.REQUEST_CODE);
        } else if (mTabPos == 0) {
            Intent intent = new Intent(getActivity(), DCheckListActivity.class);
            intent.putExtra(Const.KEY_DGROUP_ID, mDGroups.get(position).getObjectId());
            intent.putExtra(Const.KEY_USER_ID, User.getCurrentUser().getObjectId());
            intent.putStringArrayListExtra(Const.KEY_USER_IDS,
                    new ArrayList<>(mDGroups.get(position).getMembers()));
            startActivityForResult(intent, Const.REQUEST_CODE);
        }
    }

    @Override
    public Loader<ArrayList<DGroup>> onCreateLoader(int id, Bundle args) {
        isLoading = true;
        int tabPos = args.getInt(Const.KEY_TAB_POS);
        String sUri = args.getString(Const.KEY_STRING_URI);
        Uri uri = sUri == null ? null : Uri.parse(sUri);
        if (tabPos == 0) {
            String selection = DebtaContract.DGroupEntry.COLUMN_MEMBERS + " GLOB ?";
            String[] selectionArgs = new String[]{"*" + args.getString(Const.KEY_ADMIN_ID) + "*"};

            return new DGroupLoader(
                    getActivity(),
                    uri,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    tabPos);
        } else {
            String selection = DebtaContract.DGroupEntry.COLUMN_ADMIN_ID + " = ?";
            String[] selectionArgs = new String[]{args.getString(Const.KEY_ADMIN_ID)};

            return new DGroupLoader(
                    getActivity(),
                    uri,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    tabPos);
        }
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<DGroup>> loader, ArrayList<DGroup> data) {
        isLoading = false;
        mProgressBar.setVisibility(View.GONE);
        mAdapter = new DGroupListAdapter(getActivity(), R.layout.item_dgroup, data);
        mLvDGroups.setAdapter(mAdapter);
        mDGroups = data;
        mTvTotal.setText(Utils.formatMoney(calcTotal(data)));
        mSrlDGroups.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<DGroup>> loader) {
        mAdapter.setData(null);
    }

    private double calcTotal(List<DGroup> dGroups) {
        double total = 0;
        for (int i = 0; i < dGroups.size(); i++) {
            total += dGroups.get(i).getDebt();
        }
        return total;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_group:
                Intent intent = new Intent(getActivity(), DGroupEditActivity.class);
                intent.putExtra(Const.KEY_CALL_EXTRA, Const.OTHER_CALL);
                startActivityForResult(intent, Const.REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onRefresh();
    }
}
