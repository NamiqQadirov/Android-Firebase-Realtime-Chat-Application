package com.example.namiq.egisterpp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.namiq.egisterpp.R;
import com.example.namiq.egisterpp.activities.ImageViewer;
import com.example.namiq.egisterpp.dbo.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Message> messages;
    private String sender;
    private String receiverImage;
    private String senderImage;
    private String receiver;
    StorageReference storageRef;
    File localFile;
    MediaPlayer mediaPlayer;
    int currentPlayingMedia = -1;

    public MyAdapter(Context context, List<Message> messages, String sender, String receiver, String receiverImage, String senderImage) {
        this.messages = messages;
        this.context = context;
        this.sender = sender;
        this.receiverImage = receiverImage;
        this.senderImage = senderImage;
        this.receiver = receiver;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSender().equals(sender)) {
            if (messages.get(position).getType().equals("image")) {
                return 1;
            } else if (messages.get(position).getType().equals("loc")) {
                return 5;
            } else if (messages.get(position).getType().equals("voice")) {
                return 7;
            } else {
                return 2;
            }
        } else {
            if (messages.get(position).getType().equals("image")) {
                return 3;
            } else if (messages.get(position).getType().equals("loc")) {
                return 6;
            } else if (messages.get(position).getType().equals("voice")) {
                return 8;
            } else {
                return 4;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case 1:
                View viewSender = inflater.inflate(R.layout.layout_sender_images, parent, false);
                viewHolder = new ViewHolderForImage(viewSender);
                break;
            case 2:
                View viewRecipient = inflater.inflate(R.layout.layout_texts, parent, false);
                viewHolder = new ViewHolderForText(viewRecipient);
                break;
            case 3:
                View viewImage = inflater.inflate(R.layout.layout_receiver_images, parent, false);
                viewHolder = new ViewHolderForImage(viewImage);
                break;
            case 5:
                View viewLoc = inflater.inflate(R.layout.layout_sender_location, parent, false);
                viewHolder = new ViewHolderForLocation(viewLoc);
                break;
            case 6:
                View viewLoc2 = inflater.inflate(R.layout.layout_receiver_location, parent, false);
                viewHolder = new ViewHolderForLocation(viewLoc2);
                break;
            case 7:
                View viewVoice = inflater.inflate(R.layout.layout_sender_voice, parent, false);
                viewHolder = new ViewHolderForVoice(viewVoice);
                break;
            case 8:
                View viewVoice2 = inflater.inflate(R.layout.layout_receiver_voice, parent, false);
                viewHolder = new ViewHolderForVoice(viewVoice2);
                break;
            default:
                View viewSenderDefault = inflater.inflate(R.layout.layout_texts_receive, parent, false);
                viewHolder = new ViewHolderForText(viewSenderDefault);
                break;
        }
        return viewHolder;


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message upload = messages.get(position);
        if (messages.get(position).getSender().equals(sender)) {
            if (messages.get(position).getType().equals("image")) {
                ViewHolderForImage holderForImage = (ViewHolderForImage) holder;
                Glide.with(context).load(upload.getUrl()).into(holderForImage.getImageView());
                Glide.with(context).load(senderImage).into(holderForImage.getAccountImageView());
                //holderForImage.getAccountImageView().setImageResource(R.drawable.user_pacific);
                holderForImage.getTextViewName().setText("image");
            } else if (messages.get(position).getType().equals("loc")) {
                ViewHolderForLocation holderForLocation = (ViewHolderForLocation) holder;
                if (senderImage != null) {
                    Glide.with(context).load(senderImage).into(holderForLocation.getAccountImageView());
                } else {
                    Toast.makeText(context, "sender bosh", Toast.LENGTH_SHORT).show();
                }

            } else if (messages.get(position).getType().equals("voice")) {
                ViewHolderForVoice holderForVoice = (ViewHolderForVoice) holder;
                if (senderImage != null) {
                    Glide.with(context).load(senderImage).into(holderForVoice.getAccountImageView());
                } else {
                   // Toast.makeText(context, "sender empty", Toast.LENGTH_SHORT).show();
                }

            } else {
                ViewHolderForText holderForText = (ViewHolderForText) holder;
                holderForText.getTextViewName().setText(upload.getUrl());
                Glide.with(context).load(senderImage).into(holderForText.getAccountImageView());
            }
        } else {
            if (messages.get(position).getType().equals("image")) {
                ViewHolderForImage holderForImage = (ViewHolderForImage) holder;
                Glide.with(context).load(upload.getUrl()).into(holderForImage.getImageView());
                Glide.with(context).load(receiverImage).into(holderForImage.getAccountImageView());
                holderForImage.getTextViewName().setText("image");
            } else if (messages.get(position).getType().equals("loc")) {
                ViewHolderForLocation holderForLocation = (ViewHolderForLocation) holder;
                Glide.with(context).load(receiverImage).into(holderForLocation.getAccountImageView());
            } else if (messages.get(position).getType().equals("voice")) {
                ViewHolderForVoice holderForVoice = (ViewHolderForVoice) holder;
                if (receiverImage != null) {
                    Glide.with(context).load(receiverImage).into(holderForVoice.getAccountImageView());
                } else {
                    Toast.makeText(context, "sender bosh", Toast.LENGTH_SHORT).show();
                }

            }
            if (messages.get(position).getType().equals("text")) {
                ViewHolderForText holderForText = (ViewHolderForText) holder;
                holderForText.getTextViewName().setText(upload.getUrl());
                Glide.with(context).load(receiverImage).into(holderForText.getAccountImageView());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ViewHolderForImage extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView textViewName;
        public ImageView imageView;
        public ImageView accountImageView;

        public ViewHolderForImage(View itemView) {
            super(itemView);

            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            accountImageView = (ImageView) itemView.findViewById(R.id.imageView2);
            itemView.setOnClickListener(this);
        }

        public TextView getTextViewName() {
            return textViewName;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public ImageView getAccountImageView() {
            return accountImageView;
        }

        @Override
        public void onClick(View v) {
            Message upload = messages.get(getLayoutPosition());
            Intent i = new Intent(context, ImageViewer.class);
            i.putExtra("name", receiver);
            i.putExtra("image", receiverImage);
            i.putExtra("mainimage", upload.getUrl());
            ((Activity) context).startActivityForResult(i, 5555);
        }
    }

    class ViewHolderForText extends RecyclerView.ViewHolder   {

        public TextView textViewName;
        public ImageView accountImageView;


        public ViewHolderForText(View itemView) {
            super(itemView);

            textViewName = (TextView) itemView.findViewById(R.id.text_view_sender_message);
            accountImageView = (ImageView) itemView.findViewById(R.id.accountImage);

        }

        public TextView getTextViewName() {
            return textViewName;
        }

        public ImageView getAccountImageView() {
            return accountImageView;
        }

    }

    class ViewHolderForLocation extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView accountImageView;

        public ViewHolderForLocation(View itemView) {
            super(itemView);
            accountImageView = (ImageView) itemView.findViewById(R.id.imageView2);
            itemView.setOnClickListener(this);
        }

        public ImageView getAccountImageView() {
            return accountImageView;
        }

        @Override
        public void onClick(View v) {
            Message upload = messages.get(getLayoutPosition());
            Toast.makeText(context, upload.getUrl(), Toast.LENGTH_SHORT).show();
            String uri = String.format(Locale.ENGLISH, "geo:" + upload.getUrl() + "?q=" + upload.getUrl() + "&z=10");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            context.startActivity(intent);
        }
    }

    class ViewHolderForVoice extends RecyclerView.ViewHolder implements View.OnClickListener {
        boolean isPlay;
        boolean isDownloaded;
        public ImageView accountImageView;
        public ImageView player;
        public TextView download;

        public ViewHolderForVoice(View itemView) {
            super(itemView);
            accountImageView = (ImageView) itemView.findViewById(R.id.imageView2);
            player = (ImageView) itemView.findViewById(R.id.player);
            download = (TextView) itemView.findViewById(R.id.downloading);
            itemView.setOnClickListener(this);
            player.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isPlay) {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                isPlay = false;
                                player.setImageResource(R.drawable.play);
                                // Toast.makeText(context, "finished", Toast.LENGTH_SHORT).show();
                            }
                        });

                        if (!isDownloaded) {
                            if (isNetworkAvailable()) {

                                if (currentPlayingMedia != getLayoutPosition()) {
                                    currentPlayingMedia = getLayoutPosition();
                                    Message upload = messages.get(currentPlayingMedia);
                                    String path = upload.getUrl();
                                    //Toast.makeText(context, path, Toast.LENGTH_SHORT).show();
                                    download.setVisibility(View.VISIBLE);
                                    storageRef = FirebaseStorage.getInstance().getReference();
                                    StorageReference riversRef = storageRef.child("sounds/" + path + ".3gp");

                                    try {
                                        localFile = File.createTempFile("test", "3gp");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    riversRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            // Toast.makeText(context, "Downloded", Toast.LENGTH_SHORT).show();
                                            download.setVisibility(View.GONE);
                                            isDownloaded = true;
                                            player.setImageResource(R.drawable.stop);
                                            isPlay = true;

                                            try {
                                                mediaPlayer.setDataSource(localFile.getAbsolutePath());
                                                mediaPlayer.prepare();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            mediaPlayer.start();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {

                                        }
                                    });
                                } else {

                                }

                            } else {
                                Toast.makeText(context, "You have not Connection", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (currentPlayingMedia != getLayoutPosition()) {
                                if (isNetworkAvailable()) {
                                    currentPlayingMedia = getLayoutPosition();
                                    Message upload = messages.get(currentPlayingMedia);
                                    String path = upload.getUrl();
                                    //Toast.makeText(context, path, Toast.LENGTH_SHORT).show();
                                    download.setVisibility(View.VISIBLE);
                                    storageRef = FirebaseStorage.getInstance().getReference();
                                    StorageReference riversRef = storageRef.child("sounds/" + path + ".3gp");

                                    try {
                                        localFile = File.createTempFile("test", "3gp");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    riversRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            // Toast.makeText(context, "Downloded", Toast.LENGTH_SHORT).show();
                                            download.setVisibility(View.GONE);
                                            isDownloaded = true;
                                            player.setImageResource(R.drawable.stop);
                                            isPlay = true;

                                            try {
                                                mediaPlayer.setDataSource(localFile.getAbsolutePath());
                                                mediaPlayer.prepare();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            mediaPlayer.start();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {

                                        }
                                    });
                                } else {
                                    Toast.makeText(context, "You have not Connection", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                player.setImageResource(R.drawable.stop);
                                isPlay = true;
                                try {
                                    mediaPlayer.setDataSource(localFile.getAbsolutePath());
                                    mediaPlayer.prepare();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                mediaPlayer.start();
                            }

                        }
                    } else {
                        player.setImageResource(R.drawable.play);
                        isPlay = false;
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                        }
                    }

                }
            });
        }

        public ImageView getAccountImageView() {
            return accountImageView;
        }

        @Override
        public void onClick(View v) {

        }
    }

    //Check network status
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}