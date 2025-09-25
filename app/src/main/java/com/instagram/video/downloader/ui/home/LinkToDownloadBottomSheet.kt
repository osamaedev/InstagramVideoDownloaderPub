package com.instagram.video.downloader.ui.home

import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.isHighlightShareLink
import com.instagram.video.downloader.common.checkInstagramLink
import com.instagram.video.downloader.common.cleanShareLink
import com.instagram.video.downloader.common.isAudioLink
import com.instagram.video.downloader.common.isValidInstagramLink
import com.instagram.video.downloader.databinding.DialogLinkToDownloadBinding
import com.instagram.video.downloader.ui.base.BaseBottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LinkToDownloadBottomSheet : BaseBottomSheetDialog<DialogLinkToDownloadBinding>() {

    companion object {
        const val TAG = "LinkToDownloadBottomSheet"

        fun getInstance(bundle: Bundle) = LinkToDownloadBottomSheet().apply {
            arguments = bundle
        }
    }

    override fun getLayoutId() = R.layout.dialog_link_to_download

    private val viewModel: HomeViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.close.setOnClickListener { dismiss() }

        val clipBoard = baseActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipBoard.primaryClip?.getItemAt(0)?.let { link ->
            if (isValidInstagramLink(link.text.toString())) {
                binding.linkInput.editText?.setText(link.text)
            }
        }

        binding.linkInput.setEndIconOnClickListener { pastClipboardValue() }

        binding.download.setOnClickListener {
            binding.linkInput.error = null

            if (binding.linkInput.editText?.text?.isEmpty() == true) {
                binding.linkInput.error = getString(R.string.please_enter_a_link)
                return@setOnClickListener
            }

            val link = binding.linkInput.editText?.text.toString()

            val isAudio = isAudioLink(link)

            if (binding.linkInput.editText?.text?.isNotEmpty() == true
                && !checkInstagramLink(link) && !isHighlightShareLink(link) && !isAudio
            ) {
                binding.linkInput.error = getString(R.string.please_enter_a_valid_link)
                return@setOnClickListener
            }
            viewModel.onStartDownload(cleanShareLink(binding.linkInput.editText?.text.toString()))
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.linkInput.requestFocus()
    }

    private fun pastClipboardValue() {
        val clipBoard = baseActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipBoard.primaryClip?.getItemAt(0)?.let { link ->
            binding.linkInput.editText?.setText(link.text)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as BottomSheetDialog
            val bottomSheet =
                d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            bottomSheet.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
        return dialog
    }

}