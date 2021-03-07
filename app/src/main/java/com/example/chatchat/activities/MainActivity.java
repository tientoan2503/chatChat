package com.example.chatchat.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.chatchat.R;
import com.example.chatchat.fragment.ChatsFragment;
import com.example.chatchat.fragment.UsersListFragment;
import com.example.chatchat.model.User;
import com.example.chatchat.utils.Utils;
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

    private FirebaseAuth mFireAuth;
    private CircleImageView mImgAvatar;
    private FirebaseUser mUser;
    public static Activity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        mImgAvatar = findViewById(R.id.avatar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //Lấy avatar user đẩy vào icon
        mFireAuth = FirebaseAuth.getInstance();
        mUser = mFireAuth.getCurrentUser();
        if (mUser != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference(Utils.USERS).child(userId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                if (!user.getAvatar().equals(Utils.AVATAR)) {
                                    Glide.with(getApplicationContext()).load(user.getAvatar()).into(mImgAvatar);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        //Add fragment list user lên activity
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        UsersListFragment usersListFragment = new UsersListFragment();
        transaction.add(R.id.frame_layout_list_user, usersListFragment);

        //Add fragment list user da chat len activity
        ChatsFragment chatsFragment = new ChatsFragment();
        transaction.add(R.id.frame_layout_message_list, chatsFragment).commit();

        //Mo activity setting
        mImgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingActivity();
            }
        });

    }

    private void openSettingActivity() {
        if (!Utils.isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(MainActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        userStatus(1);
    }


    @Override
    protected void onPause() {
        super.onPause();
        userStatus(0);
    }

    private void userStatus(int status) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String id = user.getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Utils.USERS)
                    .child(id);
            HashMap<String, Object> map = new HashMap<>();
            map.put(Utils.STATUS, status);
            reference.updateChildren(map);
        }
    }
}
