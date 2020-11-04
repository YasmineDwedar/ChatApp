package com.example.chattapp.Ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chattapp.Adapters.ViewPageAdapter;
import com.example.chattapp.Fragments.ChatsFragment;
import com.example.chattapp.Fragments.ProfileFragment;
import com.example.chattapp.Fragments.UsersFragment;
import com.example.chattapp.Models.Chat;
import com.example.chattapp.Models.User;
import com.example.chattapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private CircleImageView mProfile_image;
    private TextView mUsername;
    private Toolbar toolbar;
    private ViewPager pager;
    private TabLayout tab;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProfile_image = findViewById(R.id.profile_image);
        mUsername = findViewById(R.id.id_home_username);
        toolbar = findViewById(R.id.toolbar);
        pager = findViewById(R.id.id_view_pager);
        tab = findViewById(R.id.id_tabs);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);
                mUsername.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    mProfile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(mProfile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Chats");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ViewPageAdapter pageAdapter = new ViewPageAdapter(getSupportFragmentManager());
                int unread =0;
                for (DataSnapshot item :snapshot.getChildren())
                {
                    Chat chat = item.getValue(Chat.class);
                    if(chat.getReciever().equals(userId) && !chat.getSeen()){
                        unread++;
                    }
                }
                if(unread ==0){
                    pageAdapter.addfragment(new ChatsFragment(),"Chats");
                }else{
                    pageAdapter.addfragment(new ChatsFragment(),"("+unread+") Chats");
                }
                pageAdapter.addfragment(new UsersFragment(),"Users");
                pageAdapter.addfragment(new ProfileFragment(),"Profile");
                pager.setAdapter(pageAdapter);
                tab.setupWithViewPager(pager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    break;
        }
        return true;
    }

    private  void status(String status){
        mDatabaseReference =FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        HashMap<String,Object> map = new HashMap<>();
        map.put("status", status);
        mDatabaseReference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}