package eg.alexu.eng.mobdev.bustourdriverside.activities.model;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;


public class Model {

    private DatabaseReference dbRef;
    private boolean newUser;
    private static Model modelInstance;
    private final String TAG = ".model.Model";

    private Model() {
        newUser = false;
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public static Model getInstance() {
        if (modelInstance == null)
            modelInstance = new Model();
        return modelInstance;
    }


    public void isANewUser(String userId) {
        dbRef.child(Constants.DRIVERS).child(userId).
                addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            newUser = true;
                        else
                            newUser = false;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, databaseError.getMessage());
                    }
                });
    }

    public boolean isNewUser () {
        return newUser;
    }

    public void submitDataFirstTime(String userId, String name, String phone, String busNum, boolean newUser) {
        Uri url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        String urlString = url != null ? url.toString() : "error";
        HashMap<String, Object> temp = new HashMap<>();
        temp.put(Constants.NAME, name);
        temp.put(Constants.PHONE, phone);
        temp.put(Constants.USER_PHOTO, urlString);
        if (newUser) {
            dbRef.child(Constants.DRIVERS).child(userId).setValue(temp);
        } else {
            dbRef.child(Constants.DRIVERS).child(userId).updateChildren(temp);
        }
    }


}
