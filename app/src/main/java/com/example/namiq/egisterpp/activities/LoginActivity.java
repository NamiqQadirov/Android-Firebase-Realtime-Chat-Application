package com.example.namiq.egisterpp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.namiq.egisterpp.R;
import com.example.namiq.egisterpp.dbo.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Namiq on 2/28/2017.
 * This activity is for login user
 */
public class LoginActivity extends AppCompatActivity {
    Button signIn, signUp;
    TextView email, password;
    private FirebaseAuth firebaseAuth;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signIn = (Button) findViewById(R.id.signIn);
        signUp = (Button) findViewById(R.id.signUp);
        email = (TextView) findViewById(R.id.email);
        password = (TextView) findViewById(R.id.password);
        firebaseAuth = FirebaseAuth.getInstance();

    }

    //if sign in button pressed this method checks network and email and
    public void signIn(View view) {
        String email = this.email.getText().toString().trim();
        String password = this.password.getText().toString().trim();
        if (email.equals("") || password.equals("")) {
            alertDialogCreate("Aproblem has occured", "Empty email or password", false);
        } else {
            if (isNetworkAvailable()) {
                logInWithEmailandPassword(email, password);
            } else {

            }
        }


    }

    public void logInWithEmailandPassword(String email, String password) {
        alertDialogCreate("Logging in", "please wait", false);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                alertDialogDismiss();
                if (task.isSuccessful()) {
                    setUserOnline();
                    startMainActivity();
                } else {

                }
            }
        });
    }

    public void signUp(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }

    //Check network status
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void alertDialogCreate(String title, String mes, boolean isCancelable) {

        dialog = new AlertDialog.Builder(LoginActivity.this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(title)
                .setMessage(mes)
                .setCancelable(isCancelable)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void alertDialogDismiss() {
        dialog.dismiss();
    }
//change user status to online if login is succesful
    public void setUserOnline() {
        if (firebaseAuth.getCurrentUser() != null) {
            String userEmail = cleanEmailAddress(firebaseAuth.getCurrentUser().getEmail());
            Toast.makeText(this, userEmail, Toast.LENGTH_SHORT).show();
            FirebaseDatabase.getInstance()
                    .getReference().
                    child("users").
                    child(userEmail).
                    child("status").
                    setValue(Constants.ONLINE);
        }
    }

    public void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    //firebase not allow dot
    private String cleanEmailAddress(String email) {
        return email.replace(".", "-");
    }
}

