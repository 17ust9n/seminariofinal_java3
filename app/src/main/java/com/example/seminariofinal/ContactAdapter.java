package com.example.seminariofinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface OnContactActionListener {
        void onContactClick(Contact contact);
        void onEdit(Contact contact, int position);
        void onDelete(Contact contact, int position);
    }

    private List<Contact> contactList;
    private final OnContactActionListener listener;
    private final boolean showActions;

    public ContactAdapter(List<Contact> contactList, boolean showActions, OnContactActionListener listener) {
        this.contactList = contactList != null ? contactList : new ArrayList<>();
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
        holder.bind(contact, position, showActions, listener);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    // Actualización eficiente mediante DiffUtil en lugar de notifyDataSetChanged
    public void updateList(List<Contact> newList) {
        if (newList == null) newList = new ArrayList<>();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ContactDiffCallback(this.contactList, newList));
        this.contactList.clear();
        this.contactList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void removeContact(int position) {
        if (position >= 0 && position < contactList.size()) {
            contactList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAvatar;
        ImageView btnEdit, btnDelete, ivKeyBadge; // ivKeyBadge opcional para el estado criptográfico

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvPhone = itemView.findViewById(R.id.tvContactPhone);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            btnEdit = itemView.findViewById(R.id.btnEditContact);
            btnDelete = itemView.findViewById(R.id.btnDeleteContact);
            ivKeyBadge = itemView.findViewById(R.id.ivKeyBadge); // Añadir en el layout XML si deseas mostrar el candado
        }

        public void bind(Contact contact, int position, boolean showActions, OnContactActionListener listener) {
            tvName.setText(contact.getName());
            tvPhone.setText(contact.getPhone());

            // Avatar basado en la inicial
            if (contact.getName() != null && !contact.getName().trim().isEmpty()) {
                tvAvatar.setText(contact.getName().trim().substring(0, 1).toUpperCase());
            } else {
                tvAvatar.setText("#");
            }

            // Indicador de clave pública (Si tu layout incluye ivKeyBadge)
            if (ivKeyBadge != null) {
                boolean hasPublicKey = contact.getPublicKey() != null && !contact.getPublicKey().trim().isEmpty();
                ivKeyBadge.setVisibility(hasPublicKey ? View.VISIBLE : View.GONE);
            }

            // Click general
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onContactClick(contact);
            });

            // Acciones de edición / borrado
            if (showActions) {
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);

                btnEdit.setOnClickListener(v -> {
                    if (listener != null) listener.onEdit(contact, position);
                });

                btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDelete(contact, position);
                });
            } else {
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
            }
        }
    }

    // Callback para comparar listas de contactos óptimamente
    private static class ContactDiffCallback extends DiffUtil.Callback {
        private final List<Contact> oldList;
        private final List<Contact> newList;

        public ContactDiffCallback(List<Contact> oldList, List<Contact> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // Asume que el teléfono o un ID único identifica al contacto
            return oldList.get(oldItemPosition).getPhone().equals(newList.get(newItemPosition).getPhone());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Contact oldContact = oldList.get(oldItemPosition);
            Contact newContact = newList.get(newItemPosition);
            return oldContact.getName().equals(newContact.getName()) &&
                    oldContact.getPhone().equals(newContact.getPhone()) &&
                    java.util.Objects.equals(oldContact.getPublicKey(), newContact.getPublicKey());
        }
    }
}