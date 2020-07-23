package com.example.finalproject;

import android.view.View;

public class AttendingEventDetailsActivity extends EventDetailsActivity {
    @Override
    protected void setUpRegistrationButton() {
        fab.setText("Cancel Registration");
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseClient.cancelUserRegistration(event, AttendingEventDetailsActivity.this);
            }
        });
    }
}
