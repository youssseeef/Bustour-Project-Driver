package eg.alexu.eng.mobdev.bustourdriverside.activities.activity;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import eg.alexu.eng.mobdev.bustourdriverside.activities.model.DriverTripsModel;
import eg.alexu.eng.mobdev.bustourdriverside.activities.model.Model;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Extras;

public class AllUsersLocationOnMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String mTripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_all_users_location_on_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mTripId = getIntent().getStringExtra(Extras.TRIP_ID);

        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.DRIVERS).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(Constants.TRIPS).
                child(mTripId).
                child(Constants.USER_IN_TRIP).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                        for (final String user : map.keySet()) {
                            dbRef.child(Constants.USERS)
                                    .child(user)
                                    .child(Constants.TRIPS)
                                    .child(mTripId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final LatLng pos = new LatLng(Double.parseDouble(dataSnapshot.child(Constants.LOC_X).getValue(String.class)),
                                            Double.parseDouble(dataSnapshot.child(Constants.LOC_Y).getValue(String.class)));
                                    if(pos.latitude != 0 && pos.longitude != 0) {
                                        dbRef.child(Constants.USERS).
                                                child(user).
                                                child(Constants.NAME).
                                                addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        mMap.addMarker(new MarkerOptions().position(pos).title(dataSnapshot.getValue(String.class) + "'s pickup location"));
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        dbRef.child(Constants.DRIVERS).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(Constants.TRIPS).
                child(mTripId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatLng pos = new LatLng(Double.parseDouble(dataSnapshot.child(Constants.LOC_X).getValue(String.class)),
                        Double.parseDouble(dataSnapshot.child(Constants.LOC_Y).getValue(String.class)));
                if(pos.latitude != 0 && pos.longitude != 0) {
                    mMap.addMarker(new MarkerOptions().position(pos).title("Driver Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
