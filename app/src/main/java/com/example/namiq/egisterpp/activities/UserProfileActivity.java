package com.example.namiq.egisterpp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.namiq.egisterpp.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private DatabaseReference mUserRefDatabase;
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsing;
    private ImageView coverImage;
    private FrameLayout framelayoutTitle;
    private LinearLayout linearlayoutTitle;
    private Toolbar toolbar;
    private TextView textviewTitle;
    private SimpleDraweeView avatar;
    private TextView userDescription;
    private TextView mainEmail;
    private TextView mainName;
    private TextView userEmail;
    private EditText editDesc;
    private String name;
    private String email;
    private String desc;
    private String imageUrl;
    private String date;
    private String description;
    MenuItem editMenu;
    private boolean editStatus;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2016-03-03 11:32:38 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        collapsing = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        coverImage = (ImageView) findViewById(R.id.imageview_placeholder);
        framelayoutTitle = (FrameLayout) findViewById(R.id.framelayout_title);
        linearlayoutTitle = (LinearLayout) findViewById(R.id.linearlayout_title);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        textviewTitle = (TextView) findViewById(R.id.textview_title);
        avatar = (SimpleDraweeView) findViewById(R.id.avatar);
        userDescription = (TextView) findViewById(R.id.desc_text);
        userEmail = (TextView) findViewById(R.id.email_text);
        mainEmail = (TextView) findViewById(R.id.mainEmail);
        mainName = (TextView) findViewById(R.id.name);
        editDesc = (EditText) findViewById(R.id.desc_edit);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        name = i.getStringExtra("userName");
        email = i.getStringExtra("userEmail");
        desc = i.getStringExtra("userDesc");
        imageUrl = i.getStringExtra("userImage");
        date = i.getStringExtra("userCreateDate");
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
        Fresco.initialize(this);
        setContentView(R.layout.activity_user_profile);
        findViews();

        toolbar.setTitle("");
        appbar.addOnOffsetChangedListener(this);

        setSupportActionBar(toolbar);
        startAlphaAnimation(textviewTitle, 0, View.INVISIBLE);

        //set avatar and cover
        avatar.setImageURI(Uri.parse(imageUrl));
        coverImage.setImageResource(R.drawable.background);
        if (!desc.equals("empty")) {
            userDescription.setText(desc);
        }
        userEmail.setText(email);
        textviewTitle.setText(name);
        mainName.setText(name);
        mainEmail.setText(email);
        editDesc.setVerticalScrollBarEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_profile, menu);
        editMenu = menu.findItem(R.id.edit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(textviewTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(textviewTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(linearlayoutTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(linearlayoutTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }


}

