package com.example.namiq.egisterpp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.namiq.egisterpp.R;
import com.example.namiq.egisterpp.services.BackgroundService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.namiq.egisterpp.adapter.UsersChatAdapter;
import com.example.namiq.egisterpp.dbo.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Namiq
 */

public class MainActivity extends AppCompatActivity {


    private static String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.progress_bar_users)
    ProgressBar mProgressBarForUsers;
    @BindView(R.id.recycler_view_users)
    RecyclerView mUsersRecyclerView;

    private String mCurrentUserUid;
    private List<String> mUsersKeyList;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserRefDatabase;
    private ChildEventListener mChildEventListener;
    private UsersChatAdapter mUsersChatAdapter;
    //Toolbar configuration
    Toolbar topToolBar;
    ImageView profileImage;
    TextView userName;
    private boolean outAsLogout = false;
    private boolean outForLogin = false;
    private boolean outForProfile = false;
    private boolean outAsRefresh = false;
    private boolean outAsSettings = false;
    private boolean isReturnedFromChat = false;//Check if activity returns or begins from zero
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBarForUsers = (ProgressBar) findViewById(R.id.progress_bar_users);
        mUsersRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_users);
        profileImage = (ImageView) findViewById(R.id.profile_image);
        userName = (TextView) findViewById(R.id.username);
        bindButterKnife();
        setAuthInstance();
        setUsersDatabase();
        setUserRecyclerView();
        setUsersKeyList();
        setAuthListener();
        setUserOnline();
        setTitle(null);
        topToolBar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(topToolBar);
        stopService(new Intent(this, BackgroundService.class));
        profileImage.setOnClickListener(new View.OnClickListener() {//if profile image clicked go to main user profile
            public void onClick(View v) {
                goToMainProfile();
            }
        });
    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    private void setAuthInstance() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setUsersDatabase() {
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference().child("users");
    }

    private void setUserRecyclerView() {
        mUsersChatAdapter = new UsersChatAdapter(this, new ArrayList<User>());
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUsersRecyclerView.setHasFixedSize(true);
        mUsersRecyclerView.setAdapter(mUsersChatAdapter);
    }

    private void setUsersKeyList() {
        mUsersKeyList = new ArrayList<String>();
    }

    private void setAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                hideProgressBarForUsers();
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    setUserData(user);
                    queryAllUsers();
                } else {
                    // User is signed out
                    outForLogin = true;
                    goToLogin();
                }
            }
        };
    }

    private void setUserData(FirebaseUser user) {
        mCurrentUserUid = cleanEmailAddress(user.getEmail());
    }

    private void queryAllUsers() {
        mChildEventListener = getChildEventListener();
        mUserRefDatabase.limitToFirst(50).addChildEventListener(mChildEventListener);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // LoginActivity is a New Task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // The old task when coming back to this activity should be cleared so we cannot come back to it.
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        isReturnedFromChat = true;
        // Toast.makeText(this, ""+isReturnedFromChat, Toast.LENGTH_SHORT).show();
        showProgressBarForUsers();
        mAuth.addAuthStateListener(mAuthListener);
        UsersChatAdapter.onStopStatus = false;
        stopService(new Intent(this, BackgroundService.class));//Stop database change listening service
    }

    @Override
    public void onStop() {
        super.onStop();
        clearCurrentUsers();
        if (mChildEventListener != null) {
            mUserRefDatabase.removeEventListener(mChildEventListener);
        }
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        //check onstop is for chatactivity or for home button if home button have pressed start service
        if (!UsersChatAdapter.onStopStatus && !outForLogin && !outForProfile && !outAsLogout && !outAsRefresh && !outAsSettings && !isReturnedFromChat) {
            Toast.makeText(this, "onstop entered", Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (sharedPrefs.getBoolean("perform_sync", false)) {
                Intent i = new Intent(this, BackgroundService.class);
                i.putExtra("username", mCurrentUserUid);
                this.startService(i);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAuth.getCurrentUser() != null) {
            String userId = cleanEmailAddress(mAuth.getCurrentUser().getEmail());
            mUserRefDatabase.child(userId).child("status").setValue(UsersChatAdapter.OFFLINE);
        }
        if (!outAsLogout && !outForLogin && !outForProfile && !outAsRefresh && !outAsSettings) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            // Toast.makeText(this, "" + sharedPrefs.getBoolean("perform_sync", false), Toast.LENGTH_SHORT).show();
            if (sharedPrefs.getBoolean("perform_sync", false)) {
                Intent i = new Intent(this, BackgroundService.class);
                i.putExtra("username", mCurrentUserUid);
                this.startService(i);
            }
        } else {

        }

    }

    private String cleanEmailAddress(String email) {
        return email.replace(".", "-");
    }

    private void clearCurrentUsers() {
        mUsersChatAdapter.clear();
        mUsersKeyList.clear();
    }

    private void logout() {
        showProgressBarForUsers();
        setUserOffline();
        mAuth.signOut();
    }

    private void setUserOffline() {
        if (mAuth.getCurrentUser() != null) {
            String userId = cleanEmailAddress(mAuth.getCurrentUser().getEmail());
            mUserRefDatabase.child(userId).child("status").setValue(UsersChatAdapter.OFFLINE);
        }
    }

    private void setUserOnline() {
        if (mAuth.getCurrentUser() != null) {
            String userId = cleanEmailAddress(mAuth.getCurrentUser().getEmail());
            mUserRefDatabase.child(userId).child("status").setValue(UsersChatAdapter.ONLINE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            if (isNetworkAvailable()) {
                outAsLogout = true;
                logout();
                return true;
            } else {
                Toast.makeText(this, "Not have connection", Toast.LENGTH_SHORT).show();
            }
        }

        if (id == R.id.profile) {
            goToMainProfile();
            return true;
        }
        if (id == R.id.action_refresh) {
            if (isNetworkAvailable()) {
                outAsRefresh = true;
                // Toast.makeText(MainActivity.this, "Refresh App", Toast.LENGTH_LONG).show();
                finish();
                startActivity(getIntent());


            } else {
                Toast.makeText(this, "Not have connection", Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.action_settings) {

            outAsSettings = true;
            Intent modifySettings = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(modifySettings);

        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgressBarForUsers() {
        mProgressBarForUsers.setVisibility(View.VISIBLE);
    }

    private void hideProgressBarForUsers() {
        if (mProgressBarForUsers.getVisibility() == View.VISIBLE) {
            mProgressBarForUsers.setVisibility(View.GONE);
        }
    }

    private ChildEventListener getChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()) {

                    String userUid = dataSnapshot.getKey();
                    // Toast.makeText(MainActivity.this, userData.getEmail(), Toast.LENGTH_SHORT).show();

                    if (dataSnapshot.getKey().equals(mCurrentUserUid)) {
                        currentUser = dataSnapshot.getValue(User.class);
                        Glide.with(getApplicationContext()).load(currentUser.getUrl()).into(profileImage);
                        mUsersChatAdapter.setCurrentUser(currentUser);
                        userName.setText(currentUser.getName());
                    } else {
                        User recipient = dataSnapshot.getValue(User.class);
                        //  Toast.makeText(getApplicationContext(), recipient.getEmail() + "came", Toast.LENGTH_SHORT).show();
                        // recipient.setRecipientId(userUid);
                        mUsersKeyList.add(userUid);
                        mUsersChatAdapter.refill(recipient);
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    String userUid = dataSnapshot.getKey();
                    if (!userUid.equals(mCurrentUserUid)) {

                        User user = dataSnapshot.getValue(User.class);
                        //Toast.makeText(getApplicationContext(), user.getEmail() + user.getConnection(), Toast.LENGTH_SHORT).show();

                        int index = mUsersKeyList.indexOf(userUid);
                        if (index > -1) {
                            mUsersChatAdapter.changeUser(index, user);
                        }
                    }

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    //Check network status
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void goToMainProfile() {
        outForProfile = true;
        Intent i = new Intent(this, MainProfileActivity.class);
        if (currentUser != null) {
            i.putExtra("userName", currentUser.getName());
            i.putExtra("userEmail", currentUser.getEmail());
            i.putExtra("userImage", currentUser.getUrl());
            i.putExtra("userCreateDate", currentUser.getCreateTime());
            i.putExtra("userDesc", currentUser.getDescription());
            startActivity(i);
            finish();
        } else {
            Toast.makeText(this, "You have no internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

}
