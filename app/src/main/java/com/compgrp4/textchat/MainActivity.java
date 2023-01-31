package com.compgrp4.textchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.compgrp4.textchat.Adapters.FragmentsAdapter;
import com.compgrp4.textchat.Adapters.UsersAdapter;
import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ActionBar actionBar;
    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        user=FirebaseAuth.getInstance().getCurrentUser();
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        actionBar=getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0b6156")));
        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        binding.tablayout.setupWithViewPager(binding.viewPager);
 }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    private void status(String status) {

                                reference = FirebaseDatabase.getInstance().getReference( "Users" ).child( user.getUid() );
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

    @Override
    protected void onPause() {
        Log.d("tata","onPause");
        super.onPause();
        status( "offline" );
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.settings:
                Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                auth.signOut();
                Intent intent = new Intent(MainActivity.this,SignInActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }
}