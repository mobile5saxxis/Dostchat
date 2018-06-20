package com.dostchat.dost.adapters.recyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.dostchat.dost.R;
import com.dostchat.dost.models.Plan;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by saxxis25 on 4/26/2017.
 */

public class PlansrvAdpater extends RecyclerView.Adapter<PlansrvAdpater.PlansrvHolder>  {

    private Context mActivity;
    private ArrayList<Plan> mData;

    public PlansrvAdpater(Context xont, ArrayList<Plan> mData){
        this.mActivity = xont;
        this.mData = mData;
    }

    @Override
    public PlansrvHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.plan_item, parent, false);
        return new PlansrvHolder(view);
    }

    @Override
    public void onBindViewHolder(PlansrvHolder holder, int position) {
        holder.txtTalktime.setText(mData.get(position).getTalktime());
        holder.txtValidity.setText(mData.get(position).getValidity());
        holder.txtAmount.setText("Rs " + mData.get(position).getAmount());

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class PlansrvHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.plan_tt)
        TextView txtTalktime;
        @BindView(R.id.plan_validity)
        TextView txtValidity;
        @BindView(R.id.plan_amount)
        TextView txtAmount;


        public PlansrvHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
