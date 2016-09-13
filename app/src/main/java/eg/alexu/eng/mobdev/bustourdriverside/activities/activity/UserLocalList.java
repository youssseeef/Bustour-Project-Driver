package eg.alexu.eng.mobdev.bustourdriverside.activities.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.adapters.LocalUsersRecyclerAdapter;
import eg.alexu.eng.mobdev.bustourdriverside.activities.model.DriverTripsModel;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Extras;


public class UserLocalList extends AppCompatActivity {

    private RecyclerView mUsersRecyclerView;
    private LocalUsersRecyclerAdapter mAdapter;
    private TextView mNoUserLabel;
    private String mTripId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_screen);
        mNoUserLabel = (TextView) findViewById(R.id.no_user_label);
        mTripId = getIntent().getStringExtra(Extras.TRIP_ID);
        mUsersRecyclerView = (RecyclerView) findViewById(R.id.users_recycler_view);
        initializeRecyclerView();
        addListenersToUsers();
    }

    private void addListenersToUsers() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.DRIVERS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constants.TRIPS)
                .child(mTripId)
                .child(Constants.USER_IN_TRIP)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        modifyUserInTrip(dataSnapshot);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        modifyUserInTrip(dataSnapshot);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        removeUserInTrip(dataSnapshot);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }


    public void onClickAddNewUserFab(View v) {
        Intent i = new Intent(UserLocalList.this, AddingUser.class);
        i.putExtra(Extras.TRIP_ID, mTripId);
        startActivity(i);
    }

    private void removeUserInTrip(DataSnapshot dataSnapshot) {
        String userId = dataSnapshot.getKey();
        DriverTripsModel.removeUserInATrip(mTripId, userId);
        mAdapter.updateList(DriverTripsModel.getUsersInATrip(mTripId), mTripId);
        chooseLabelOrRecyclerView();
    }

    private void modifyUserInTrip(DataSnapshot dataSnapshot) {
        String userId = dataSnapshot.getKey();
        DriverTripsModel.addUserInATrip(mTripId, userId);
        mAdapter.updateList(DriverTripsModel.getUsersInATrip(mTripId), mTripId);
        chooseLabelOrRecyclerView();
    }

    private void chooseLabelOrRecyclerView() {
        if(mUsersRecyclerView.getAdapter().getItemCount() == 0) {
            mNoUserLabel.setVisibility(View.VISIBLE);
            mUsersRecyclerView.setVisibility(View.GONE);
        } else {
            mNoUserLabel.setVisibility(View.GONE);
            mUsersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void initializeRecyclerView() {
        mAdapter = new LocalUsersRecyclerAdapter();
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mUsersRecyclerView.setAdapter(mAdapter);
    }
}
