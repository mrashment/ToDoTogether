package com.mrashment.todotogether.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrashment.todotogether.R;
import com.mrashment.todotogether.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private List<User> mUsers;
    private OnUserClickListener listener;

    public UserAdapter(List<User> mUsers, OnUserClickListener listener) {
        this.mUsers = mUsers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_search,parent,false);
        return new UserHolder(v,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        holder.tvUserEmail.setText(mUsers.get(position).getEmail());
    }

    public void setmUsers(List<User> users) {
        this.mUsers = users;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvUserEmail;
        private OnUserClickListener onUserClickListener;

        public UserHolder(@NonNull View itemView, OnUserClickListener onUserClickListener) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            this.onUserClickListener = onUserClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onUserClickListener.onUserClick(mUsers.get(getAdapterPosition()));
        }
    }

    public interface OnUserClickListener {
        void onUserClick(User user);
    }
}
