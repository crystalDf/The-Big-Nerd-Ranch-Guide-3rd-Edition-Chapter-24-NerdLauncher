package com.star.nerdlauncher;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class NerdLauncherFragment extends Fragment {

    private static final String TAG = "NerdLauncherFragment";

    private RecyclerView mRecyclerView;

    public static NerdLauncherFragment newInstance() {
        return new NerdLauncherFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);
        mRecyclerView = view.findViewById(R.id.fragment_nerd_launcher_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return view;
    }

    private void setupAdapter() {
        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(startupIntent, 0);

        Collections.sort(resolveInfos, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(
                lhs.loadLabel(packageManager).toString(),
                rhs.loadLabel(packageManager).toString()
        ));

        Log.i(TAG, "Found " + resolveInfos.size() + " resolveInfos.");
        
        mRecyclerView.setAdapter(new ActivityAdapter(resolveInfos));
    }

    private class ActivityHolder extends RecyclerView.ViewHolder {

        private ResolveInfo mResolveInfo;
        private TextView mNameIconTextView;

        public ActivityHolder(View itemView) {
            super(itemView);
            mNameIconTextView = (TextView) itemView;
            mNameIconTextView.setOnClickListener(v -> {
                ActivityInfo activityInfo = mResolveInfo.activityInfo;

                Intent intent = new Intent(Intent.ACTION_MAIN)
                        .setClassName(activityInfo.applicationInfo.packageName,
                                activityInfo.name)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            });
        }

        public void bindActivity(ResolveInfo resolveInfo) {
            mResolveInfo = resolveInfo;

            PackageManager packageManager = getActivity().getPackageManager();

            final String appName = mResolveInfo.loadLabel(packageManager).toString();
            final Drawable appIcon = mResolveInfo.loadIcon(packageManager);

            mNameIconTextView.getViewTreeObserver().addOnGlobalLayoutListener(
                    () -> {
                        mNameIconTextView.setText(appName);

                        appIcon.setBounds(0, 0,
                                mNameIconTextView.getHeight(), mNameIconTextView.getHeight());
                        mNameIconTextView.setCompoundDrawables(null, null, appIcon, null);
//                        mNameIconTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, appIcon, null);
                    });
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder> {

        private List<ResolveInfo> mResolveInfos;

        public ActivityAdapter(List<ResolveInfo> resolveInfos) {
            mResolveInfos = resolveInfos;
        }

        @Override
        public ActivityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(
                    android.R.layout.simple_list_item_1, parent, false);
            return new ActivityHolder(view);
        }

        @Override
        public void onBindViewHolder(ActivityHolder holder, int position) {
            ResolveInfo resolveInfo = mResolveInfos.get(position);
            holder.bindActivity(resolveInfo);
        }

        @Override
        public int getItemCount() {
            return mResolveInfos.size();
        }
    }
}
