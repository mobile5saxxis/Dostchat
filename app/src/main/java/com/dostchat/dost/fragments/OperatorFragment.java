package com.dostchat.dost.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.dostchat.dost.R;
import com.dostchat.dost.activities.recharge.DCActivity;
import com.dostchat.dost.activities.recharge.DTHActivity;
import com.dostchat.dost.activities.recharge.MobileRechargeActivity;
import com.dostchat.dost.adapters.recyclerView.OperatorsAdapter;
import com.dostchat.dost.helpers.ItemClickSupport;
import com.dostchat.dost.models.Operator;

import java.util.ArrayList;

/**
 * Created by saxxis25 on 4/27/2017.
 */

public class OperatorFragment extends DialogFragment {

    private ArrayList<Operator> mData;
    private String mType;

    public OperatorFragment() {
    }

    public static OperatorFragment newInstance(ArrayList<Operator> mData, String prepaid) {
        OperatorFragment f = new OperatorFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("array",mData);
        args.putString("type",prepaid);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData  = getArguments().getParcelableArrayList("array");
        mType = getArguments().getString("type");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders,container,false);
        RecyclerView rvOperators = (RecyclerView)view.findViewById(R.id.orders_rv);

        rvOperators.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvOperators.setAdapter(new OperatorsAdapter(getActivity(),mData));
        ItemClickSupport.addTo(rvOperators).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                submitData(mData.get(position),mType);
            }
        });

        return view;
    }

    private void submitData(Operator operator, String mType) {
        switch (mType){
            case "prepaid":
                ((MobileRechargeActivity)getActivity()).updateOperator(operator);
                dismiss();
                break;
            case "postpaid":
                //((MobileRechargePostpaidActivity)getActivity()).updateOperator(operator);
                dismiss();
                break;
            case "dth":
                ((DTHActivity)getActivity()).updateOperator(operator);
                dismiss();
                break;
            case "dc":
               ((DCActivity)getActivity()).updateOperator(operator);
                dismiss();
                break;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
}
