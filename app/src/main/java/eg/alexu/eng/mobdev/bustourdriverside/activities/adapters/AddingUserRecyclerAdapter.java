package eg.alexu.eng.mobdev.bustourdriverside.activities.adapters;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
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

import java.util.HashMap;
import java.util.List;
import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;

public class AddingUserRecyclerAdapter extends RecyclerView.Adapter<AddingUserRecyclerAdapter.ViewHolder>{

    private List<String> mUsersIds;
    private String mTripId;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String userId = mUsersIds.get(position);
        addName(holder, userId);
        addPhone(holder, userId);
        addPhoto(holder, userId);
        addListenerForAddUser(holder, userId);
    }

    private void addName(final ViewHolder holder, String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.USERS)
                .child(userId)
                .child(Constants.NAME)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            holder.userName.setText(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void addPhone(final ViewHolder holder, String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.USERS)
                .child(userId)
                .child(Constants.PHONE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            holder.phoneNum.setText(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void addPhoto(final ViewHolder holder, String userId) {
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
                                    .override(40, 40).
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

    private void addListenerForAddUser(ViewHolder holder, final String userId) {
        holder.addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertForAddUser(v, userId);
            }
        });
    }

    private void createAlertForAddUser(final View v, final String userId) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(v.getContext());
        builder1.setMessage(R.string.add_user);
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
                                child(userId).setValue(Constants.NO_VALUE);
                        HashMap<String, Object> tripInfo = new HashMap<String, Object>();
                        tripInfo.put(Constants.DRIVER_ID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                        tripInfo.put(Constants.LOC_X, Constants.NO_VALUE);
                        tripInfo.put(Constants.LOC_Y, Constants.NO_VALUE);
                        tripInfo.put(Constants.ENABLED, "false");
                        tripInfo.put(Constants.ARRIVED, "false");
                        dbRef.child(Constants.USERS).
                                child(userId).
                                child(Constants.TRIPS).
                                child(mTripId).updateChildren(tripInfo);
                        Toast.makeText(v.getContext(), R.string.user_created, Toast.LENGTH_LONG).show();
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


    @Override
    public int getItemCount() {
        return mUsersIds != null ? mUsersIds.size() : 0;
    }

    public void updateList(List<String> usersIds, String tripId) {
        mUsersIds = usersIds;
        mTripId = tripId;
        notifyDataSetChanged();
    }

    public void clearList() {
        if(mUsersIds != null) {
            mUsersIds.clear();
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView userName;
        private TextView phoneNum;
        private ImageView addUser;
        private ImageView userPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.user_name_text_view);
            phoneNum = (TextView) itemView.findViewById(R.id.phone_text_view);
            addUser = (ImageView) itemView.findViewById(R.id.add_user_to_trip);
            userPhoto = (ImageView) itemView.findViewById(R.id.user_profile_pic);
        }
    }
}
