package com.dostchat.dost.adapters.recyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dostchat.dost.R;
import com.dostchat.dost.models.Operator;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by saxxis25 on 4/27/2017.
 */

public class OperatorsAdapter extends RecyclerView.Adapter<OperatorsAdapter.OperatorsHolder> {

    private Context mContext;
    private ArrayList<Operator> mData;

    public OperatorsAdapter(Context cont, ArrayList<Operator> data){
        this.mContext = cont;
        this.mData = data;
    }

    @Override
    public OperatorsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.operator_item,parent,false);
        return new OperatorsHolder(view);
    }

    @Override
    public void onBindViewHolder(OperatorsHolder holder, int position) {
        Glide.with(mContext).load("http://www.topcharging.com/images/osproperty/category/"+mData.get(position).getImageLocation()).into(holder.cimv);
        holder.title.setText(mData.get(position).getOptype_name());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class OperatorsHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.operator_img)
        ImageView cimv;

        @BindView(R.id.operator_title)
        TextView title;

        public OperatorsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
