package com.example.chattapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chattapp.Models.Chat;
import com.example.chattapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessegeAdapter extends RecyclerView.Adapter<MessegeAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChatsList;
    private String mImageURL;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference mDatabaseReference;


    public MessegeAdapter(Context mContext, List<Chat> list, String imgURL) {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();
        this.mContext = mContext;
        this.mChatsList = list;
        mImageURL = imgURL;
    }

    @NonNull
    @Override
    // takes the list of Chats and displays left / right according to whos the sender
    public MessegeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessegeAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessegeAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = mChatsList.get(position);
        holder.show_messege.setText(chat.getMessege());
        if (mImageURL.equals("default")) {
            holder.mProfile_image.setImageResource(R.drawable.ic_person_);
        } else {
            Glide.with(mContext).load(mImageURL).into(holder.mProfile_image);
        }
        // if its the last chat in the mChatList
        if(position == mChatsList.size()-1){
            // check his seen boolean and set text accordingly
            if(chat.getSeen()){
                holder.seenMessege.setText("seen");
            }else
            {
                holder.seenMessege.setText("Delivered");
            }
        }else{
            holder.seenMessege.setVisibility(View.GONE);
        }


    }

    // takes the list of Chats and displays left / right according to whos the sender
    @Override
    public int getItemViewType(int position) {

        if (mChatsList.get(position).getSender().equals(userId)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }

    @Override
    public int getItemCount() {
        return mChatsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mProfile_image;
        public TextView show_messege, seenMessege;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mProfile_image = itemView.findViewById(R.id.profile_image);
            show_messege = itemView.findViewById(R.id.show_message);
            seenMessege = itemView.findViewById(R.id.id_seen);
        }
    }
}
