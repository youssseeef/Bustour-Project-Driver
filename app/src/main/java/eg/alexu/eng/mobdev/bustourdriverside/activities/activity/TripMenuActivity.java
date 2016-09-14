package eg.alexu.eng.mobdev.bustourdriverside.activities.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.adapters.TripMenuRecyclerAdapter;
import eg.alexu.eng.mobdev.bustourdriverside.activities.model.DriverTripsModel;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;

public class TripMenuActivity extends AppCompatActivity {

    private RecyclerView mAllTripsRecyclerView;
    private TripMenuRecyclerAdapter mAdapter;
    private TextView mNoTripsLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trips_list);
        mNoTripsLabel = (TextView) findViewById(R.id.no_trips_label);
        mAllTripsRecyclerView = (RecyclerView) findViewById(R.id.trip_recycler_view);
        initializeRecyclerView();
        addListenerToTrips();
    }

    private void addListenerToTrips() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.DRIVERS).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(Constants.TRIPS).
                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        changeTripDetails(dataSnapshot);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        changeTripDetails(dataSnapshot);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        removeTrip(dataSnapshot);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("TripMenuActivity", databaseError.getMessage());
                    }
                });
    }

    public void onClickAddTripFab(View v) {
        Intent i = new Intent(TripMenuActivity.this, CreateNewTripActivity.class);
        startActivity(i);
    }

    private void initializeRecyclerView() {
        mAdapter = new TripMenuRecyclerAdapter();
        mAllTripsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAllTripsRecyclerView.setAdapter(mAdapter);
    }

    private void changeTripDetails(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            String tripId = dataSnapshot.getKey();
            HashMap<String, String> tripDetails = (HashMap<String, String>) dataSnapshot.getValue();
            String name = tripDetails.get(Constants.NAME);
            String description = tripDetails.get(Constants.DESCRIPTION);
            DriverTripsModel.addNewTrip(tripId, description, name);
            mAdapter.updateList(DriverTripsModel.getTripIds());
            showHideRecyclerView();
        }
    }

    private void showHideRecyclerView() {
        if (mAllTripsRecyclerView.getAdapter().getItemCount() == 0) {
            mAllTripsRecyclerView.setVisibility(View.GONE);
            mNoTripsLabel.setVisibility(View.VISIBLE);
        } else {
            mAllTripsRecyclerView.setVisibility(View.VISIBLE);
            mNoTripsLabel.setVisibility(View.GONE);
        }
    }

    private void removeTrip(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            String tripId = dataSnapshot.getKey();
            HashMap<String, String> hm = (HashMap<String, String>) dataSnapshot.getValue();
            DriverTripsModel.removeTrip(tripId);
            mAdapter.updateList(DriverTripsModel.getTripIds());
            showHideRecyclerView();
        }
    }
}
