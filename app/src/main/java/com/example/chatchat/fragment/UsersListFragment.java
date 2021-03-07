package com.example.chatchat.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatchat.R;
import com.example.chatchat.adapter.UserAdapter;
import com.example.chatchat.model.User;
import com.example.chatchat.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersListFragment extends Fragment {

    private RecyclerView mRecyclerview;
    private List<User> mUsersList;
    private UserAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        mUsersList = new ArrayList<>();

        mRecyclerview = view.findViewById(R.id.recycler_view_users);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerview.setLayoutManager(linearLayoutManager);

        //lấy dữ liệu ng dùng đổ lên recycler view
        fetchDataUser();

        return view;
    }

    //Lấy dữ liệu user trên database về
    private void fetchDataUser() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Utils.USERS);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mUsersList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            if (!user.getId().equals(currentUser.getUid())) {
                                mUsersList.add(user);
                            }
                        }
                    }
                    mAdapter = new UserAdapter(getContext(), mUsersList, UserAdapter.USER_TYPE_LIST);
                    mRecyclerview.setAdapter(mAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();


    }

}
