package com.example.namiq.egisterpp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.namiq.egisterpp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.IOException;

public class VoiceRecorder extends AppCompatActivity {
    private FloatingActionButton mRecordButton = null;
    private Chronometer mChronometer = null;
    ProgressBar progress;
    private TextView mRecordingPrompt,playText,uploadText;
    private boolean mStartRecording = true;
    private int progresStatus = 0;
    private int mRecordPromptCount = 0;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    StorageReference storageRef;
    public static final int RequestPermissionCode = 1;
    ImageView player, upload;
    boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recorder);
        storageRef = FirebaseStorage.getInstance().getReference();
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        progress = (ProgressBar) findViewById(R.id.recordProgressBar);
        upload = (ImageView) findViewById(R.id.uploader);
        player = (ImageView) findViewById(R.id.player);
        player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaying) {
                    isPlaying = true;
                    player.setImageResource(R.drawable.stop1);
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(AudioSavePathInDevice);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mediaPlayer.start();
                    Toast.makeText(VoiceRecorder.this, "Recording Playing", Toast.LENGTH_LONG).show();
                } else {
                    isPlaying = false;
                    player.setImageResource(R.drawable.play1);
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        MediaRecorderReady();
                    }
                }

            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(VoiceRecorder.this, AudioSavePathInDevice, Toast.LENGTH_SHORT).show();
                Toast.makeText(VoiceRecorder.this, "Upload Pressed",
                        Toast.LENGTH_LONG).show();
                uploadFile();
            }
        });
        //update recording prompt text
        mRecordingPrompt = (TextView) findViewById(R.id.recording_status_text);
        playText = (TextView) findViewById(R.id.playText);
        uploadText = (TextView) findViewById(R.id.uploadText);
        mRecordButton = (FloatingActionButton) findViewById(R.id.btnRecord);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;


            }
        });

    }

    private void onRecord(boolean start) {
        if (start) {
            AudioSavePathInDevice =
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                            System.currentTimeMillis() + "AudioRecording.3gp";

            MediaRecorderReady();
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IllegalStateException e) {

            } catch (IOException e) {

            }

            progress.setVisibility(View.VISIBLE);
            Toast.makeText(this, "started", Toast.LENGTH_SHORT).show();
            mRecordButton.setImageResource(R.drawable.ic_media_stop);
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {

                    if (progresStatus < 60) {
                        progresStatus++;
                        progress.setProgress(progresStatus);
                    } else {
                        progresStatus = 1;
                        progress.setProgress(progresStatus);
                    }

                    if (mRecordPromptCount == 0) {
                        mRecordingPrompt.setText("Recording.");
                    } else if (mRecordPromptCount == 1) {
                        mRecordingPrompt.setText("Recording..");
                    } else if (mRecordPromptCount == 2) {
                        mRecordingPrompt.setText("Recording...");
                        mRecordPromptCount = -1;
                    }
                    mRecordPromptCount++;
                }
            });
            mRecordPromptCount++;

        } else {
            try {
                mediaRecorder.stop();
            } catch (IllegalStateException e) {

            }
            player.setVisibility(View.VISIBLE);
            upload.setVisibility(View.VISIBLE);
            playText.setVisibility(View.VISIBLE);
            uploadText.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
            progresStatus = 0;
            progress.setVisibility(View.INVISIBLE);
            //stop recording
            mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
            //mPauseButton.setVisibility(View.GONE);
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mRecordingPrompt.setText(getString(R.string.record_prompt));
        }
    }


    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    private void uploadFile() {
        //if there is a file to upload
        if (AudioSavePathInDevice != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            final String time = String.valueOf(System.currentTimeMillis());
            StorageReference riversRef = storageRef.child("sounds/" + time + ".3gp");
            riversRef.putFile(Uri.fromFile(new File(AudioSavePathInDevice)))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                            File file = new File(AudioSavePathInDevice);
                            file.delete();
                            Toast.makeText(getApplicationContext(), taskSnapshot.getDownloadUrl().toString(), Toast.LENGTH_LONG).show();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("voice", time);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage

                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is not any file
        else {
            //you can display an error toast
        }
    }
}
