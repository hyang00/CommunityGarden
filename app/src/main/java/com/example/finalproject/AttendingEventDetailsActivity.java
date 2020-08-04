package com.example.finalproject;

import android.view.View;

public class AttendingEventDetailsActivity extends EventDetailsActivity {
    @Override
    protected void setUpRegistrationButton() {
        fab.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
        fab.setText("Cancel Registration");
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseClient.cancelUserRegistration(event, AttendingEventDetailsActivity.this);
            }
        });
    }
}
