package eg.alexu.eng.mobdev.bustourdriverside.activities.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.adapters.AddingUserRecyclerAdapter;
import eg.alexu.eng.mobdev.bustourdriverside.activities.adapters.LocalUsersRecyclerAdapter;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Extras;

public class AddingUser extends AppCompatActivity {

    private RecyclerView mAddUserRecyclerView;
    private AddingUserRecyclerAdapter mAdapter;
    private EditText mSearchEditText;
    private ImageView mSearchImageView;
    private TextView mNoResultFound;
    private String mTripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_user);
        mTripId = getIntent().getStringExtra(Extras.TRIP_ID);
        initializeFields();
        initializeRecyclerView();
    }

    private void initializeFields() {
        mAddUserRecyclerView = (RecyclerView) findViewById(R.id.search_user_recycler_view);
        mSearchEditText = (EditText) findViewById(R.id.search_user_edit_text);
        mSearchImageView = (ImageView) findViewById(R.id.search_user_image_view);
        mNoResultFound = (TextView) findViewById(R.id.no_result_label);
        addListenerForSearchUser();
    }

    private void addListenerForSearchUser() {
        mSearchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchEditText.getText().toString().trim().equals(""))
                    mSearchEditText.setError("There is no text");
                else {
                    String searchQuery = mSearchEditText.getText().toString();
                    searchQuery = searchQuery.replace(" ", "");
                    searchQuery = searchQuery.toLowerCase();
                    Log.d("Search_Query", searchQuery);
                    DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
                    dbReference.child(Constants.ALL_USERS_IN_DB)
                            .child(searchQuery)
                            .child(Constants.USERS)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                        matchUsers(dataSnapshot);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        });
    }

    private void matchUsers(DataSnapshot dataSnapshot) {
        HashMap<String, String> users = (HashMap<String, String>) dataSnapshot.getValue();
        List<String> tempList = new ArrayList<String>();
        if (users != null) {
            for (String key : users.keySet()) {
                tempList.add(key);
            }
            mAdapter.updateList(tempList, mTripId);
        } else {
            mAdapter.clearList();
        }
        if(mAddUserRecyclerView.getAdapter().getItemCount() == 0) {
            mNoResultFound.setVisibility(View.VISIBLE);
            mAddUserRecyclerView.setVisibility(View.GONE);
        } else {
            mNoResultFound.setVisibility(View.GONE);
            mAddUserRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void initializeRecyclerView() {
        mAdapter = new AddingUserRecyclerAdapter();
        mAddUserRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAddUserRecyclerView.setAdapter(mAdapter);
    }
}
