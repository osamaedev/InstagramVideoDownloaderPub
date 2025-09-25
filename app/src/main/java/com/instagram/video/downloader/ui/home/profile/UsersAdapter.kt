package com.instagram.video.downloader.ui.home.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.instagram.video.downloader.R
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.data.local.room.entities.User
import com.instagram.video.downloader.databinding.UserItemBinding
import com.instagram.video.downloader.ui.base.BaseViewHolder

class UsersAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var users: List<User> = arrayListOf()
    var currentLoggedInUser: LoggedInUser? = null
    var onItemListener: OnItemListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return UserItemViewHolder(
            UserItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) = holder.onBind(position)

    override fun getItemCount() = users.size

    inner class UserItemViewHolder(private val userItemBinding: UserItemBinding) :
        BaseViewHolder(userItemBinding.root) {
        override fun onBind(position: Int) {
            users[position].let { user ->
                if (user.identifier == currentLoggedInUser?.id)
                    userItemBinding.isSelected.setImageResource(R.drawable.ic_user_selected)
                else
                    userItemBinding.isSelected.setImageResource(R.drawable.ic_user_unselected)
                userItemBinding.fullName.text = user.fullName
                userItemBinding.username.text = user.username
                userItemBinding.storagePath.text =
                    String.format("Internal Storage/{MediaType}/${user.username}")
                Glide.with(userItemBinding.root.context)
                    .load(user.profilePicUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(userItemBinding.userPicture)


                userItemBinding.container.setOnClickListener {
                    onItemListener?.onUserProfileClick(
                        user,
                        currentLoggedInUser!!,
                        userItemBinding
                    )
                }

                userItemBinding.delete.setOnClickListener {
                    onItemListener?.onUserProfileDeleteClick(
                        user
                    )
                }
            }
        }
    }
}

interface OnItemListener {
    fun onUserProfileClick(
        user: User,
        currentLoggedInUser: LoggedInUser,
        userItemBinding: UserItemBinding
    )

    fun onUserProfileDeleteClick(user: User)
}