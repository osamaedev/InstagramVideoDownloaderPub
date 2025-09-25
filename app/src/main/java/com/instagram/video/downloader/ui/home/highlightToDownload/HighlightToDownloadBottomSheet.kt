package com.instagram.video.downloader.ui.home.highlightToDownload

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.instagram.video.downloader.R
import com.instagram.video.downloader.databinding.DialogHighlightToDownloadBinding
import com.instagram.video.downloader.ui.base.BaseBottomSheetDialog
import com.instagram.video.downloader.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HighlightToDownloadBottomSheet : BaseBottomSheetDialog<DialogHighlightToDownloadBinding>() {


    companion object {
        const val TAG = "HighlightToDownloadBottomSheet"

        fun getInstance(data: Bundle) = HighlightToDownloadBottomSheet().apply {
            arguments = data
        }
    }

    override fun getLayoutId() = R.layout.dialog_highlight_to_download

    private val adapter: HToDownloadAdapter = HToDownloadAdapter()
    private val viewModel: HomeViewModel by activityViewModels()

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.setHasStableIds(true)
        adapter.highlights = viewModel.highlightsToChoose.second
        binding.items.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.download.setOnClickListener {
            if (adapter.getAllSelectedItems().isEmpty()) {
                showMessage(R.string.select_at_least_one_item)
                return@setOnClickListener
            }
            viewModel.onHighlightsStartFilesDownload(
                viewModel.highlightsToChoose.first!!,
                adapter.getAllSelectedItems()
            )
            dismiss()
        }

        binding.selectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) adapter.selectAll() else adapter.unSelectAll()
            }
        }

    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as BottomSheetDialog
            val bottomSheet =
                d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            bottomSheet.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
        return dialog
    }
}