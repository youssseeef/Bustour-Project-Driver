package eg.alexu.eng.mobdev.bustourdriverside.activities.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.adapters.PlayTripAdapter;
import eg.alexu.eng.mobdev.bustourdriverside.activities.model.DriverTripsModel;
import eg.alexu.eng.mobdev.bustourdriverside.activities.recyclerview.SnappyRecyclerView;
import eg.alexu.eng.mobdev.bustourdriverside.activities.service.DriverLocationService;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.CalculateDistanceBetweenTwoPoint;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Extras;

public class PlayTripActivity extends AppCompatActivity {

    private PlayTripAdapter mAdapter;
    private SnappyRecyclerView mRecyclerView;
    private String mTripId;
    private double locX;
    private double locY;
    private List<String> usersId;
    private boolean isAppExit;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_trip);
        mTripId = getIntent().getStringExtra(Extras.TRIP_ID);
        locX = 0;
        locY = 0;
        isAppExit = false;
        launchService();
        initializeRecyclerView();
        updateUserLocation();
        getUsersId();
    }

    @Override
    public void onBackPressed() {
        continueTripOrPauseIt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!isAppExit) {
            stopService();
        }
    }

    private void stopService() {
        if (isMyServiceRunning(DriverLocationService.class))
            processStopService(DriverLocationService.TAG);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.DRIVERS).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(Constants.TRIPS).
                child(mTripId).
                child(Constants.ENABLE_TRACKING).
                setValue("false");
    }

    private void continueTripOrPauseIt() {
        isAppExit = true;
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(R.string.continue_trip);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopService();
                        dialog.cancel();
                        finish();
                    }
                });

        builder1.setNegativeButton(
                "No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    private void launchService() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 10);
            return;
        }
        if (!isMyServiceRunning(DriverLocationService.class)) {
            processStartService(DriverLocationService.TAG);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isMyServiceRunning(DriverLocationService.class))
                        processStopService(DriverLocationService.TAG);
                }
            }, 1000 * 60 * 60 * 2);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void updateUserLocation() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.DRIVERS).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(Constants.TRIPS).
                child(mTripId).
                child(Constants.LOC_X).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    locX = Double.parseDouble(dataSnapshot.getValue(String.class));
                    updateDistances();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        dbRef.child(Constants.DRIVERS).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(Constants.TRIPS).
                child(mTripId).
                child(Constants.LOC_Y).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    locY = Double.parseDouble(dataSnapshot.getValue(String.class));
                    updateDistances();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateDistances() {
        if (usersId != null) {
            for (final String userId : usersId) {
                final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                dbRef.child(Constants.USERS).
                        child(userId).
                        child(Constants.TRIPS).
                        child(mTripId).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists() && locX != 0.0 && locY != 0.0) {
                                    HashMap<String, String> tempMap = (HashMap<String, String>) dataSnapshot.getValue();
                                    double distance = CalculateDistanceBetweenTwoPoint.distanceBetweenTwoCoordinates
                                            (locX, Double.parseDouble(tempMap.get(Constants.LOC_X)), locY, Double.parseDouble(tempMap.get(Constants.LOC_Y)));
                                    DriverTripsModel.setUserDistance(mTripId, userId, (int) distance);
                                    if (CalculateDistanceBetweenTwoPoint.checkTargetReached(distance)) {
                                        dbRef.child(Constants.USERS).
                                                child(userId).
                                                child(Constants.TRIPS).
                                                child(mTripId).
                                                child(Constants.ARRIVED).setValue("true");
                                    } else {
                                        dbRef.child(Constants.USERS).
                                                child(userId).
                                                child(Constants.TRIPS).
                                                child(mTripId).
                                                child(Constants.ARRIVED).setValue("false");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
            mAdapter.updateData();
        }
    }

    private void getUsersId() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.DRIVERS).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(Constants.TRIPS).
                child(mTripId).
                child(Constants.USER_IN_TRIP).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                            modifyUsersId(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void processStopService(final String tag) {
        Intent intent = new Intent(getApplicationContext(), DriverLocationService.class);
        intent.addCategory(tag);
        stopService(intent);
    }

    private void processStartService(final String tag) {
        Intent intent = new Intent(getApplicationContext(), DriverLocationService.class);
        intent.addCategory(tag);
        intent.putExtra(Extras.TRIP_ID, mTripId);
        startService(intent);
    }

    private void modifyUsersId(DataSnapshot dataSnapshot) {
        HashMap<String, String> temp = (HashMap<String, String>) dataSnapshot.getValue();
        List<String> usersId = new ArrayList<String>();
        if(temp != null) {
            for (String userId : temp.keySet()) {
                usersId.add(userId);
            }
        }
        this.usersId = usersId;
        mAdapter.updateListUsersId(usersId, mTripId);
    }

    private void initializeRecyclerView() {
        mAdapter = new PlayTripAdapter();
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mRecyclerView = (SnappyRecyclerView) findViewById(R.id.play_trip_recycler_view);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
