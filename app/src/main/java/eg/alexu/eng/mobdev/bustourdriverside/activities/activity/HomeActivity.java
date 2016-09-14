package eg.alexu.eng.mobdev.bustourdriverside.activities.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import eg.alexu.eng.mobdev.bustourdriverside.R;

public class HomeActivity extends AppCompatActivity {

    public static Intent newIntent(AppCompatActivity callerActivity) {
        return new Intent(callerActivity, HomeActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
    }

    public void onClickTrip(View v) {
        Intent i = new Intent(HomeActivity.this, TripMenuActivity.class);
        startActivity(i);
    }

    public void onClickProfile(View v) {
        Intent i = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(i);
    }

}
