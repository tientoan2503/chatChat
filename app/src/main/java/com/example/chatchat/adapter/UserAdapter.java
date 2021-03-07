package com.example.chatchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatchat.R;
import com.example.chatchat.activities.MessageActivity;
import com.example.chatchat.model.Chat;
import com.example.chatchat.model.User;
import com.example.chatchat.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsersList;
    public static final int USER_TYPE_LIST = 0;
    public static final int USER_TYPE_CHAT = 1;
    private int mViewType;
    private String mLastMessage;

    public UserAdapter(Context context, List<User> userList, int viewtype) {
        this.mContext = context;
        this.mUsersList = userList;
        this.mViewType = viewtype;
    }

    @Override
    public int getItemViewType(int position) {
        if (mViewType == USER_TYPE_CHAT) {
            return USER_TYPE_CHAT;
        } else {
            return USER_TYPE_LIST;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == USER_TYPE_CHAT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.user_chat_item, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        }
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsersList.get(position);
        final String id = user.getId();
        holder.mTvUserName.setText(user.getUsername());
        if (!user.getAvatar().equals(Utils.DEFAULT)) {
            Glide.with(mContext).load(user.getAvatar()).into(holder.mImgAvt);
        }
        //Kiem tra user online hay khong
        if (user.getStatus() == 1) {
            holder.mStatusOff.setVisibility(View.GONE);
            holder.mStatusOn.setVisibility(View.VISIBLE);
        } else if (user.getStatus() == 0) {
            holder.mStatusOff.setVisibility(View.VISIBLE);
            holder.mStatusOn.setVisibility(View.GONE);
        } else {
            holder.mStatusOff.setVisibility(View.GONE);
            holder.mStatusOn.setVisibility(View.GONE);
        }

        //Click vao avatar mo message
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra(Utils.ID, id);
                mContext.startActivity(intent);
                if (!Utils.isNetworkAvailable(mContext)) {
                    Toast.makeText(mContext, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Hien thi last message
        holder.lastMessage(user.getId(), holder.lastMessage);
    }

    @Override
    public int getItemCount() {
        return mUsersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView mImgAvt, mStatusOn, mStatusOff;
        public TextView mTvUserName, lastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTvUserName = itemView.findViewById(R.id.user_name);
            mImgAvt = itemView.findViewById(R.id.user_avt);
            mStatusOn = itemView.findViewById(R.id.user_status_on);
            mStatusOff = itemView.findViewById(R.id.user_status_off);
            lastMessage = itemView.findViewById(R.id.last_message);

        }

        private void lastMessage(final String id, final TextView lastMsg) {
            mLastMessage = Utils.DEFAULT;
            final FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Utils.CHATS);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        if (chat != null) {
                            if (mCurrentUser != null) {
                                if (chat.getReceiver().equals(mCurrentUser.getUid()) && chat.getSender().equals(id) ||
                                        chat.getSender().equals(mCurrentUser.getUid()) && chat.getReceiver().equals(id)) {
                                    // Nếu tin nhắn cuối là mình gửi thì thêm chữ "You"
                                    if (chat.getSender().equals(mCurrentUser.getUid())) {
                                        mLastMessage = mContext.getString(R.string.you) + chat.getMessage();
                                    } else {
                                        mLastMessage = chat.getMessage();
                                        // Check xem tin nhắn xem chưa thì để màu đỏ
                                        if (chat.isSeen()) {
                                            lastMsg.setTextColor(Color.GRAY);
                                            lastMsg.setTypeface(null, Typeface.NORMAL);
                                        } else {
                                            lastMsg.setTextColor(Color.RED);
                                            lastMsg.setTypeface(null, Typeface.BOLD);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!Utils.DEFAULT.equals(mLastMessage)) {
                        lastMsg.setText(mLastMessage);
                    }
                    mLastMessage = Utils.DEFAULT;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
