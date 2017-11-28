package com.example.bounceprogressbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private BounceProgressBar qp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        qp = (BounceProgressBar) findViewById(R.id.qp);
        qp.startTotalAnimations();
    }
}
