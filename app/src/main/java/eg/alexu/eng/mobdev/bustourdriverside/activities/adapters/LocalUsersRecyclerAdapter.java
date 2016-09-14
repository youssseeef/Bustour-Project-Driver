package eg.alexu.eng.mobdev.bustourdriverside.activities.adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.service.DriverLocationService;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;


public class LocalUsersRecyclerAdapter extends RecyclerView.Adapter<LocalUsersRecyclerAdapter.ViewHolder> {

    private List<String> mUsersIds;
    private String mTripId;
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String userId = mUsersIds.get(position);
        addName(holder, userId);
        addPhone(holder, userId);
        addPhoto(holder, userId);
        onClickListenerForDeleteUser(holder, userId);
    }

    private void addName(final ViewHolder holder, String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.USERS).child(userId).child(Constants.NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.userName.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addPhone(final ViewHolder holder, String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.USERS).child(userId).child(Constants.PHONE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String phone = dataSnapshot.getValue(String.class);
                holder.userPhone.setText(phone);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addPhoto(final ViewHolder holder, String userId) {
        Log.d("Hamada", ""+FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.USERS)
                .child(userId)
                .child(Constants.USER_PHOTO)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imageUrl = dataSnapshot.getValue(String.class);
                        if (imageUrl != null) {
                            Glide.with(holder.userPhoto.getContext())
                                    .load(imageUrl)
                                    .asBitmap()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                                    .centerCrop()
                                    .override(25, 25).
                                    into(new BitmapImageViewTarget(holder.userPhoto) {
                                        @Override
                                        protected void setResource(Bitmap resource) {
                                            RoundedBitmapDrawable circularBitmapDrawable =
                                                    RoundedBitmapDrawableFactory.create(holder.userPhoto.getResources(), resource);
                                            circularBitmapDrawable.setCircular(true);
                                            holder.userPhoto.setImageDrawable(circularBitmapDrawable);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void onClickListenerForDeleteUser(ViewHolder holder, final String userId) {
        holder.deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertForDelete(v, userId);
            }
        });
    }

    private void createAlertForDelete(final View v, final String userId) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(v.getContext());
        builder1.setMessage(R.string.delete_user);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                        dbRef.child(Constants.DRIVERS).
                                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                child(Constants.TRIPS).
                                child(mTripId).
                                child(Constants.USER_IN_TRIP).
                                child(userId).
                                removeValue();
                        dbRef.child(Constants.USERS).
                                child(userId).
                                child(Constants.TRIPS).
                                child(mTripId).removeValue();
                        Toast.makeText(v.getContext(), R.string.user_deleted, Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void updateList(List<String> usersIds, String tripId) {
        mUsersIds = usersIds;
        mTripId = tripId;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mUsersIds != null ? mUsersIds.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView userName;
        private TextView userPhone;
        private ImageView deleteUser;
        private ImageView userPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            userPhone = (TextView) itemView.findViewById(R.id.user_phone);
            deleteUser = (ImageView) itemView.findViewById(R.id.delete_user);
            userPhoto = (ImageView) itemView.findViewById(R.id.user_profile_photo);
        }
    }
}
