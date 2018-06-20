package com.dostchat.dost.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dostchat.dost.R;
import com.dostchat.dost.activities.recharge.BrowsePlansActivity;
import com.dostchat.dost.adapters.recyclerView.PlansrvAdpater;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.ItemClickSupport;
import com.dostchat.dost.models.Plan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlansFragment extends Fragment {

    @BindView(R.id.plans_rv)
    RecyclerView rvPlans;

    // private UserPref mUser;

    public PlansFragment() {
        // Required empty public constructor
    }

    public static PlansFragment newInstance(int position, int id, String serviceid) {
        PlansFragment f = new PlansFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("planid", id);
        args.putString("serviceid", serviceid);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // mUser = new UserPref(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plans, container, false);
        ButterKnife.bind(this, view);

        String planid = String.valueOf(getArguments().getInt("planid"));
        String serviceid = getArguments().getString("serviceid");
        fetchPlans(getArguments().getInt("position"), planid, serviceid);

        return view;
    }

    private void fetchPlans(int position, String planid, String serviceid) {

        String url = AppConstants.SERVER_URL + "/index.php?option=com_jbackend&view=request&module=user&action=get&resource=getplansbycategory&plancategoryid=" + planid + "&serviceid=" + serviceid;
        final ArrayList<Plan> mPlans = new ArrayList<>();
        StringRequest str = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("ok")) {
                        JSONArray jsonArray1 = jsonObject.getJSONArray("data");
                        int length = jsonArray1.length();
                        if (length != 0) {
                            for (int i = 0; i < length; i++) {
                                JSONObject obj = jsonArray1.getJSONObject(i);
                                mPlans.add(new Plan(obj.getString("amount"),
                                        obj.getString("validity"),
                                        obj.getString("talktime"),
                                        obj.getString("service_id"),
                                        obj.getString("plancategory")));
                            }
                            displayPlans(mPlans);
                        } else {
                            displayEmpty();
                        }
                    }

                } catch (Exception ignored) {

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
//        {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//               // headers.put("Cookie",mUser.getSessionId());
//                return headers;
//            }
//        }
                ;
        DostChatApp.getInstance().addToRequestQueue(str);

    }

    private void displayEmpty() {

    }


    private void displayPlans(final ArrayList<Plan> data) {
        rvPlans.setHasFixedSize(true);
        rvPlans.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPlans.setAdapter(new PlansrvAdpater(getActivity(), data));
        ItemClickSupport.addTo(rvPlans).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                ((BrowsePlansActivity) getActivity()).sendToActivity(data.get(position).getAmount());
            }
        });
    }

}
