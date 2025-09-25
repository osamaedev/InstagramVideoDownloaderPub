package com.instagram.video.downloader.ui.home.explore.collection

import android.os.Bundle
import android.os.Parcelable
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.common.DataReference
import com.instagram.video.downloader.common.TempPlayerDataModel
import com.instagram.video.downloader.common.download.VDDownloadManager
import com.instagram.video.downloader.common.toPlayerDataModel
import com.instagram.video.downloader.common.ui.views.InstaDownloaderCircularProgressView
import com.instagram.video.downloader.data.remote.api.dto.hasValidSubscription
import com.instagram.video.downloader.data.remote.instaApi.dto.collections.CollectionMedia
import com.instagram.video.downloader.databinding.FragmentCollectionBinding
import com.instagram.video.downloader.ui.base.BaseFragment
import com.instagram.video.downloader.ui.home.HomeActivity
import com.instagram.video.downloader.ui.home.explore.collection.adapter.CollectionAdapter
import com.instagram.video.downloader.ui.home.explore.explore.adapter.ExploreLoadStateAdapter
import com.instagram.video.downloader.ui.home.HomeViewModel
import com.instagram.video.downloader.ui.home.explore.collection.adapter.CollectionUiModel
import com.instagram.video.downloader.ui.home.explore.collection.adapter.OnCollectionListener
import com.instagram.video.downloader.ui.player.PlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class CollectionFragment : BaseFragment<FragmentCollectionBinding>(), OnCollectionListener {


    companion object {
        const val TAG = "CollectionFragment"
    }

    private val viewModel: HomeViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_collection

    private val adapter = CollectionAdapter()
    private val loadAdapter = ExploreLoadStateAdapter()

    @Inject
    lateinit var downloadManager: VDDownloadManager

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private var recyclerViewState: Parcelable? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressView.setContent {
            InstaDownloaderCircularProgressView()
        }
        binding.cookiePolicy.movementMethod = LinkMovementMethod.getInstance()

        binding.refreshLayout.setOnRefreshListener {
            viewModel.onCollectionAction(CollectionAction.OnSwipeRefresh)
        }

        binding.reload.setOnClickListener { viewModel.onCollectionAction(CollectionAction.OnReloadClick) }
        binding.instagramLogin.setOnClickListener { viewModel.onCollectionAction(CollectionAction.OnShowInstagramLoginDialog) }

        baseActivity()?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // NO-OP
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.batch_download_close -> adapter.disableSelection()
                    R.id.collection_mass_download -> {
                        val itemsToDownload = adapter.getAllSelectedItems()
                        if (itemsToDownload.isNotEmpty()) {
                            adapter.snapshot().items.forEachIndexed { _, item ->
                                if (item is CollectionUiModel.MediaItem && itemsToDownload.contains(
                                        item
                                    )
                                ) {
                                    item.collectionItem.isDownloading = true
                                }
                            }
                            adapter.selectedItems.value.forEach { index ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    adapter.notifyItemChanged(index)
                                }
                            }
                            adapter.disableSelection()
                            viewModel.handleCollectionMassDownload(itemsToDownload)
                        } else {
                            showMessage(R.string.select_items_to_start_download)
                        }
                    }
                }
                return false
            }
        })

        lifecycleScope.launch {
            viewModel.resetSelection.collect {
                if (it) {
                    adapter.disableSelection()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.collectionChannel.collect { event ->
                when (event) {
                    CollectionAction.OnReloadClick, CollectionAction.OnSwipeRefresh -> adapter.refresh()
                    CollectionAction.OnShowInstagramLoginDialog -> {}
                    CollectionAction.HideAds -> {
                        // Remove ads
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.massDownloadRewarded.collect {
                if (it) {
                    (baseActivity() as HomeActivity).updateSelectedItemsCount(adapter.selectedItemsCount.value)
                    viewModel.massDownloadRewarded.tryEmit(false)
                }
            }
        }

        lifecycleScope.launch {
            viewModel
                .massDownloadItemDownloadingInfo
                .collect { info ->
                    adapter.snapshot()
                        .items
                        .firstOrNull { it is CollectionUiModel.MediaItem && it.collectionItem.id == info.instagramId }
                        ?.let { mediaItem ->
                            Timber.tag("CollectionFragment")
                                .d("Progress: ${info.currentProgress}")
                            if (info.currentProgress >= 100.0) {
                                (mediaItem as CollectionUiModel.MediaItem).collectionItem.downloadProgress =
                                    100.0
                                mediaItem.collectionItem.isDownloading = true
                                adapter.notifyItemChanged(
                                    adapter.snapshot().items.indexOf(mediaItem)
                                )
                                handler.postDelayed({
                                    mediaItem.collectionItem.isDownloaded = true
                                    mediaItem.collectionItem.isDownloading = false
                                    adapter.notifyItemChanged(
                                        adapter.snapshot().items.indexOf(mediaItem)
                                    )
                                }, 1000)
                            } else {
                                (mediaItem as CollectionUiModel.MediaItem).collectionItem.isDownloading =
                                    true
                                mediaItem.collectionItem.downloadProgress = info.currentProgress
                                adapter.notifyItemChanged(
                                    adapter.snapshot().items.indexOf(mediaItem)
                                )
                            }
                        }
                }
        }

        lifecycleScope.launch {
            adapter
                .selectedItemsCount
                .collect {
                if (viewModel.currentRemoteUser != null
                    && it > viewModel.currentRemoteUser?.remainingMassDownloadCount!!
                    && viewModel.currentRemoteUser?.hasValidSubscription() == false
                ) {
                    adapter.removeLastItemFromSelection()
                    (baseActivity() as HomeActivity).showReachedMaxMassDownloadCount()
                } else {
                    baseActivity()?.runOnUiThread {
                        (baseActivity() as HomeActivity).updateSelectedItemsCount(it)
                    }
                }
            }
        }

        lifecycleScope.launch {
            adapter.isMultiSelectMode.collect { isSelection ->
                if (activity == null)
                    return@collect
                if (isSelection) {
                    (activity as HomeActivity).showCollectionMassDownloadMenu()
                } else {
                    (activity as HomeActivity).showDefaultMenu()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.collectionState.collect { state ->
                if (state.error != null) showMessage(state.error!!)

                if (state.isLoading) showLoading() else hideLoading()

                binding.loginRequiredSection.isVisible =
                    state.isLoading.not() && (state.loggedInUser == null || state.loggedInUser?.id == -1L)

                binding.refreshLayout.isRefreshing = state.isSwipeLoading

                if (state.loggedInUser != null && state.loggedInUser?.id != -1L && state.isSwipeLoading.not() && adapter.snapshot()
                        .isEmpty()
                )
                    initCollectionItems()

                if (state.massMediaToDownload != null && state.isMassDownloading) {
                    CoroutineScope(Dispatchers.IO).launch {
                        downloadManager.addToDownloadItems(state.massMediaToDownload!!)
                    }
                }

            }
        }

        onBackPressedCallback = object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                if (adapter.isMultiSelectMode.value) {
                    adapter.disableSelection()
                } else {
                    onBackPressedCallback.remove()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }


    override fun onResume() {
        if (recyclerViewState != null && adapter.isMultiSelectMode.value.not()) {
            binding.collectionRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
        }
        super.onResume()
    }

    override fun onPause() {
        if (!adapter.isMultiSelectMode.value) {
            recyclerViewState = binding.collectionRecyclerView.layoutManager?.onSaveInstanceState()
        }
        super.onPause()
    }

    private fun initCollectionItems() {
        adapter.withLoadStateFooter(loadAdapter)
        adapter.collectionListener = this
        val layoutManager = GridLayoutManager(requireContext(), 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    R.layout.collection_media_item -> 1
                    R.layout.player_native_ad_item -> 3
                    else -> 3
                }
            }
        }

        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        binding.collectionRecyclerView.itemAnimator = itemAnimator


        binding.collectionRecyclerView.layoutManager = layoutManager
        binding.collectionRecyclerView.adapter = adapter

        adapter.addLoadStateListener { loadState ->
            binding.progressView.isVisible =
                loadState.refresh is LoadState.Loading

            binding.collectionRecyclerView.isVisible =
                (loadState.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading)
                        && adapter.itemCount != 0

            binding.errorStatus.isVisible =
                loadState.mediator?.refresh is LoadState.Error && adapter.itemCount == 0

            if (loadState.mediator?.refresh is LoadState.NotLoading && adapter.itemCount == 0) {
                binding.reload.isVisible = false
                binding.errorStatus.isVisible = false
            }

            binding.reload.isVisible =
                !loadState.source.hasError && adapter.itemCount == 0 && loadState.source.refresh is LoadState.NotLoading
            binding.statusContainer.isVisible =
                (loadState.source.hasError
                        || loadState.mediator?.hasError == true
                        || loadState.refresh is LoadState.Error
                        || adapter.itemCount == 0)
                        && loadState.source.refresh is LoadState.NotLoading
            binding.errorStatus.isVisible =
                (loadState.source.hasError
                        || loadState.mediator?.hasError == true
                        || loadState.refresh is LoadState.Error
                        || adapter.itemCount == 0)
                        && loadState.source.refresh is LoadState.NotLoading

            if ((loadState.source.hasError || adapter.itemCount == 0) && loadState.source.refresh is LoadState.NotLoading) {
                binding.errorStatus.text = getString(R.string.collection_no_items_found)
            }


            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
                ?: loadState.refresh as? LoadState.Error

            if (errorState != null) {
                binding.errorStatus.isVisible = true
                binding.statusContainer.isVisible = true
            }

            if (errorState?.error is HttpException) {
                when ((errorState.error as HttpException).code()) {
                    400 -> {
                        if (adapter.snapshot().isEmpty())
                            binding.errorStatus.text = getString(R.string.failed_to_load_data)
                        // TODO: may open the webView for the verification
                    }

                    401, 403 -> {
                        binding.errorStatus.text = getString(R.string.reached_limit)
                    }

                    else -> {
                        binding.errorStatus.text = getString(R.string.failed_to_load_data)
                    }
                }
            } else if (errorState?.error != null) {
                if (adapter.snapshot().isEmpty()) {
                    binding.errorStatus.text = getString(R.string.failed_to_load_data)
                }
            }
        }

        lifecycleScope.launch {
            viewModel
                .collectionDataFlow
                .catch {
                    Timber.tag(TAG).e(it)
                }
                .collect { data ->
                    adapter.submitData(data)
                }
        }
    }


    override fun onCollectionClick(media: CollectionMedia) {
        val collectionItems = adapter.snapshot().filterIsInstance<CollectionUiModel.MediaItem>()
            .map { it.collectionItem.toPlayerDataModel() }
        val scrollTo = adapter.snapshot().filterIsInstance<CollectionUiModel.MediaItem>()
            .indexOfFirst { it.collectionItem == media }
        val tempData = TempPlayerDataModel(ArrayList(collectionItems))
        val sessionId = UUID.randomUUID().toString()
        DataReference<TempPlayerDataModel>(sessionId = sessionId).save(
            VideoDownloaderApp.application,
            tempData
        )
        startActivity(PlayerActivity.getIntent(baseActivity()!!, sessionId, scrollTo))
    }
}