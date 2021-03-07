package com.example.chatchat.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.chatchat.R;
import com.example.chatchat.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DialogCustom extends AppCompatDialogFragment {

    private EditText mEdtInput, mEdtConfirm;
    private String mType;
    private Context mContext;

    public DialogCustom(String type, Context context) {
        mType = type;
        mContext = context;
    }


    public DialogCustom(Context context) {
        mContext = context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if (mType != null) {
            View view = inflater.inflate(R.layout.dialog_custom, null);
            mEdtInput = view.findViewById(R.id.edt_dialog);
            mEdtConfirm = view.findViewById(R.id.edt_dialog_confirm);
            if (mType.equals(Utils.RENAME_DIALOG_TYPE)) {
                mEdtConfirm.setVisibility(View.GONE);
                mEdtInput.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                mEdtInput.setHint(R.string.new_username);
                builder.setView(view).setTitle(R.string.rename)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!mEdtInput.getText().toString().equals("")) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user != null) {
                                        final String id = user.getUid();
                                        FirebaseDatabase.getInstance().getReference(Utils.USERS)
                                                .child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                String username = mEdtInput.getText().toString().trim();
                                                String searchName = username.toLowerCase();

                                                Map<String, Object> map = new HashMap<>();
                                                map.put(Utils.USER_NAME, username);
                                                map.put(Utils.SEARCH, searchName);
                                                FirebaseDatabase.getInstance().getReference(Utils.USERS).child(id).updateChildren(map)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(mContext, R.string.name_updated, Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(mContext, R.string.name_updated_failed, Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }


                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }
                        });
            } else if (mType.equals(Utils.PASSWORD_DIALOG_TYPE)) {
                mEdtInput.setHint(R.string.password);
                mEdtConfirm.setVisibility(View.VISIBLE);
                builder.setView(view).setTitle(R.string.change_password)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String password = mEdtInput.getText().toString().trim();
                                String confirmPassword = mEdtConfirm.getText().toString().trim();
                                if (!password.equals("") && !confirmPassword.equals("")) {
                                    final String id = FirebaseAuth.getInstance().getUid();

                                    if (password.length() < 6) {
                                        Toast.makeText(mContext, R.string.pass_than_6, Toast.LENGTH_SHORT).show();
                                    } else if (!password.equals(confirmPassword)) {
                                        Toast.makeText(mContext, R.string.password_not_match, Toast.LENGTH_SHORT).show();
                                    } else {
                                        FirebaseDatabase.getInstance().getReference(Utils.USERS)
                                                .child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                Map<String, Object> map = new HashMap<>();
                                                map.put(Utils.PASSWORD, password);
                                                FirebaseDatabase.getInstance().getReference(Utils.USERS).child(id).updateChildren(map).
                                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(mContext, R.string.pass_updated, Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(mContext, R.string.pass_update_fail, Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(mContext, R.string.pass_update_fail, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        } else {
            View view = inflater.inflate(R.layout.dialog_about, null);
            builder.setView(view)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        }
        return builder.create();
    }
}
