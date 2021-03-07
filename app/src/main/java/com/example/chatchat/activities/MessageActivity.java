package com.example.chatchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatchat.R;
import com.example.chatchat.utils.Utils;
import com.example.chatchat.adapter.ChatAdapter;
import com.example.chatchat.fragment.APIService;
import com.example.chatchat.model.Chat;
import com.example.chatchat.model.User;
import com.example.chatchat.notification.Client;
import com.example.chatchat.notification.Data;
import com.example.chatchat.notification.MyResponse;
import com.example.chatchat.notification.Sender;
import com.example.chatchat.notification.Token;
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

public class MessageActivity extends AppCompatActivity {

    private CircleImageView mImgAvt, mStatusOn, mStatusOff;
    private TextView mUsername;
    private Toolbar mToolbar;
    private FirebaseUser mSenderUser;
    private EditText mMessage;
    private ImageButton mBtnSend;
    private String mReceiverId;

    private ChatAdapter mChatAdapter;
    private List<Chat> mChatList;
    private RecyclerView mRecyclerview;

    private ValueEventListener mSeenListener;

    private APIService mApiService;
    private boolean mIsNotify = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mImgAvt = findViewById(R.id.avt_current);
        mUsername = findViewById(R.id.current_name);
        mToolbar = findViewById(R.id.tool_bar);
        mMessage = findViewById(R.id.edt_message);
        mBtnSend = findViewById(R.id.btn_send);
        mStatusOff = findViewById(R.id.status_off);
        mStatusOn = findViewById(R.id.status_on);

        mApiService = Client.getClient(Utils.URL).create(APIService.class);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerview = findViewById(R.id.recyclerview_message);
        mRecyclerview.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        manager.setStackFromEnd(true);
        mRecyclerview.setLayoutManager(manager);

        Intent intent = getIntent();
        mReceiverId = intent.getStringExtra(Utils.ID);
        mSenderUser = FirebaseAuth.getInstance().getCurrentUser();

        //Lay du lieu name, avt, message
        if (mReceiverId != null) {
            FirebaseDatabase.getInstance().getReference(Utils.USERS)
                    .child(mReceiverId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        mUsername.setText(user.getUsername());
                        if (!user.getAvatar().equals(Utils.DEFAULT)) {
                            Glide.with(getApplicationContext()).load(user.getAvatar())
                                    .into(mImgAvt);
                        }
                        if (user.getStatus() == 0) {
                            mStatusOff.setVisibility(View.VISIBLE);
                            mStatusOn.setVisibility(View.GONE);
                        } else if (user.getStatus() == 1) {
                            mStatusOn.setVisibility(View.VISIBLE);
                            mStatusOff.setVisibility(View.GONE);
                        } else {
                            mStatusOff.setVisibility(View.GONE);
                            mStatusOn.setVisibility(View.GONE);
                        }
                        readMessage(mReceiverId, mSenderUser.getUid(), user.getAvatar(), user.getStatus());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            seenMessage(mReceiverId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //update status online
        userStatus(1);

        //Gui tin nhan
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    //Send message len database
                    mIsNotify = true;
                    String message = mMessage.getText().toString().trim();
                    String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    if (Utils.isNetworkAvailable(getApplicationContext())) {
                        sendMessage(message, senderId, mReceiverId);
                    } else {
                        Toast.makeText(MessageActivity.this, R.string.cant_send_message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        //update status offline
        userStatus(0);

        if (mSeenListener != null) {
            FirebaseDatabase.getInstance().getReference(Utils.CHATS).removeEventListener(mSeenListener);
        }
    }

    private void userStatus(int status) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Utils.USERS)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            HashMap<String, Object> map = new HashMap<>();
            map.put(Utils.STATUS, status);
            reference.updateChildren(map);
        }
    }

    private void sendMessage(String message, String senderId, final String receiverId) {
        if (!TextUtils.isEmpty(message)) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(Utils.MESSAGE, message);
            map.put(Utils.SENDER_ID, senderId);
            map.put(Utils.RECEIVER_ID, receiverId);
            map.put(Utils.SEEN, false);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child(Utils.CHATS).push().setValue(map);
            mMessage.setText("");

            final String msg = message;

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                reference = FirebaseDatabase.getInstance().getReference(Utils.USERS).child(
                        FirebaseAuth.getInstance().getCurrentUser().getUid());

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (mIsNotify) {
                                sendNotification(mReceiverId, user.getUsername(), msg);
                            }
                            mIsNotify = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }

    private void sendNotification(final String receiverId, final String username, final String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Utils.TOKENS);
        Query query = tokens.orderByKey().equalTo(mReceiverId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    if (token != null) {
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            Data data = new Data(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    R.mipmap.ic_launcher, username + ": " + msg,
                                    getString(R.string.you_have_message), mReceiverId);

                            Sender sender = new Sender(data, token.getToken());

                            mApiService.sendNotification(sender)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if (response.code() == 200) {
                                                if (response.body().success != 1) {
                                                    Toast.makeText(MessageActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {

                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage(final String receiverId, final String senderId, final String avatarUrl, final int status) {
        mChatList = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference(Utils.CHATS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChatList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat != null) {
                        if (receiverId.equals(chat.getReceiver()) && senderId.equals(chat.getSender()) ||
                                receiverId.equals(chat.getSender()) && senderId.equals(chat.getReceiver())) {
                            mChatList.add(chat);
                        }

                        mChatAdapter = new ChatAdapter(MessageActivity.this, mChatList, avatarUrl, status);
                        mRecyclerview.setAdapter(mChatAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenMessage(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Utils.CHATS);
        mSeenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat != null) {
                        if (chat.getReceiver().equals(mSenderUser.getUid()) && chat.getSender().equals(userId)) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(Utils.SEEN, true);
                            dataSnapshot.getRef().updateChildren(map);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
