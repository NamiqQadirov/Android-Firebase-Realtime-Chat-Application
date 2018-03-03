package com.example.namiq.egisterpp.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.namiq.egisterpp.R;
import com.example.namiq.egisterpp.dbo.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    EditText email, username, password;
    TextView status;
    Button signUp;
    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri filePath;
    private String url;
    String name, mail;
    private AlertDialog dialog;
    //creating reference to firebase storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://firesoft-d3cef.appspot.com");    //change the url according to your firebase app
    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        status = (TextView) findViewById(R.id.status);
        signUp = (Button) findViewById(R.id.signUp);
        username = (EditText) findViewById(R.id.name);
        userDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void signUp(View view) {
        String email2 = email.getText().toString().trim();
        String password2 = password.getText().toString().trim();
        this.mail = email2;
        this.name = username.getText().toString().trim();
        if (TextUtils.isEmpty(email2)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password2)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        //create user
        showAlertDialog("Registering...", true);
        auth.createUserWithEmailAndPassword(email2, password2)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(getApplicationContext(), "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        dismissAlertDialog();
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), name + "  have created", Toast.LENGTH_SHORT).show();
                            user = task.getResult().getUser();
                            uploadImage();
                        }
                    }
                });
    }

    public void selectImage(View view) {
        showFileChooser();
    }

    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
        }
    }

    public void uploadImage() {
        if (filePath != null) {


            StorageReference childRef = storageRef.child("profiles/" + System.currentTimeMillis() + "." + getFileExtension(filePath));
            //uploading the image
            UploadTask uploadTask = childRef.putFile(filePath);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    url = taskSnapshot.getDownloadUrl().toString();
                    Toast.makeText(getApplicationContext(), "Upload successful" + url, Toast.LENGTH_SHORT).show();
                    createUserInDatabase();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext(), "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            filePath = Uri.parse("android.resource://com.example.namiq.egisterpp/drawable/image_profile");

            StorageReference childRef = storageRef.child("profiles/" + System.currentTimeMillis() + "." + getFileExtension(filePath));
            //uploading the image
            UploadTask uploadTask = childRef.putFile(filePath);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    url = taskSnapshot.getDownloadUrl().toString();
                    Toast.makeText(getApplicationContext(), "Upload successful" + url, Toast.LENGTH_SHORT).show();
                    createUserInDatabase();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext(), "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                }
            });
            Toast.makeText(getApplicationContext(), "Select an image", Toast.LENGTH_SHORT).show();
        }
    }

    //Create user profile
    public void createUserInDatabase() {
        String mail = cleanEmailAddress(this.mail);  //clean dots in user email
        User user = new User(name, mail, url, "online",System.currentTimeMillis() / 1000L,"empty");
        userDatabase.child("users").child(mail).setValue(user,  new DatabaseReference.CompletionListener() {
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void showAlertDialog(String message, boolean isCancelable) {

        dialog = buildAlertDialog(getString(R.string.login_error_title), message, isCancelable, RegisterActivity.this);
        dialog.show();
    }

    public static AlertDialog buildAlertDialog(String title, String message, boolean isCancelable, Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title);

        if (isCancelable) {
            builder.setPositiveButton(android.R.string.ok, null);
        } else {
            builder.setCancelable(false);
        }
        return builder.create();
    }

    private void dismissAlertDialog() {
        dialog.dismiss();
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //firebase not allow dot
    private String cleanEmailAddress(String email) {
        return email.replace(".", "-");
    }

    public void signIn(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
