package com.example.chattapp.Fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chattapp.Adapters.UserAdapter;
import com.example.chattapp.Models.Chat;
import com.example.chattapp.Models.ChatList;
import com.example.chattapp.Models.User;
import com.example.chattapp.Notification.Token;
import com.example.chattapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private RecyclerView recycler;
    private UserAdapter userAdapter;
    private List<ChatList> mUsersList = new ArrayList<>();
    private List<User> mUsers = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;
    private DatabaseReference mDatabaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recycler = view.findViewById(R.id.id_recyceler_chats);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chatlist").child(userId);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsersList.clear();
                for (DataSnapshot item:snapshot.getChildren()){
                    ChatList chatList = item.getValue(ChatList.class);
                    mUsersList.add(chatList);
                }
                chatList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateToken(getActivity());

//        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chats");
//        mDatabaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                mUsers.clear();
//                for (DataSnapshot item : snapshot.getChildren()) {
//                    // el taraf el tany // reciever
//                    Chat chat = item.getValue(Chat.class);
//                    if (chat.getSender().equals(userId)) {
//                        mUsers.add(chat.getReciever());
//                    }
//                    if (chat.getReciever().equals(userId)) {
//                        mUsers.add(chat.getSender());
//                    }
//                }
//                readChats();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        return view;
    }

    private void chatList() {
        mDatabaseReference =FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot item :snapshot.getChildren()){
                    User user=item.getValue(User.class);
                   for (ChatList id: mUsersList){
                       if(id.getId().equals(user.getId())){
                           mUsers.add(user);
                       }
                   }
                }
                userAdapter = new UserAdapter(getContext(),mUsers,true);// to show the on/off circular imageview
                recycler.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void updateToken(String token){
//        mDatabaseReference =FirebaseDatabase.getInstance().getReference().child("Tokens");
//        Token token1 = new Token(token);
//        mDatabaseReference.child(userId).setValue(token1);
//    }


    /**
     * add new token of each user to tokens list in firebase
     *
     * @param activity
     */
    public void updateToken(Activity activity) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(activity, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
                Token token1 = new Token(newToken);
                reference.child(mUser.getUid()).setValue(token1);
                Log.e("newToken", newToken);

            }
        });
    }

//    private void readChats() {
//        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
//        mDatabaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                mUsersList.clear();
//                for (DataSnapshot item : snapshot.getChildren()) {
//                    User user = item.getValue(User.class);
//                    for (String id : mUsers) {
//                        if (user.getId().equals(id)) { // check if the user is one of the mUserList(people i chatted with) or no
//                            if (mUsersList.size()!= 0) {
//                                for (User user1 : mUsersList) {
//                                    if (!user1.getId().equals(user.getId())) {
//                                        mUsersList.add(user);
//                                    }
//                                }
//                            } else {
//                                mUsersList.add(user);
//                            }
//                        }
//                    }
//                }
//                userAdapter = new UserAdapter(getContext(), mUsersList,false);
//                recycler.setAdapter(userAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
}