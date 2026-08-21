package com.example.seminariofinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface OnContactActionListener {
        void onEdit(Contact contact, int position);
        void onDelete(Contact contact, int position);
    }

    private List<Contact> contactList;
    private OnContactActionListener listener;
    private boolean showActions; // Flag para controlar si se muestran los botones

    public ContactAdapter(List<Contact> contactList, boolean showActions, OnContactActionListener listener) {
        this.contactList = contactList;
        this.showActions = showActions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhone());

        if (showActions) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(contact, position);
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(contact, position);
            });
        } else {
            // Ocultar acciones en MainActivity
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateList(List<Contact> newList) {
        this.contactList = newList;
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageView btnEdit, btnDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvPhone = itemView.findViewById(R.id.tvContactPhone);
            btnEdit = itemView.findViewById(R.id.btnEditContact);
            btnDelete = itemView.findViewById(R.id.btnDeleteContact);
        }
    }
}