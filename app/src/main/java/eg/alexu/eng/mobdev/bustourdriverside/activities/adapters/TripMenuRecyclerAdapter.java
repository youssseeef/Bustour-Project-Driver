package eg.alexu.eng.mobdev.bustourdriverside.activities.adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.activity.AllUsersLocationOnMapActivity;
import eg.alexu.eng.mobdev.bustourdriverside.activities.activity.PlayTripActivity;
import eg.alexu.eng.mobdev.bustourdriverside.activities.activity.UserLocalList;
import eg.alexu.eng.mobdev.bustourdriverside.activities.model.DriverTripsModel;
import eg.alexu.eng.mobdev.bustourdriverside.activities.service.DriverLocationService;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Extras;

public class TripMenuRecyclerAdapter extends RecyclerView.Adapter<TripMenuRecyclerAdapter.ViewHolder> {

    private List<String> mTripsId;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String tripId = mTripsId.get(position);
        final String tripName = DriverTripsModel.getTripData(tripId).get(Constants.NAME);
        final String tripDescription = DriverTripsModel.getTripData(tripId).get(Constants.DESCRIPTION);

        holder.tripName.setText(tripName);
        holder.tripDescription.setText(tripDescription);

        onClickListenerForPlayButton(holder, tripId);
        onClickListenerForAllUsersLocation(holder, tripId);
        onClickListenerForManageUsers(holder, tripId);
        onClickListenerForDeleteTrip(holder, tripId);
    }

    public void updateList(List<String> tripsId) {
        mTripsId = tripsId;
        notifyDataSetChanged();
    }

    private void onClickListenerForPlayButton(ViewHolder holder, final String tripId) {
        holder.playTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                dbRef.child(Constants.DRIVERS).
                        child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                        child(Constants.TRIPS).
                        child(tripId).
                        child(Constants.ENABLE_TRACKING).
                        setValue("true");
                usersExistInTrip(v, tripId);
            }
        });

    }

    private void usersExistInTrip(final View v, final String tripId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.DRIVERS).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(Constants.TRIPS).
                child(tripId).
                child(Constants.USER_IN_TRIP).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (DriverLocationService.mTripId == null || DriverLocationService.mTripId.equals(tripId)) {
                                Intent intent = new Intent(v.getContext(), PlayTripActivity.class);
                                intent.putExtra(Extras.TRIP_ID, tripId);
                                v.getContext().startActivity(intent);
                            } else {
                                makeAlertForClosingService(v, tripId);
                            }
                        } else {
                            Toast.makeText(v.getContext(), R.string.trip_empty, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void makeAlertForClosingService(final View v, final String tripId) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(v.getContext());
        builder1.setMessage(R.string.close_service);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        processStopService(v, DriverLocationService.TAG);
                        Intent intent = new Intent(v.getContext(), PlayTripActivity.class);
                        intent.putExtra(Extras.TRIP_ID, tripId);
                        v.getContext().startActivity(intent);
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

    private void processStopService(View v, final String tag) {
        Intent intent = new Intent(v.getContext(), DriverLocationService.class);
        intent.addCategory(tag);
        v.getContext().stopService(intent);
    }

    private void onClickListenerForAllUsersLocation(ViewHolder holder, final String tripId) {
        holder.allUsersLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AllUsersLocationOnMapActivity.class);
                intent.putExtra(Extras.TRIP_ID, tripId);
                v.getContext().startActivity(intent);
            }
        });
    }

    private void onClickListenerForManageUsers(final ViewHolder holder, final String tripId) {
        holder.manageUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UserLocalList.class);
                intent.putExtra(Extras.TRIP_ID, tripId);
                v.getContext().startActivity(intent);
            }
        });
    }

    private void onClickListenerForDeleteTrip(ViewHolder holder, final String tripId) {
        holder.deleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertForDelete(v, tripId);
            }
        });
    }

    private void createAlertForDelete(final View v, final String tripId) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(v.getContext());
        builder1.setMessage(R.string.delete_trip);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        processStopService(v, tripId);
                        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                        dbRef.child(Constants.DRIVERS).
                                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                child(Constants.TRIPS).
                                child(tripId).
                                child(Constants.USER_IN_TRIP).
                                addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                                        if (map != null) {
                                            for (String s : map.keySet()) {
                                                dbRef.child(Constants.USERS).
                                                        child(s).
                                                        child(Constants.TRIPS).
                                                        child(tripId).removeValue();
                                            }
                                        }
                                        dbRef.child(Constants.DRIVERS).
                                                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                                child(Constants.TRIPS).
                                                child(tripId).
                                                removeValue();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                        Toast.makeText(v.getContext(), R.string.trip_deleted, Toast.LENGTH_LONG).show();
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
        return mTripsId != null ? mTripsId.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView playTrip;
        private ImageView allUsersLocations;
        private ImageView manageUsers;
        private ImageView deleteTrip;
        private TextView tripName;
        private TextView tripDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            playTrip = (ImageView) itemView.findViewById(R.id.play_trip);
            allUsersLocations = (ImageView) itemView.findViewById(R.id.all_users_location);
            manageUsers = (ImageView) itemView.findViewById(R.id.manage_users);
            deleteTrip = (ImageView) itemView.findViewById(R.id.delete_trip);
            tripName = (TextView) itemView.findViewById(R.id.trip_name);
            tripDescription = (TextView) itemView.findViewById(R.id.trip_description);
        }
    }
}
