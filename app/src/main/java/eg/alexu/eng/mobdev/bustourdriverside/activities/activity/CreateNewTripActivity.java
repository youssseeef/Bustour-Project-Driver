package eg.alexu.eng.mobdev.bustourdriverside.activities.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Extras;

public class CreateNewTripActivity extends AppCompatActivity {

    private EditText mTripName;
    private EditText mTripDate;
    private EditText mTripDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_trip);
        mTripName = (EditText) findViewById(R.id.add_trip_name);
        mTripDate = (EditText) findViewById(R.id.add_trip_date);
        mTripDescription = (EditText) findViewById(R.id.add_trip_description);
    }

    public void onClickDoneNewTrip(View v) {
        boolean wrongEntry = false;

        if (mTripName.getText().toString().trim().equals("")) {
            mTripName.setError("Please put some name for the trip!");
            wrongEntry = true;
        }

        if (mTripDescription.getText().toString().trim().equals("")) {
            mTripDescription.setError("Please put a description for the trip! ");
            wrongEntry = true;
        }

        if (mTripDate.getText().toString().trim().equals("")) {
            mTripDate.setError("Please put a description for the trip! ");
            wrongEntry = true;
        }

        if (!wrongEntry) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            Map<String, Object> m1 = new HashMap<String, Object>();
            Map<String, String> m2 = new HashMap<String, String>();
            m2.put(Constants.NAME, mTripName.getText().toString().trim());
            m2.put(Constants.DESCRIPTION, mTripDescription.getText().toString().trim());
            m2.put(Constants.LOC_X, "0.0");
            m2.put(Constants.LOC_Y, "0.0");
            m2.put(Constants.ENABLE_TRACKING, "false");
            long uniqueVar = System.currentTimeMillis();
            String tripId = "trip" + uniqueVar;
            m1.put(tripId, m2);
            dbRef.child(Constants.DRIVERS).
                    child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                    child(Constants.TRIPS).
                    updateChildren(m1);
            Intent i = new Intent(CreateNewTripActivity.this, AddingUser.class);
            i.putExtra(Extras.TRIP_ID, tripId);
            startActivity(i);
            finish();
        }
    }
}
