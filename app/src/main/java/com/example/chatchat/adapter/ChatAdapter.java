package com.example.chatchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatchat.R;
import com.example.chatchat.utils.Utils;
import com.example.chatchat.model.Chat;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context mContext;
    private List<Chat> mChatList;
    private String mAvatar;
    private int mStatus;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    public ChatAdapter(Context context, List<Chat> chatList, String avatar, int status) {
        this.mContext = context;
        this.mChatList = chatList;
        this.mAvatar = avatar;
        this.mStatus = status;
    }

    @Override
    public int getItemViewType(int position) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (mChatList.get(position).getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                return MSG_TYPE_RIGHT;
            } else {
                return MSG_TYPE_LEFT;
            }
        }
        return MSG_TYPE_LEFT;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ChatAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        Chat chat = mChatList.get(position);
        holder.mChat.setText(chat.getMessage());
        if (!mAvatar.equals(Utils.DEFAULT)) {
            Glide.with(mContext).load(mAvatar).into(holder.mImgAvt);
        }
        if (mStatus == 1) {
            holder.mStatusOn.setVisibility(View.VISIBLE);
            holder.mStatusOff.setVisibility(View.GONE);
        } else if (mStatus == 0) {
            holder.mStatusOn.setVisibility(View.GONE);
            holder.mStatusOff.setVisibility(View.VISIBLE);
        } else {
            holder.mStatusOn.setVisibility(View.GONE);
            holder.mStatusOff.setVisibility(View.GONE);
        }

        //Chi hien thi seen o tin nhan cuoi cung
        if (position == mChatList.size() - 1) {
            if (chat.isSeen() == true) {
                Glide.with(mContext).load(mAvatar).into(holder.mImgSeen);
            }
        } else {
            holder.mImgSeen.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView mImgAvt, mStatusOn, mStatusOff, mImgSeen;
        public TextView mChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mChat = itemView.findViewById(R.id.message);
            mImgAvt = itemView.findViewById(R.id.avt_user);
            mStatusOff = itemView.findViewById(R.id.status_off);
            mStatusOn = itemView.findViewById(R.id.status_on);
            mImgSeen = itemView.findViewById(R.id.img_seen);
        }
    }

}

