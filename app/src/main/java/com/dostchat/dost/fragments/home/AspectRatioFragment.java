package com.dostchat.dost.fragments.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.cameraview.AspectRatio;

import java.util.Arrays;
import java.util.Set;

public class AspectRatioFragment extends DialogFragment {

    private static final String ARG_ASPECT_RATIOS = "aspect_ratios";
    private static final String ARG_CURRENT_ASPECT_RATIO = "current_aspect_ratio";

    private Listener mListener;

    public static AspectRatioFragment newInstance(Set<AspectRatio> ratios,
                                                  AspectRatio currentRatio) {
        final AspectRatioFragment fragment = new AspectRatioFragment();
        final Bundle args = new Bundle();
        args.putParcelableArray(ARG_ASPECT_RATIOS,
                ratios.toArray(new AspectRatio[ratios.size()]));
        args.putParcelable(ARG_CURRENT_ASPECT_RATIO, currentRatio);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final AspectRatio[] ratios = (AspectRatio[]) args.getParcelableArray(ARG_ASPECT_RATIOS);
        if (ratios == null) {
            throw new RuntimeException("No ratios");
        }
        Arrays.sort(ratios);
        final AspectRatio current = args.getParcelable(ARG_CURRENT_ASPECT_RATIO);
        final AspectRatioAdapter adapter = new AspectRatioAdapter(ratios, current);
        return new AlertDialog.Builder(getActivity())
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        if (mListener != null) {
                            mListener.onAspectRatioSelected(ratios[position]);
                        }
                    }
                })
                .create();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private static class AspectRatioAdapter extends BaseAdapter {

        private final AspectRatio[] mRatios;
        private final AspectRatio mCurrentRatio;

        AspectRatioAdapter(AspectRatio[] ratios, AspectRatio current) {
            mRatios = ratios;
            mCurrentRatio = current;
        }

        @Override
        public int getCount() {
            return mRatios.length;
        }

        @Override
        public AspectRatio getItem(int position) {
            return mRatios[position];
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            AspectRatioAdapter.ViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                holder = new AspectRatioAdapter.ViewHolder();
                holder.text = (TextView) view.findViewById(android.R.id.text1);
                view.setTag(holder);
            } else {
                holder = (AspectRatioAdapter.ViewHolder) view.getTag();
            }
            AspectRatio ratio = getItem(position);
            StringBuilder sb = new StringBuilder(ratio.toString());
            if (ratio.equals(mCurrentRatio)) {
                sb.append(" *");
            }
            holder.text.setText(sb);
            return view;
        }

        private static class ViewHolder {
            TextView text;
        }

    }

    public interface Listener {
        void onAspectRatioSelected(@NonNull AspectRatio ratio);
    }

}
