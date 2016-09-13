package eg.alexu.eng.mobdev.bustourdriverside.activities.adapters;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.model.DriverTripsModel;
import eg.alexu.eng.mobdev.bustourdriverside.activities.recyclerview.SnappyRecyclerView;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.CalculateDistanceBetweenTwoPoint;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;


public class PlayTripAdapter extends SnappyRecyclerView.Adapter<PlayTripAdapter.ViewHolder> {

    private List<String> usersId;
    private String mTripId;
    private boolean distanceFlag;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.during_play_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String userId = usersId.get(position);
        if (!distanceFlag) {
            loadImage(holder, userId);
            setUserName(holder, userId);
            setPhoneNumber(holder, userId);
        }
        setDistanceRemaining(holder, userId);
    }

    private void loadImage(final ViewHolder holder, String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.USERS)
                .child(userId)
                .child(Constants.USER_PHOTO)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imageUrl = dataSnapshot.getValue(String.class);
                        if (imageUrl != null) {
                            Glide.with(holder.userImage.getContext())
                                    .load(imageUrl)
                                    .asBitmap()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                                    .centerCrop()
                                    .override(500, 500).
                                    into(new BitmapImageViewTarget(holder.userImage) {
                                        @Override
                                        protected void setResource(Bitmap resource) {
                                            RoundedBitmapDrawable circularBitmapDrawable =
                                                    RoundedBitmapDrawableFactory.create(holder.userImage.getResources(), resource);
                                            circularBitmapDrawable.setCircular(true);
                                            holder.userImage.setImageDrawable(circularBitmapDrawable);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setDistanceRemaining(final ViewHolder holder, final String userId) {
        int distance = DriverTripsModel.getUserDistance(mTripId, userId);
        if (distance == -1) {
            holder.distanceRemaining.setText("__ meters");
            holder.distanceRemainingProgressBar.setProgress(0);
            return;
        }
        holder.distanceRemaining.setText(distance + " meters");
        if (distance > 1000)
            holder.distanceRemainingProgressBar.setProgress(0);
        else if (distance < CalculateDistanceBetweenTwoPoint.RING_DISTANCE) {
            holder.distanceRemainingProgressBar.setProgress(100);
        } else {
            double temp = (distance - CalculateDistanceBetweenTwoPoint.RING_DISTANCE) / 10;
            int finalProgress = 100 - (int) temp;
            holder.distanceRemainingProgressBar.setProgress(finalProgress);
        }
    }

    public void updateData() {
        distanceFlag = true;
        notifyDataSetChanged();
    }

    private void setPhoneNumber(final ViewHolder holder, String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.USERS).
                child(userId).
                child(Constants.PHONE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.userPhone.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUserName(final ViewHolder holder, String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.USERS).
                child(userId).
                child(Constants.NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.userName.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return usersId != null ? usersId.size() : 0;
    }

    public void updateListUsersId(List<String> usersId, String tripId) {
        this.usersId = usersId;
        mTripId = tripId;
        distanceFlag = false;
        notifyDataSetChanged();
    }

    public class ViewHolder extends SnappyRecyclerView.ViewHolder {

        private ImageView userImage;
        private TextView userName;
        private TextView userPhone;
        private TextView distanceRemaining;
        private ProgressBar distanceRemainingProgressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            userImage = (ImageView) itemView.findViewById(R.id.user_photo);
            userName = (TextView) itemView.findViewById(R.id.user_name_during_trip);
            userPhone = (TextView) itemView.findViewById(R.id.user_phone_during_trip);
            distanceRemaining = (TextView) itemView.findViewById(R.id.distance_remaining_during_trip);
            distanceRemainingProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar_distance_remaining_during_trip);
        }
    }
}
