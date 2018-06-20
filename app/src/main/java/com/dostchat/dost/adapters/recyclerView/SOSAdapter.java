package com.dostchat.dost.adapters.recyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.dostchat.dost.R;
import com.dostchat.dost.interfaces.RetrofitService;
import com.dostchat.dost.models.sos.Result;
import com.dostchat.dost.models.sos.SOSAddResponse;
import com.dostchat.dost.services.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SOSAdapter extends RecyclerView.Adapter<SOSAdapter.SOSHolder> {

    private List<Result> contacts;
    private Context context;
    private String number;
    private RelativeLayout rl_empty;

    public SOSAdapter(Context context, String number, RelativeLayout rl_empty) {
        this.context = context;
        this.number = number;
        this.rl_empty = rl_empty;
        contacts = new ArrayList<>();
    }

    @Override
    public SOSHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new SOSHolder(view);
    }

    @Override
    public void onBindViewHolder(SOSHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void addItems(List<Result> result) {
        contacts.addAll(result);
        notifyDataSetChanged();
    }

    public void resetItems() {
        contacts.clear();
        notifyDataSetChanged();
    }

    class SOSHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tv_name, tv_phone;
        private ImageView iv_menu;
        private ImageView iv_profile;

        SOSHolder(View view) {
            super(view);

            tv_name = (TextView) view.findViewById(R.id.tv_name);
            tv_phone = (TextView) view.findViewById(R.id.tv_phone);
            iv_menu = (ImageView) view.findViewById(R.id.iv_menu);
            iv_profile = (ImageView) view.findViewById(R.id.iv_profile);

            iv_menu.setOnClickListener(this);
        }

        void bindData(int position) {
            Result result = contacts.get(position);

            tv_name.setText(result.getName());
            tv_phone.setText(result.getPhone_number());

            new ImageLoad(result.getPhone_number(), iv_profile).execute();
        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onClick(View view) {
            final Result result = contacts.get(getAdapterPosition());

            MenuBuilder menuBuilder = new MenuBuilder(context);

            menuBuilder.setCallback(new MenuBuilder.Callback() {
                @Override
                public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                    RetrofitService service = RetrofitInstance.createService(RetrofitService.class);

                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            service.deleteSOSContacts("DeleteSOSContacts", number, result.getPhone_number()).enqueue(new Callback<SOSAddResponse>() {
                                @Override
                                public void onResponse(Call<SOSAddResponse> call, Response<SOSAddResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Toast.makeText(context, response.body().getResult(), Toast.LENGTH_SHORT).show();
                                        contacts.remove(getAdapterPosition());
                                        notifyItemRemoved(getAdapterPosition());

                                        if (contacts.size() == 0) {
                                            rl_empty.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<SOSAddResponse> call, Throwable t) {

                                }
                            });
                            return true;
                    }

                    return false;
                }

                @Override
                public void onMenuModeChange(MenuBuilder menu) {

                }
            });

            MenuInflater inflater = new MenuInflater(context);
            inflater.inflate(R.menu.menu_delete, menuBuilder);

            MenuPopupHelper optionsMenu = new MenuPopupHelper(context, menuBuilder, view);
            optionsMenu.setForceShowIcon(true);
            optionsMenu.show();
        }
    }

    class ImageLoad extends AsyncTask<Void, Void, String> {

        private String phoneNumber;
        private ImageView imageView;

        ImageLoad(String phoneNumber, ImageView imageView) {
            this.imageView = imageView;
            this.phoneNumber = phoneNumber;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String image = null;
            Cursor phonesCursor;

            try {
                Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                phonesCursor = context.getContentResolver().query(phoneUri, new String[]{ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI}, null, null, null);

                if (phonesCursor != null && phonesCursor.moveToFirst()) {
                    image = phonesCursor.getString(0);
                }

                if (phonesCursor != null) {
                    phonesCursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return image;
        }

        @Override
        protected void onPostExecute(String image) {
            if (image != null) {
                Glide.with(context).load(image).asBitmap().centerCrop().into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }
        }
    }
}
