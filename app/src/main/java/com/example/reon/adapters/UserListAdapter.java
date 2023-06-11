package com.example.reon.adapters;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.classes.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> implements Filterable {

    private final String TAG = "reon_UserListAdapter";
    private ArrayList<User> users;
    private ArrayList<User> allUsers;
    private final Context context;
    private final UserListAdapter.OnUserListener listener;

    public UserListAdapter(Context context, ArrayList<User> users, UserListAdapter.OnUserListener listener) {
        sortUsers(users);
        this.users = users;
        this.allUsers = users;
        this.context = context;
        this.listener = listener;
    }

    public void setUsers(ArrayList<User> users) {
        sortUsers(users);
        this.users = users;
        this.allUsers = users;
    }

    public void sortUsers(List<User> users) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(users, Comparator.comparing(User::getName));
            Map<Boolean, List<User>> map = users.stream().collect(Collectors.groupingBy(User::isAdmin));
            List<User> admins = new ArrayList<>();
            List<User> members = new ArrayList<>();
            for ( Map.Entry<Boolean, List<User>> entry : map.entrySet()) {
                Boolean key = entry.getKey();
                if (key)
                    admins.addAll(entry.getValue());
                else
                    members.addAll(entry.getValue());

            }

            users.clear();
            users.addAll(admins);
            users.addAll(members);
            Log.d("reon_UserListAdapter", "Users: " + users);
        }


    }

    @NonNull
    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListAdapter.ViewHolder holder, int position) {
        holder.name.setText(users.get(position).getName());

        User user = users.get(position);
        if(user.isAdmin())
            holder.admin.setText(R.string.admin);
        else
            holder.admin.setText("");

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public Filter getFilter() {
        return fileFilter;
    }

    private final Filter fileFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filteredList.addAll(allUsers);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (User user : allUsers) {
                    if (user.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            users = (ArrayList<User>) results.values;
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView name;
        public ImageView image;
        public TextView admin;

        UserListAdapter.OnUserListener onUserListener;

        public ViewHolder(View view, UserListAdapter.OnUserListener onUserListener) {
            super(view);

            name = view.findViewById(R.id.user_item_name);
            image = view.findViewById(R.id.user_item_image);
            admin = view.findViewById(R.id.user_item_admin);

            this.onUserListener = onUserListener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onUserListener.onUserClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            onUserListener.onUserLongClick(getAdapterPosition());
            return true;
        }
    }

    public interface OnUserListener {
        void onUserClick(int position);
        void onUserLongClick(int position);
    }

}
