package com.compgrp4.textchat.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.compgrp4.textchat.Adapters.UsersAdapter;
import com.compgrp4.textchat.Models.Chatlist;
import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.Notifications.Token;
import com.compgrp4.textchat.R;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

        private RecyclerView recyclerView;

        private UsersAdapter userAdapter;
        private List<Users> mUsers;

        FirebaseUser firebaseUser;
        DatabaseReference reference;

        private List<Chatlist> usersList;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_chat, container, false);

            recyclerView = view.findViewById(R.id.chatRecyclarView);

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            usersList = new ArrayList<>();

                                    reference = FirebaseDatabase.getInstance().getReference().child("Chatlist").child(firebaseUser.getUid());
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            usersList.clear();
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                                Chatlist chatlist = snapshot.getValue(Chatlist.class);
                                                usersList.add(chatlist);
                                            }
                                            chatList();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
            return view;
        }


        private void chatList() {
            mUsers = new ArrayList<>();
            reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Users user = snapshot.getValue(Users.class);
                        for (Chatlist chatlist : usersList){
                            if (user.getUserId().equals(chatlist.getId())){
                                mUsers.add(user);
                            }
                        }
                    }
                    userAdapter = new UsersAdapter(getContext(), mUsers, true);
                    recyclerView.setAdapter(userAdapter);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }