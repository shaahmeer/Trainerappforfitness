package com.example.trainerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trainerapp.R
import com.example.trainerapp.data.User

class UserAdapter(private val users: List<User>, private val listener: UserClickListener) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    interface UserClickListener {
        fun onUserClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProfile: ImageView = itemView.findViewById(R.id.imageProfile)
        private val textName: TextView = itemView.findViewById(R.id.textName)

        fun bind(user: User) {
            textName.text = user.name
            Glide.with(itemView.context)
                .load(user.profileImage)
                .circleCrop()
                .placeholder(R.drawable.baseline_person_24) // Optional: Placeholder while loading
                .error(R.drawable.baseline_person_24) // Optional: Error image if loading fails
                .into(imageProfile)

            itemView.setOnClickListener {
                listener.onUserClick(user)
            }
        }
    }
}
