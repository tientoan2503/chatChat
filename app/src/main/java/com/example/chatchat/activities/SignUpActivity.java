package com.example.chatchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatchat.R;
import com.example.chatchat.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mFireAuth;
    private ProgressBar mProgressBar;
    private EditText mEdtEmail, mEdtPassword, mEdtConfirm, mEdtUserName;
    private Button mBtnSignUp;
    private DatabaseReference mReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mProgressBar = findViewById(R.id.progress_bar);
        mEdtUserName = findViewById(R.id.edt_user_name);
        mEdtEmail = findViewById(R.id.edt_email);
        mEdtPassword = findViewById(R.id.edt_password);
        mEdtConfirm = findViewById(R.id.edt_confirm_password);
        mBtnSignUp = findViewById(R.id.btn_sign_up);
        mFireAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Dang ki tai khoan
        if (Utils.isNetworkAvailable(getApplicationContext())) {
            signUp();
        } else {
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    //Đăng kí tài khoản
    private void signUp() {
        mBtnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = mEdtUserName.getText().toString().trim();
                final String email = mEdtEmail.getText().toString().trim();
                final String password = mEdtPassword.getText().toString().trim();
                String confirmPassword = mEdtConfirm.getText().toString().trim();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    if (TextUtils.isEmpty(username)) {
                        mEdtUserName.setError(getString(R.string.name_required));
                    }

                    if (TextUtils.isEmpty(email)) {
                        mEdtEmail.setError(getString(R.string.email_required));
                    }

                    if (password.length() < 6) {
                        mEdtPassword.setError(getString(R.string.pass_than_6));
                    }

                    if (TextUtils.isEmpty(confirmPassword) || !password.equals(confirmPassword)) {
                        mEdtConfirm.setError(getString(R.string.password_not_match));
                    }
                } else if (TextUtils.isEmpty(confirmPassword) || !confirmPassword.equals(password)) {
                    mEdtConfirm.setError(getString(R.string.confirm_required));
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);

                    //Xử lí tạo tài khoản cho user
                    mFireAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mFireAuth.getCurrentUser();
                                assert currentUser != null;
                                String userId = currentUser.getUid();

                                //Đẩy dữ liệu user vào database
                                mReference = FirebaseDatabase.getInstance().getReference(Utils.USERS).child(userId);
                                Map<String, Object> hashMap = new HashMap<>();
                                hashMap.put(Utils.ID, userId);
                                hashMap.put(Utils.USER_NAME, username);
                                hashMap.put(Utils.PASSWORD, password);
                                hashMap.put(Utils.EMAIL, email);
                                hashMap.put(Utils.AVATAR, Utils.DEFAULT);
                                hashMap.put(Utils.STATUS, 1);
                                hashMap.put(Utils.SEARCH, username.toLowerCase());

                                mReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });

                            } else {
                                Toast.makeText(SignUpActivity.this, R.string.user_exist, Toast.LENGTH_SHORT).show();
                            }
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }
}
