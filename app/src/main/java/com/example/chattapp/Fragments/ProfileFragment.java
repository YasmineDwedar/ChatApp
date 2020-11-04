package com.example.chattapp.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattapp.Models.User;
import com.example.chattapp.R;
import com.example.chattapp.Ui.MainActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private CircleImageView mProfile_image;
    private TextView mUsername;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private StorageTask mUploadTask;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mProfile_image = view.findViewById(R.id.id_item_profile_image);
        mUsername = view.findViewById(R.id.id_profile_name);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();
        mStorageReference = FirebaseStorage.getInstance().getReference().child("uploads");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                mUsername.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    mProfile_image.setImageResource(R.drawable.ic_person_);
                } else {
                    Glide.with(getActivity()).load(user.getImageURL()).into(mProfile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        mProfile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

        return view;
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getExtensions(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mProfile_image.setImageURI(mImageUri);
            if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(getContext(), "Uplaod in Progess", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }


    // Both Methods Work in 2 different ways :)
    private void uploadImage() {

        if (mImageUri != null) {
            final ProgressDialog progressDialo = new ProgressDialog(getContext());
            progressDialo.setMessage("Uploading..");
            progressDialo.show();
            final StorageReference fileReference = mStorageReference.child(System.currentTimeMillis() + "." + getExtensions(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri);
            mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        mDatabaseReference.updateChildren(map);
                        progressDialo.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        progressDialo.dismiss();
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialo.dismiss();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "No Image selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage2() {

        if (mImageUri != null) {
            final ProgressDialog progressDialo = new ProgressDialog(getContext());
            progressDialo.setMessage("Uploading..");
            progressDialo.show();
            final StorageReference fileReference = mStorageReference.child(System.currentTimeMillis() + "." + getExtensions(mImageUri));
            fileReference.putFile(mImageUri)
                    // upload the image to storage successListner
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get the DownloadUrl by CompleteListener
                            fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        String mUri = downloadUri.toString();
                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("imageURL", mUri);
                                        mDatabaseReference.updateChildren(map);
                                        progressDialo.dismiss();
                                    } else {
                                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                                        progressDialo.dismiss();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialo.dismiss();
                }
            });
        } else {
            Toast.makeText(getContext(), "No Image selected!", Toast.LENGTH_SHORT).show();
        }
    }
}

