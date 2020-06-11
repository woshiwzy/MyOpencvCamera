package com.demo.cv42;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class EntryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
    }


    public void onClickBack(View view) {
        startActivity(new Intent(this, CameraActivity.class));
    }


    public void onClickBackLand(View view) {
        startActivity(new Intent(this, BackCameraActivity.class));
    }

    public void onClickSwitch(View view) {

    }

    public void onClickFrontCamera(View view) {
        startActivity(new Intent(this, FrontCameraLandActivity.class));
    }

    public void onClickPortriatCamera(View view) {
        startActivity(new Intent(this, FrontCameraPortraitActivity.class));
    }
    public void onClickPortriatFullCamera(View view) {
        startActivity(new Intent(this, FrontCameraPortraitFullScreenActivity.class));
    }

    public void onClickCustomCamera(View view) {
        startActivity(new Intent(this, CustomCameraActivity.class));
    }
}
