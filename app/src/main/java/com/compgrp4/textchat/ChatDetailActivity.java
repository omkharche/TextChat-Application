package com.compgrp4.textchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.compgrp4.textchat.Adapters.ChatAdapter;
import com.compgrp4.textchat.Adapters.UsersAdapter;
import com.compgrp4.textchat.Fragments.UserFragment;
import com.compgrp4.textchat.Models.MessageModel;
import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ChatDetailActivity extends AppCompatActivity {
    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 400;
    public static final int IMAGE_PICK_GALLERY_CODE=1000;
    public static final int IMAGE_PICK_CAMERA_CODE=1001;
    String camerapermission[];
    String storagepermission[];
    Uri image_uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding=ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String senderId = firebaseUser.getUid();
        String receiveId = getIntent().getStringExtra("userid");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);


        camerapermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels,ChatDetailActivity.this);
        binding.ChatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatDetailActivity.this);
        binding.ChatRecyclerView.setLayoutManager(linearLayoutManager);
        database.getReference().child("chats").child(senderId+receiveId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        messageModels.clear();
                                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                            MessageModel model = snapshot1.getValue(MessageModel.class);
                                            messageModels.add(model);
                                        }
                                        chatAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
        binding.backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(binding.etMessage.getText().toString().equals(""))
                {
                    Log.d("dar", "if executed");
                    binding.send.setVisibility(View.GONE);
                    binding.audio.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.send.setVisibility(View.VISIBLE);
                    binding.audio.setVisibility(View.GONE);
                }
            }
        });
        binding.audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                if(intent.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(intent,10);
                }
                else
                {
                    Toast.makeText(ChatDetailActivity.this, "Settings Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.textrecog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items ={"Camera","Gallery"};
                AlertDialog.Builder dialog =  new AlertDialog.Builder(ChatDetailActivity.this);
                dialog.setTitle("Select Image");
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            if(!checkCameraPermission()){
                                requestCameraPermission();
                            }else {
                                pickCamera();
                            }
                        }
                        if(which==1){
                            if(!checkStoragePermission()){
                                requestStoragePermission();
                            }else {
                                pickGallery();
                            }
                        }
                    }
                });
                dialog.create().show();
            }
        });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.etMessage.getText().toString();
                final MessageModel model= new MessageModel(senderId,message);
                model.setTimestamp(new Date().getTime());
                model.setuId(senderId);
                binding.etMessage.setText("");
                database.getReference().child("chats").child(senderId+receiveId).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        database.getReference().child("chats").child(receiveId+senderId).push().setValue(model).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                                                .child(senderId)
                                                .child(receiveId);
                                        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.exists()){
                                                    chatRef.child("id").setValue(receiveId);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                                                .child(receiveId)
                                                .child(senderId);
                                        chatRefReceiver.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.exists()){
                                                    chatRefReceiver.child("id").setValue(senderId);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                        );

                    }
                });

            }
        });




    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues values =new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to Text");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(ChatDetailActivity.this,storagepermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(ChatDetailActivity.this,camerapermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result&&result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0)
                {
                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted&&writeStorageAccepted){
                        pickCamera();
                    }
                    else
                    {
                        Toast.makeText(ChatDetailActivity.this,"Permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0)
                {
                    boolean writeStorageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        pickGallery();
                    }
                    else
                    {
                        Toast.makeText(ChatDetailActivity.this,"Permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case IMAGE_PICK_GALLERY_CODE:
                if(resultCode==RESULT_OK)
                {
                    CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(ChatDetailActivity.this);
                }
                break;
            case IMAGE_PICK_CAMERA_CODE:
                if(resultCode==RESULT_OK)
                {
                    CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(ChatDetailActivity.this);
                }
                break;
            case 10:
                if(resultCode==RESULT_OK&&data!=null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                binding.etMessage.setText(result.get(0));
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if(resultCode==RESULT_OK){
                    Uri resultUri = result.getUri();
                    binding.ImageIv.setImageURI(resultUri);
                    BitmapDrawable bitmapDrawable = (BitmapDrawable)binding.ImageIv.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                    if(!textRecognizer.isOperational()){
                        Toast.makeText(ChatDetailActivity.this,"Error",Toast.LENGTH_SHORT);
                    }
                    else
                    {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> items = textRecognizer.detect(frame);
                        StringBuilder sb = new StringBuilder();
                        for(int i=0;i< items.size();i++){
                            TextBlock myitem = items.valueAt(i);
                            sb.append(myitem.getValue());
                        }
                        binding.etMessage.setText(sb.toString());
                    }

                }
                else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                        Exception error = result.getError();
                    Toast.makeText(ChatDetailActivity.this,""+error,Toast.LENGTH_SHORT);
                }
                break;
        }
    }

    private void status(String status) {

                                reference = FirebaseDatabase.getInstance().getReference( "Users" ).child(firebaseUser.getUid());
                                HashMap<String, Object> hashMap = new HashMap<>();
                                Log.d("tata", "child updated");
                                Log.d("tata", status);
                                hashMap.put( "status", status );
                                reference.updateChildren( hashMap );
    }

    @Override
    protected void onResume() {
        Log.d("tata","onResume");
        super.onResume();
        status( "online" );
    }
}
