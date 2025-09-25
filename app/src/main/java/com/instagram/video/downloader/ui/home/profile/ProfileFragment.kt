package com.instagram.video.downloader.ui.home.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.instagram.video.downloader.R
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.data.local.room.entities.User
import com.instagram.video.downloader.databinding.FragmentProfileBinding
import com.instagram.video.downloader.databinding.UserItemBinding
import com.instagram.video.downloader.ui.base.BaseAlertDialog
import com.instagram.video.downloader.ui.base.BaseFragment
import com.instagram.video.downloader.ui.home.HomeAction
import com.instagram.video.downloader.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(), OnItemListener {


    private val viewModel: HomeViewModel by activityViewModels()
    private val adapter = UsersAdapter()

    override fun getLayoutId() = R.layout.fragment_profile


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listener = View.OnClickListener { viewModel.onHomeAction(HomeAction.OnAddNewUser) }
        binding.addUser.setOnClickListener(listener)
        binding.noUsers.setOnClickListener(listener)

        lifecycleScope.launch {
            viewModel.profilesDataFlow.collect {
                adapter.currentLoggedInUser = viewModel.currentLoggedInUser.value
                adapter.users = it
                initUsers()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initUsers() {
        baseActivity()?.runOnUiThread {
            if (adapter.users.isEmpty()) {
                binding.noUsers.isVisible = true
                binding.list.isVisible = false
            } else {
                binding.noUsers.isVisible = false
                binding.list.isVisible = true
            }
            binding.list.adapter = adapter
            adapter.onItemListener = this@ProfileFragment
            binding.list.setHasFixedSize(true)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onUserProfileClick(
        user: User,
        currentLoggedInUser: LoggedInUser,
        userItemBinding: UserItemBinding
    ) {
        if (user.identifier != currentLoggedInUser.id) {
            val dialog = BaseAlertDialog(baseActivity()!!)
            dialog.setTitle(R.string.profile_switch)
            dialog.setMessage(getString(R.string.profile_switch_message, user.username))
            dialog.addButton(R.string.yes, isPrimary = true, isDestructive = false) { _, _ ->
                viewModel.switchTo(user, currentLoggedInUser)
            }
            dialog.setExtraCloseButtonVisible(true)
            dialog.show()
        }
    }

    override fun onUserProfileDeleteClick(user: User) {
        val deleteDialog = BaseAlertDialog(baseActivity()!!)
        deleteDialog.setTitle(R.string.delete_profile_title)
        deleteDialog.setMessage(getString(R.string.delete_profile_message, user.username))
        deleteDialog.addButton(R.string.yes, isPrimary = true, isDestructive = true) { _, _ ->
            viewModel.deleteProfileUser(user)
        }
        deleteDialog.setExtraCloseButtonVisible(true)
        deleteDialog.show()
    }
}