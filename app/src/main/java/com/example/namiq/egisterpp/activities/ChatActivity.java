package com.example.namiq.egisterpp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.namiq.egisterpp.R;
import com.example.namiq.egisterpp.adapter.MyAdapter;
import com.example.namiq.egisterpp.adapter.UsersChatAdapter;
import com.example.namiq.egisterpp.boommenu.BuilderManager;
import com.example.namiq.egisterpp.dbo.Constants;
import com.example.namiq.egisterpp.dbo.Message;
import com.example.namiq.egisterpp.dbo.UnreadedMessages;
import com.example.namiq.egisterpp.dbo.User;
import com.example.namiq.egisterpp.services.BackgroundService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.OnBoomListenerAdapter;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener /*  implementing click listener */ {
    //constant to track image chooser intent
    private static final int PICK_IMAGE_REQUEST = 234;
    private static final int CAMERA_REQUEST = 235;

    //view objects
    private Button buttonChoose;
    private EditText editTextName;
    //recyclerview object
    private RecyclerView recyclerView;

    //adapter object
    private RecyclerView.Adapter adapter;
    private List<Message> message;

    //uri to store file
    private Uri filePath;
    private BoomMenuButton bmb;

    //firebase objects
    private StorageReference storageReference;
    private DatabaseReference mDatabase;
    private DatabaseReference userDatabase;
    private DatabaseReference userDatabaseForStatus;
    private ChildEventListener mChildEventListener;
    private String sender;
    private String receiver;
    private String chatRoomName;
    private String name;
    private String receiverImage;
    private String senderImage;
    private String isOnline;
    private String createTime;
    private String description;
    Toolbar topToolBar;
    ImageView profileImage;
    TextView userName;
    TextView status;
    //check why onstop worked.
    private boolean isBackPressed;
    private boolean isProfilePressed;
    private boolean isSettingsPressed;
    private boolean isHomeMenuPressed;

    /*
    *Initialize datas and get messages from firebase database
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        topToolBar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(topToolBar);
        profileImage = (ImageView) findViewById(R.id.profile_image);
        userName = (TextView) findViewById(R.id.username);
        status = (TextView) findViewById(R.id.status);
        Intent intent = getIntent();
        sender = intent.getStringExtra("sender");
        receiver = intent.getStringExtra("recipent");
        chatRoomName = intent.getStringExtra("chatroomname");
        name = intent.getStringExtra("name");
        receiverImage = intent.getStringExtra("receiverimage");
        senderImage = intent.getStringExtra("senderimage");
        isOnline = intent.getStringExtra("isonline");
        createTime = intent.getStringExtra("createtime");
        description = intent.getStringExtra("description");
        userName.setText(name);
        Glide.with(getApplicationContext()).load(receiverImage).into(profileImage);
        setUpStatus();
        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        editTextName = (EditText) findViewById(R.id.editText);

        storageReference = FirebaseStorage.getInstance().getReference();

        mDatabase = FirebaseDatabase.getInstance().getReference("chatrooms");


        message = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference("chatrooms/" + chatRoomName);
        userDatabase = FirebaseDatabase.getInstance().getReference("unreaded/" + receiver);
        userDatabaseForStatus = FirebaseDatabase.getInstance().getReference().child("users");
        mChildEventListener = getChildEventListener();
        userDatabaseForStatus.addChildEventListener(mChildEventListener);
        //adding an event listener to fetch values
        mDatabase.limitToLast(20).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //dismissing the progress dialog
                message.clear();

                //iterating through all the values in database
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Message upload = postSnapshot.getValue(Message.class);
                    message.add(upload);
                }
                //creating adapter
                adapter = new MyAdapter(ChatActivity.this, message, sender, name, receiverImage, senderImage);
                // recyclerView.smoothScrollToPosition(12);
                // adding adapter to recyclerview
                // recyclerView.setAdapter(adapter);
                // recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()-1);
                recyclerView.setAdapter(adapter);// set adapter on recyclerview
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1); //use to focus the item with index
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        buttonChoose.setOnClickListener(this);
        setBoomMenu();
        userName.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToMainProfile();
            }
        });
    }

    //if send message button clicked send text message
    @Override
    public void onClick(View v) {
        if (v == buttonChoose) {
            String senderMessage = editTextName.getText().toString().trim();
            if (!senderMessage.isEmpty()) {
                sendTextMessage(senderMessage, "text");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        stopService(new Intent(this, BackgroundService.class));//Stop database change listening service
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!isProfilePressed && !isHomeMenuPressed && !isBackPressed && !isSettingsPressed) {
            //check onstop is for chatactivity or for home button if home button have pressed start service
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (sharedPrefs.getBoolean("perform_sync", false)) {
                Intent i = new Intent(this, BackgroundService.class);
                i.putExtra("username", sender);
                this.startService(i);
            }
        }
    }


    //send different type of messages
    private void sendTextMessage(String msg, String type) {
        Message upload = new Message(" ", msg, sender, receiver, type);
        //adding an upload to firebase database
        String uploadId = mDatabase.push().getKey();
        mDatabase.child(uploadId).setValue(upload);
        editTextName.setText("");
        if (!isOnline.equals("online")) {
            String id = mDatabase.push().getKey();
            UnreadedMessages unreadedMessages = new UnreadedMessages(msg, "text");
            userDatabase.child(sender).child(id).setValue(unreadedMessages);
        }

    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //take image from camera
    private void takeImage() {
        Toast.makeText(this, "burda", Toast.LENGTH_SHORT).show();
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    /*
    Initialize boom menu with builder.
     */
    private void setBoomMenu() {
        bmb = (BoomMenuButton) findViewById(R.id.bmb);
        assert bmb != null;
        bmb.setButtonEnum(ButtonEnum.SimpleCircle);
        bmb.setPiecePlaceEnum(PiecePlaceEnum.DOT_4_1);
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.SC_4_1);
        for (int i = 0; i < bmb.getPiecePlaceEnum().pieceNumber(); i++) addBuilder();
    }

    private void addBuilder() {
        bmb.addBuilder(new SimpleCircleButton.Builder()
                .normalImageRes(BuilderManager.getImageResource())
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Toast.makeText(getApplicationContext(), "No." + index + " boom-button is clicked!", Toast.LENGTH_SHORT).show();
                        if (index == 1) {
                            showFileChooser();
                        } else if (index == 0) {
                            takeImage();
                        } else if (index == 3) {
                            Intent i = new Intent(ChatActivity.this, MapActivity.class);
                            startActivityForResult(i, 236);
                        } else if (index == 2) {
                            Intent i = new Intent(ChatActivity.this, VoiceRecorder.class);
                            startActivityForResult(i, 237);
                        }
                    }
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            uploadFile();
        }
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "onactivityresult", Toast.LENGTH_SHORT).show();
            filePath = data.getData();
            uploadCameraFile(data);
        }
        if (requestCode == 236 && resultCode == RESULT_OK) {
            String coordinates = data.getStringExtra("loc");
            Toast.makeText(this, coordinates, Toast.LENGTH_SHORT).show();
            sendTextMessage(coordinates, "loc");
        }
        if (requestCode == 237 && resultCode == RESULT_OK) {
            String voiceUrl = data.getStringExtra("voice");
            Toast.makeText(this, voiceUrl, Toast.LENGTH_SHORT).show();
            sendTextMessage(voiceUrl, "voice");
        }
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //upload file whch taken from camera
    private void uploadCameraFile(Intent data) {
        //displaying progress dialog while image is uploading
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();

        //getting the storage reference
        StorageReference sRef = storageReference.child(Constants.FILEUPLOADPATH + System.currentTimeMillis());


        Bundle extras = data.getExtras();
        Bitmap bmp = (Bitmap) extras.get("data");

        //Change to bitmap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();


        //ve resmi sisteme gönderiyoruz
        UploadTask upload = sRef.putBytes(datas);
        upload.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                //Get upload datas as message
                @SuppressWarnings("VisibleForTests")
                Message upload = new Message(editTextName.getText().toString().trim(), taskSnapshot.getDownloadUrl().toString(), sender, receiver, "image");

                //adding an upload to firebase database
                String uploadId = mDatabase.push().getKey();
                mDatabase.child(uploadId).setValue(upload);
                if (!isOnline.equals("online")) {
                    String id = mDatabase.push().getKey();
                    UnreadedMessages unreadedMessages = new UnreadedMessages("image", "image");
                    userDatabase.child(sender).child(id).setValue(unreadedMessages);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "A failure have detected", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    //upload file from gallery
    private void uploadFile() {
        //checking if file is available
        if (filePath != null) {
            //displaying progress dialog while image is uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            //getting the storage reference
            StorageReference sRef = storageReference.child(Constants.FILEUPLOADPATH + System.currentTimeMillis() + "." + getFileExtension(filePath));

            //adding the file to reference
            sRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //dismissing the progress dialog
                            progressDialog.dismiss();

                            //displaying success toast
                            // Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                            @SuppressWarnings("VisibleForTests")
                            Message upload = new Message(editTextName.getText().toString().trim(), taskSnapshot.getDownloadUrl().toString(), sender, receiver, "image");

                            String uploadId = mDatabase.push().getKey();
                            mDatabase.child(uploadId).setValue(upload);
                            if (!isOnline.equals("online")) {
                                String id = mDatabase.push().getKey();
                                UnreadedMessages unreadedMessages = new UnreadedMessages("image", "image");
                                userDatabase.child(sender).child(id).setValue(unreadedMessages);
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests")
                            //displaying the upload progress
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        } else {
            //display an error if no file is selected
        }
    }

    //initialize menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    //get menu action
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (isNetworkAvailable()) {
            if (id == R.id.profile) {
                goToMainProfile(); //redirect to main profile
                return true;
            }
            if (id == R.id.home) {
                isHomeMenuPressed = true;
                Toast.makeText(this, "Home", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class)); //redirect to main activity screen
                finish();
            }
            if (id == R.id.action_settings) {
                isSettingsPressed = true;
                Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
                Intent modifySettings = new Intent(ChatActivity.this, SettingsActivity.class); //manage settings such as notification enable disable
                startActivity(modifySettings);
            }

        } else {
            Toast.makeText(this, "Not have connection", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    //Check network status
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void goBack(View view) {
        isBackPressed = true;
        finish();
    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        Toast.makeText(this, "back pressed", Toast.LENGTH_SHORT).show();
        finish();
    }

    //Check user status. For example if status has changed know that it is online or not
    private ChildEventListener getChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    String userUid = dataSnapshot.getKey();
                    if (userUid.equals(receiver)) {
                        User user = dataSnapshot.getValue(User.class);
                        Toast.makeText(getApplicationContext(), user.getEmail() + "receiver Status Changed to " + user.getStatus(), Toast.LENGTH_SHORT).show();
                        isOnline = user.getStatus();
                        setUpStatus();
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

    private void setUpStatus() {
        if (isOnline.equals("online")) {
            status.setText("Available");
        } else if (isOnline.equals("offline")) {
            status.setText("Not available");
        } else {
            status.setText("Indefinite");
        }
    }

    //Go to user profile
    public void goToMainProfile() {
        Intent i = new Intent(this, UserProfileActivity.class);
        if (name != null) {
            isProfilePressed = true;
            i.putExtra("userName", name);
            i.putExtra("userEmail", sender);
            i.putExtra("userImage", receiverImage);
            i.putExtra("userCreateDate", createTime);
            i.putExtra("userDesc", description);
            startActivity(i);
            finish();
        } else {
            Toast.makeText(this, "You have no internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

}