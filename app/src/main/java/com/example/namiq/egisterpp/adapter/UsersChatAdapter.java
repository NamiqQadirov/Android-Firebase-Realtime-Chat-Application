package com.example.namiq.egisterpp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.namiq.egisterpp.R;
import com.example.namiq.egisterpp.activities.ChatActivity;
import com.example.namiq.egisterpp.dbo.Constants;
import com.example.namiq.egisterpp.dbo.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.example.namiq.egisterpp.ChatActivity;

import java.util.List;

/**
 * Created by Marcel on 11/11/2015.
 */
public class UsersChatAdapter extends RecyclerView.Adapter<UsersChatAdapter.ViewHolderUsers> {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    private List<User> mUsers;
    private Context mContext;
    public static boolean onStopStatus;
    private User currentUser;

    public UsersChatAdapter(Context context, List<User> fireChatUsers) {
        mUsers = fireChatUsers;
        mContext = context;
    }

    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(mContext, LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderUsers holder, int position) {

        User fireChatUser = mUsers.get(position);
        Glide.with(mContext).load(fireChatUser.getUrl()).into(holder.getProfileImage());

        // Set display name
        holder.getFriendName().setText(fireChatUser.getName());

        // Set presence status
        holder.getStatus().setText(fireChatUser.getStatus());
        // Set presence text color
        if (fireChatUser.getStatus().equals(ONLINE)) {
            // Green color
            holder.getStatus().setTextColor(Color.parseColor("#00FF00"));
        } else {
            // Red color
            holder.getStatus().setTextColor(Color.parseColor("#FF0000"));
        }

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void refill(User users) {
        mUsers.add(users);
        notifyDataSetChanged();
    }

    public void changeUser(int index, User user) {
        mUsers.set(index, user);
        notifyDataSetChanged();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void clear() {
        mUsers.clear();
    }


    /* ViewHolder for RecyclerView */
    public class ViewHolderUsers extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView profileImage;
        private TextView friendName;
        private TextView status;
        private Context mContextViewHolder;

        public ImageView getProfileImage() {
            return profileImage;
        }

        public TextView getFriendName() {
            return friendName;
        }

        public TextView getStatus() {
            return status;
        }


        public ViewHolderUsers(Context context, View itemView) {
            super(itemView);
            profileImage = (ImageView) itemView.findViewById(R.id.img_avatar);
            friendName = (TextView) itemView.findViewById(R.id.text_view_display_name);
            status = (TextView) itemView.findViewById(R.id.text_view_connection_status);
            mContextViewHolder = context;

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            onStopStatus = true;
            User recipent = mUsers.get(getLayoutPosition());
            String chatRoomName = createMessageRoom(recipent, currentUser);
            //Toast.makeText(mContextViewHolder, chatRoomName, Toast.LENGTH_SHORT).show();
            Intent chatIntent = new Intent(mContextViewHolder, ChatActivity.class);
            chatIntent.putExtra(Constants.SENDER, currentUser.getEmail());
            chatIntent.putExtra(Constants.RECIPENT, recipent.getEmail());
            chatIntent.putExtra(Constants.CHATROOMNAME, chatRoomName);
            chatIntent.putExtra(Constants.USERNAME, recipent.getName());
            chatIntent.putExtra(Constants.USERIMAGE, recipent.getUrl());
            chatIntent.putExtra(Constants.SENDERIMAGE, currentUser.getUrl());
            chatIntent.putExtra(Constants.ISONLINE, recipent.getStatus());
            chatIntent.putExtra(Constants.CREATETIME, recipent.getCreateTime());
            chatIntent.putExtra(Constants.DESCRIPTION, recipent.getDescription());
            clearUnreadedMessages(currentUser.getEmail(), recipent.getEmail());
            // Start new activity
            mContextViewHolder.startActivity(chatIntent);

        }
    }

    private String createMessageRoom(User recipent, User currentUser) {
        String chatRoom;

        if (recipent.getCreateTime() > currentUser.getCreateTime()) {
            chatRoom = recipent.getEmail() + "+" + currentUser.getEmail();
        } else {
            chatRoom = currentUser.getEmail() + "+" + recipent.getEmail();
        }
        return chatRoom;
    }

    private void clearUnreadedMessages(String sender, String receiver) {
        FirebaseDatabase.getInstance().getReference().child("unreaded").child(sender).child(receiver).removeValue();
    }
}
