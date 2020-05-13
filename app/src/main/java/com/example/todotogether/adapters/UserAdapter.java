package com.example.todotogether.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todotogether.R;
import com.example.todotogether.models.User;

import java.util.List;

public class UserAdapter extends  RecyclerView.Adapter<UserAdapter.UserHolder> {

    private List<User> mUsers;

    public UserAdapter(List<User> mUsers) {
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_search,parent,false);
        return new UserHolder(v);
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

    class UserHolder extends RecyclerView.ViewHolder {

        private TextView tvUserEmail;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
        }
    }
}
