package com.example.chattapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chattapp.Models.Chat;
import com.example.chattapp.Models.User;
import com.example.chattapp.R;
import com.example.chattapp.Ui.LoginActivity;
import com.example.chattapp.Ui.MessegeActivity;
import com.example.chattapp.Ui.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<User> mUsersList;
    private boolean isStatus;
    private String mLastMessege;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;
    private DatabaseReference mDatabaseReference;

    public UserAdapter(Context context, List<User> mUsersList, boolean isStatus) {
        this.context = context;
        this.mUsersList = mUsersList;
        this.isStatus = isStatus;
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser != null){
            userId = mUser.getUid();
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {

        User user = mUsersList.get(position);
        holder.mUsername.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.mProfile_image.setImageResource(R.drawable.ic_person_);
        } else {
            Glide.with(context).load(user.getImageURL()).into(holder.mProfile_image);
        }

        if (isStatus) {
            lastMessege(user.getId(), holder.mLast);
        } else {
            holder.mLast.setVisibility(View.GONE);
        }

        if (isStatus) {
            if (user.getStatus().equals("online")) {
                holder.mOn.setVisibility(View.VISIBLE);
                holder.mOff.setVisibility(View.GONE);
            } else {
                holder.mOff.setVisibility(View.VISIBLE);
                holder.mOn.setVisibility(View.GONE);
            }
        } else {
            holder.mOn.setVisibility(View.GONE);
            holder.mOff.setVisibility(View.GONE);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessegeActivity.class);
                intent.putExtra("userid", user.getId());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mUsersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView mProfile_image;
        TextView mUsername;
        CircleImageView mOn, mOff;
        TextView mLast;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mProfile_image = itemView.findViewById(R.id.id_item_profile_image);
            mUsername = itemView.findViewById(R.id.id_item_username);
            mOff = itemView.findViewById(R.id.id_item_status_off);
            mOn = itemView.findViewById(R.id.id_item_status_on);
            mLast = itemView.findViewById(R.id.id_last_messege);

        }
    }

    private void lastMessege(String userid, TextView last_msg) {
        mLastMessege = "default";
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chats");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    Chat chat = item.getValue(Chat.class);
                    if (chat.getReciever().equals(userId) && chat.getSender().equals(userid) || chat.getReciever().equals(userid) && chat.getSender().equals(userId)) {
                        mLastMessege = chat.getMessege();

                    }
                }
                switch (mLastMessege) {
                    case "default":
                        last_msg.setText("No Messege");
                        break;
                    default:
                        last_msg.setText(mLastMessege);
                        break;
                }
//                mLastMessege = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
