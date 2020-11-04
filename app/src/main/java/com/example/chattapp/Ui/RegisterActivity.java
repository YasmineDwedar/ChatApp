package com.example.chattapp.Ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chattapp.Models.User;
import com.example.chattapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    private EditText mUsername, mEmail, mPassword;
    private Toolbar toolbar;
    private Button mRegister;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;
    private DatabaseReference mDatabaseReference;
    private Map<String, String> hashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mUsername = findViewById(R.id.id_register_username);
        mEmail = findViewById(R.id.id_register_email);
        mPassword = findViewById(R.id.id_register_password);
        mRegister = findViewById(R.id.id_register_button);
        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.id_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsername.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                if (TextUtils.isEmpty(username) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "All Field are required!", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(username)) {
                    mUsername.setError("Feild Required!");
                } else if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Feild Required!");
                } else if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Feild Required!");
                } else {
                    if (password.length() < 6) {
                        Toast.makeText(RegisterActivity.this, "password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    } else {
                        register(username, email, password);

                    }
                }
            }
        });
    }

    private void register(String username, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mUser = mAuth.getCurrentUser();
                    userId = mUser.getUid();
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                    mDatabaseReference.keepSynced(true);
                    User user = new User(username,"default",userId,"offline",username.toLowerCase());
                    mDatabaseReference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Go To Home", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    Toast.makeText(RegisterActivity.this, "This email is  already used", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
