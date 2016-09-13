package eg.alexu.eng.mobdev.bustourdriverside.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import eg.alexu.eng.mobdev.bustourdriverside.R;
import eg.alexu.eng.mobdev.bustourdriverside.activities.activity.AddingUser;

public class AddingUserManually extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_user_manually);
    }
    public void onClickDoneNewUserManually(View v) {
        Intent i = new Intent(AddingUserManually.this, AddingUser.class);
        startActivity(i);
    }
}
