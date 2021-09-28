package com.teyouale.objectdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;

public class Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        TextView earn = findViewById(R.id.earn);
        String earnPoint = String.format("%01d", new Random().nextInt(10));
        earn.setText("You Earn "+ earnPoint + " Star");
    }
}