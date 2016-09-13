package eg.alexu.eng.mobdev.bustourdriverside.activities.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.model.Model;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Extras;

/**
 * Created by Paula B. Bassily on 05/09/2016.
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText busNumberEditText;
    private FirebaseAuth fba;
    private DatabaseReference dbRef;
    private boolean isNewUser;
    private ImageView userPhoto;
    private final String TAG = "ProfileActivity";

    public static Intent newIntent(AppCompatActivity currAc) {
        Intent intent = new Intent(currAc, ProfileActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initializeProfileActivity();
        addListenerToProfileData();
    }

    private void addListenerToProfileData() {
        if (!isNewUser) {
            dbRef.child(Constants.DRIVERS).
                    child(fba.getCurrentUser().getUid()).
                    addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.child(Constants.NAME).getValue(String.class);
                            String phone = dataSnapshot.child(Constants.PHONE).getValue(String.class);
                            String busNumber = dataSnapshot.child(Constants.BUS_NUMBER).getValue(String.class);
                            setProfile(name, phone, busNumber);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, databaseError.getMessage());
                        }
                    });
        }
    }

    private void setProfile(String fullName, String phone, String busNumber) {
        nameEditText.setText(fullName);
        phoneEditText.setText(phone);
        busNumberEditText.setText(busNumber);
    }

    public void onClickSave(View v) {
        boolean wrongEntry = false;
        if(nameEditText.getText().toString().trim().equals("")){
            nameEditText.setError("Required!");
            wrongEntry = true;
        }

        if(busNumberEditText.getText().toString().trim().equals("")){
            busNumberEditText.setError("Required!");
            wrongEntry = true;
        }

        if(phoneEditText.getText().toString().trim().length() < 10){
            phoneEditText.setError("Enter a valid phone!");
            wrongEntry = true;
        }

        if(!wrongEntry) {
            Model.getInstance().submitDataFirstTime(fba.getCurrentUser().getUid(),
                    nameEditText.getText().toString(),
                    phoneEditText.getText().toString(),
                    busNumberEditText.getText().toString(),
                    isNewUser);
            startActivity(HomeActivity.newIntent(this));
            finish();
        }
    }

    private void initializeProfileActivity() {
        userPhoto = (ImageView) findViewById(R.id.user_photo_imageView_profile);
        nameEditText = (EditText) findViewById(R.id.profile_name_edittext);
        phoneEditText = (EditText) findViewById(R.id.profile_phone_edittext);
        busNumberEditText = (EditText) findViewById(R.id.profile_bus_number_edittext);
        Firebase.setAndroidContext(this);
        if (Firebase.getDefaultConfig().isPersistenceEnabled() == false) {
            Firebase.getDefaultConfig().setPersistenceEnabled(true);
        }
        Glide.with(userPhoto.getContext())
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .centerCrop()
                .override(500, 500).
                into(new BitmapImageViewTarget(userPhoto) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(userPhoto.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        userPhoto.setImageDrawable(circularBitmapDrawable);
                    }
                });
        fba = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        isNewUser = getIntent().getBooleanExtra(Extras.IS_NEW_USER, false);
        if (!fba.getCurrentUser().getDisplayName().equals("") ||
                fba.getCurrentUser().getDisplayName() != null) {
            nameEditText.setText(fba.getCurrentUser().getDisplayName());
        }
    }


}
