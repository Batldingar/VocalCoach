package com.baldware.gesangstraining.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.baldware.gesangstraining.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.layout_home);

        //set the onClickListener for the layout in order to move to the comparison layout
        ConstraintLayout errordetectionLayout = findViewById(R.id.constraint_layout_error_detection);
        errordetectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ErrorDetectionActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });

        //set the onClickListener for the layout in order to move to the selection layout
        ConstraintLayout selectionLayout = findViewById(R.id.constraint_layout_selection);
        selectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, SelectionActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });


        //set the onClickListener for the layout in order to move to the error detection layout

        ConstraintLayout visualisingLayout = findViewById(R.id.constraint_layout_visualising);
        visualisingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, VisualisingActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });

        ConstraintLayout creditsLayout = findViewById(R.id.constraint_layout_credits);
        creditsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, CreditsActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });
    }
}



