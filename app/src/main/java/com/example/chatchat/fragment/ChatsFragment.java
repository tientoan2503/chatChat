package com.example.chatchat.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatchat.R;
import com.example.chatchat.adapter.UserAdapter;
import com.example.chatchat.model.Chat;
import com.example.chatchat.model.User;
import com.example.chatchat.notification.Token;
import com.example.chatchat.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView mRecyclerview;
    private List<User> mUserList;
    private DatabaseReference mReference;
    private FirebaseUser mCurrentUser;
    private List<String> mIdList;
    private UserAdapter mUserAdapter;
    private EditText mSearchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mRecyclerview = view.findViewById(R.id.recycler_chat);
        mRecyclerview.setHasFixedSize(true);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        mSearchView = view.findViewById(R.id.edt_search);


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mReference = FirebaseDatabase.getInstance().getReference(Utils.CHATS);

        //Lấy ra những người đã nhắn tin thông qua id
        mIdList = new ArrayList<>();
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mIdList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat != null) {
                        //Kiểm tra có xuất hiện id của user hiện tại trong bất kì đoạn chat nào hay không
                        if (chat.getSender().equals(mCurrentUser.getUid())) {
                            mIdList.add(chat.getReceiver());
                        } else if (chat.getReceiver().equals(mCurrentUser.getUid())) {
                            mIdList.add(chat.getSender());
                        }
                    }
                }

                getUserHaveChated();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;
    }

    private void getUserHaveChated() {
        mUserList = new ArrayList<>();
        mReference = FirebaseDatabase.getInstance().getReference(Utils.USERS);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mSearchView.getText().toString().equals("")) {
                    mUserList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            for (String id : mIdList) {
                                if (user.getId().equals(id)) {
                                    if (!mUserList.contains(user)) {
                                        mUserList.add(user);
                                    }
                                }
                            }
                        }
                    }

                    mUserAdapter = new UserAdapter(getContext(), mUserList, UserAdapter.USER_TYPE_CHAT);
                    mRecyclerview.setAdapter(mUserAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //Tim kiem user
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void searchUsers(String s) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference(Utils.USERS)
                .orderByChild(Utils.SEARCH)
                .startAt(s)
                .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUserList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        if (!user.getId().equals(currentUser.getUid())) {
                            if (mSearchView.getText().toString().equals("")) {
                                if (mIdList.contains(user.getId())) {
                                    mUserList.add(user);
                                }
                            } else {
                                mUserList.add(user);
                            }
                        }
                    }
                }
                mUserAdapter = new UserAdapter(getContext(), mUserList, UserAdapter.USER_TYPE_CHAT);
                mRecyclerview.setAdapter(mUserAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Utils.TOKENS);
        Token token1 = new Token(token);
        reference.child(mCurrentUser.getUid()).setValue(token1);
    }
}
