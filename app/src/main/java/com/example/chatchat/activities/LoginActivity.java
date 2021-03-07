package com.example.chatchat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatchat.R;
import com.example.chatchat.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText mEdtEmail, mEdtPassword;
    private TextView mTvRegister, mTvResetPassword;
    private Button mBtnLogin;
    private Context mContext;
    private FirebaseAuth mFireAuth;
    private ProgressBar mProgressBar;

    @Override
    protected void onStart() {
        super.onStart();

        mFireAuth = FirebaseAuth.getInstance();
        //Kiểm tra xem đã log in hay chưa
        if (mFireAuth.getCurrentUser() != null) {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Anh xa view
        mEdtEmail = findViewById(R.id.edt_email);
        mEdtPassword = findViewById(R.id.edt_password);
        mTvRegister = findViewById(R.id.tv_i_have_no_acc);
        mBtnLogin = findViewById(R.id.btn_login);
        mProgressBar = findViewById(R.id.progress_bar);
        mTvResetPassword = findViewById(R.id.forgot_password);

        mContext = getApplicationContext();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Mo activity dang ki
        mTvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUp();
            }
        });

        //Dang nhap
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    login();
                } else {
                    Toast.makeText(mContext, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Quen mat khau
        mTvResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    //Mở activity đăng kí
    private void openSignUp() {
        Intent intent = new Intent(mContext, SignUpActivity.class);
        startActivity(intent);
    }

    //Ấn nút login
    private void login() {
        String email = mEdtEmail.getText().toString().trim();
        String password = mEdtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            if (TextUtils.isEmpty(email)) {
                mEdtEmail.setError(getString(R.string.email_required));
            }

            if (TextUtils.isEmpty(password)) {
                mEdtPassword.setError(getString(R.string.pass_required));
            }
        } else if (password.length() < 6) {
            mEdtPassword.setError(getString(R.string.pass_than_6));
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mFireAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(mContext, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(mContext, R.string.email_pass_wrong, Toast.LENGTH_SHORT).show();
                    }
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}