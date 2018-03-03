package com.example.namiq.egisterpp.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.namiq.egisterpp.R;
/**
 * Created by Namiq on 2/23/2017.
 * This activity is for show image from chat
 */
public class ImageViewer extends AppCompatActivity {
    String name;
    String image;
    String mainImg;
    Toolbar topToolBar;
    ImageView profileImage;
    TextView userName;
    ImageView mainImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        topToolBar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(topToolBar);
        mainImage=(ImageView)findViewById(R.id.mainImage);
        profileImage = (ImageView) findViewById(R.id.profile_image);
        userName = (TextView) findViewById(R.id.username);
        name=getIntent().getStringExtra("name");
        image=getIntent().getStringExtra("image");
        mainImg=getIntent().getStringExtra("mainimage");
        Glide.with(getApplicationContext()).load(image).into(profileImage);
        Glide.with(getApplicationContext()).load(mainImg).into(mainImage);
        userName.setText(name);
    }

    public void goBack(View view) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}
