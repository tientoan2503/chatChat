package com.example.chatchat.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatchat.R;
import com.example.chatchat.utils.Utils;
import com.example.chatchat.dialog.DialogCustom;
import com.example.chatchat.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private CircleImageView mAvatar;
    private TextView mUsername;
    private Button mBtnAddAvt, mBtnEdtName, mBtnChangePass, mBtnLogout, mBtnTurnOffStatus, mBtnAbout;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;
    private StorageTask mUploadTask;
    private StorageReference mStorageReference;
    private Uri mAvtUri;
    private FirebaseUser mCurrentUser;
    private static final int OPEN_AVT_REQUEST_CODE = 1;
    private int mStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        mAvatar = findViewById(R.id.big_avt);
        mUsername = findViewById(R.id.user_name);
        mBtnAddAvt = findViewById(R.id.btn_add_avt);
        mBtnEdtName = findViewById(R.id.btn_edit_username);
        mBtnChangePass = findViewById(R.id.btn_change_password);
        mBtnLogout = findViewById(R.id.btn_logout);
        mBtnTurnOffStatus = findViewById(R.id.btn_turn_off_status);
        mProgressBar = findViewById(R.id.progress_bar);
        mBtnAbout = findViewById(R.id.btn_about);

        mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference(Utils.UPLOADS);
        mCurrentUser = mAuth.getCurrentUser();


        //Lấy dữ liệu user đang sử dụng để đổ vào avatar và name
        getUser();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Update status
        userStatus(1);

        //Thay đổi avatar
        mBtnAddAvt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    setAvatar();
                } else {
                    Toast.makeText(SettingActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Logout
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userStatus(0);
                mAuth.signOut();
                mProgressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                MainActivity.mActivity.finish();
            }
        });

        //Thay doi ten
        mBtnEdtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    rename();
                } else {
                    Toast.makeText(SettingActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Thay doi pass
        mBtnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    changePassword();
                }
            }
        });

        //set lai text cho btn turn on off status
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Utils.USERS)
                .child(mCurrentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    mStatus = user.getStatus();
                    if (user.getStatus() == -1) {
                        mBtnTurnOffStatus.setText(R.string.turn_on_status);
                    } else {
                        mBtnTurnOffStatus.setText(R.string.turn_off_status);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Tat bat trang thai hoat dong
        mBtnTurnOffStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    turnOnOffStatus();
                }
            }
        });

        //About =)))
        mBtnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    private void changePassword() {
        DialogCustom renameDialog = new DialogCustom(Utils.PASSWORD_DIALOG_TYPE, getApplicationContext());
        renameDialog.show(getSupportFragmentManager(), "change password dialog");
    }

    private void setAvatar() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, OPEN_AVT_REQUEST_CODE);
    }

    private void rename() {
        DialogCustom renameDialog = new DialogCustom(Utils.RENAME_DIALOG_TYPE, getApplicationContext());
        renameDialog.show(getSupportFragmentManager(), "rename dialog");
    }

    private void getUser() {
        final String id = mCurrentUser.getUid();
        FirebaseDatabase.getInstance().getReference(Utils.USERS).child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getId().equals(id)) {
                                String username = user.getUsername();
                                mUsername.setText(username);
                                if (!user.getAvatar().equals(Utils.DEFAULT)) {
                                    Glide.with(getApplicationContext()).load(user.getAvatar()).into(mAvatar);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadAvatar() {
        final ProgressDialog progressDialog = new ProgressDialog(SettingActivity.this);
        progressDialog.setMessage(getString(R.string.uploading));
        progressDialog.show();

        if (mAvtUri != null) {
            final StorageReference storageReference = mStorageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(mAvtUri));
            mUploadTask = storageReference.putFile(mAvtUri);
            mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String uri = task.getResult().toString();
                        String id = mCurrentUser.getUid();

                        HashMap<String, Object> map = new HashMap<>();
                        map.put(Utils.AVATAR, uri);
                        FirebaseDatabase.getInstance().getReference(Utils.USERS).child(id).updateChildren(map);
                    } else {
                        Toast.makeText(SettingActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SettingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_image_selected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OPEN_AVT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            mAvtUri = data.getData();

            if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(getApplicationContext(), R.string.upload_in_progress, Toast.LENGTH_SHORT).show();
            } else {
                uploadAvatar();
            }
        }
    }

    private void turnOnOffStatus() {
        HashMap<String, Object> map = new HashMap<>();
        if (mStatus == -1) {
            map.put(Utils.STATUS, 1);
        } else {
            map.put(Utils.STATUS, -1);
        }

        FirebaseDatabase.getInstance().getReference(Utils.USERS).
                child(mCurrentUser.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (mStatus == -1) {
                        mBtnTurnOffStatus.setText(R.string.turn_on_status);
                        Toast.makeText(SettingActivity.this, R.string.status_is_off, Toast.LENGTH_SHORT).show();
                    } else {
                        mBtnTurnOffStatus.setText(R.string.turn_off_status);
                        Toast.makeText(SettingActivity.this, R.string.status_is_on, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void about() {
        DialogCustom renameDialog = new DialogCustom(getApplicationContext());
        renameDialog.show(getSupportFragmentManager(), "rename dialog");
    }
}
