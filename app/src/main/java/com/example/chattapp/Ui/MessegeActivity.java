package com.example.chattapp.Ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattapp.Adapters.MessegeAdapter;
import com.example.chattapp.Fragments.APIService;
import com.example.chattapp.Models.Chat;
import com.example.chattapp.Models.User;
import com.example.chattapp.Notification.Client;
import com.example.chattapp.Notification.Data;
import com.example.chattapp.Notification.MyResponse;
import com.example.chattapp.Notification.Sender;
import com.example.chattapp.Notification.Token;
import com.example.chattapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessegeActivity extends AppCompatActivity {

    private RecyclerView mRecycler;
    private CircleImageView mProfile_image;
    private TextView mUsername;
    private ImageButton mSend;
    private EditText mTextMessege;
    private Toolbar toolbar;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference mDatabaseReference;
    MessegeAdapter adapter;
    List<Chat> mChats;
    ValueEventListener seenListner;
    String reciever_id = "";
    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messege);
        mTextMessege = findViewById(R.id.id_text_send);
        mRecycler = findViewById(R.id.id_messege_recycler);
        mSend = findViewById(R.id.btn_msg);
        mProfile_image = findViewById(R.id.msg_profile_image);
        mUsername = findViewById(R.id.id_msg_username);
        toolbar = findViewById(R.id.msg_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        linearLayout.setStackFromEnd(true);
        mRecycler.setLayoutManager(linearLayout);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();
        //               back arrow on toolBar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
//              startActivity(new Intent(MessegeActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        // Retrofit implements the interface APII
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        Intent intent = getIntent();
        reciever_id = intent.getStringExtra("userid");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(reciever_id);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // display reciever image and name + displaying his replies
                User user = snapshot.getValue(User.class);
                mUsername.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    mProfile_image.setImageResource(R.drawable.ic_person_);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(mProfile_image);
                }
                readMessege(userId, reciever_id, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessege(reciever_id);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String messge = mTextMessege.getText().toString().trim();
                if (!messge.equals("")) {
                    sendMessege(new Chat(userId, reciever_id, messge, false));
                } else {
                    Toast.makeText(MessegeActivity.this, "You can't send an empty messege!", Toast.LENGTH_SHORT).show();
                }
                mTextMessege.setText("");


            }
        });
    }

    private void seenMessege(String hisID) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chats");
        seenListner = mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    Chat chat = item.getValue(Chat.class);
                    //if i am reciever means i seen the messege and i am on the messegeActivity(our chat), he is sender also, so i have seen the messege
                    if (chat.getReciever().equals(userId) && chat.getSender().equals(hisID)) {
                        chat.setSeen(true);
                        item.getRef().setValue(chat);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessege(Chat chat) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        mRef.push().setValue(chat);

        DatabaseReference Chatref = FirebaseDatabase.getInstance().getReference().child("Chatlist").child(userId).child(reciever_id);

        Chatref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    HashMap<String, Object> hm = new HashMap<>();
                    hm.put("id", reciever_id);
                    Chatref.setValue(hm);
                }// no recivers ids yet
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg = chat.getMessege();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify) {
                    sendNotifications(reciever_id, user.getUsername(), msg);
                }

                notify = false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotifications(String reciever_id, String username, String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Query query = tokens.orderByKey().equalTo(reciever_id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    Token token = item.getValue(Token.class);
                    Data data = new Data(userId, username + ": " + msg, "New Messege", reciever_id, R.mipmap.ic_launcher_round);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessegeActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessege(String myID, String userID, String imgURL) {
        mChats = new ArrayList<>();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chats");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChats.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Chat chat = item.getValue(Chat.class);
                    if (chat.getSender().equals(myID) && chat.getReciever().equals(userID) || chat.getSender().equals(userID) && chat.getReciever().equals(myID)) {
                        mChats.add(chat); // list of chats between me and receiever
                    }
                    adapter = new MessegeAdapter(MessegeActivity.this, mChats, imgURL);
                    mRecycler.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void currentUser(String id){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser",id);
        editor.apply();
    }

    private void setStatus(String status) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);
        mDatabaseReference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
        currentUser(reciever_id);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(seenListner != null && mDatabaseReference != null){
        mDatabaseReference.removeEventListener(seenListner);
        setStatus("offline");
//        }
        currentUser("none");
    }


}


















