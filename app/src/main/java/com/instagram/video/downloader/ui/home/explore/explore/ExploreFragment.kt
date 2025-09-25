package com.instagram.video.downloader.ui.home.explore.explore

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
import com.instagram.video.downloader.common.Constants.PLAYER_NATIVE_AD_INTERVAL
import com.instagram.video.downloader.common.Constants.SIZE_TO_INCLUDE_NATIVE_PLAYER_AFTER
import com.instagram.video.downloader.common.DataReference
import com.instagram.video.downloader.common.TempPlayerDataModel
import com.instagram.video.downloader.common.download.VDDownloadManager
import com.instagram.video.downloader.common.insertItemEveryNItems
import com.instagram.video.downloader.common.toPlayerDataModel
import com.instagram.video.downloader.common.ui.views.InstaDownloaderCircularProgressView
import com.instagram.video.downloader.data.remote.api.dto.hasValidSubscription
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.Media
import com.instagram.video.downloader.databinding.FragmentExploreBinding
import com.instagram.video.downloader.ui.base.BaseFragment
import com.instagram.video.downloader.ui.home.HomeActivity
import com.instagram.video.downloader.ui.home.explore.explore.adapter.ExploreAdapter
import com.instagram.video.downloader.ui.home.explore.explore.adapter.ExploreLoadStateAdapter
import com.instagram.video.downloader.ui.home.HomeViewModel
import com.instagram.video.downloader.ui.home.explore.explore.adapter.ExploreItemListener
import com.instagram.video.downloader.ui.home.explore.explore.adapter.ExploreUiModel
import com.instagram.video.downloader.ui.player.PlayerActivity
import com.instagram.video.downloader.ui.player.PlayerDataModel
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
class ExploreFragment : BaseFragment<FragmentExploreBinding>(), ExploreItemListener {


    companion object {
        const val TAG = "ExploreFragment"
    }

    private val viewModel: HomeViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_explore

    private lateinit var onBackPressedCallback: OnBackPressedCallback


    private val adapter: ExploreAdapter = ExploreAdapter()
    private val loadAdapter = ExploreLoadStateAdapter()

    @Inject
    lateinit var downloadManager: VDDownloadManager

    private var recyclerViewState: Parcelable? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cookiePolicy.movementMethod = LinkMovementMethod.getInstance()
        binding.progressView.setContent {
            InstaDownloaderCircularProgressView()
        }
        binding.refreshLayout.setOnRefreshListener {
            viewModel.onExploreAction(ExploreAction.OnSwipeRefresh)
        }

        binding.instagramLogin.setOnClickListener {
            viewModel.onExploreAction(ExploreAction.OnShowInstagramLoginDialog)
        }

        baseActivity()?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // NO-OP
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {

                    R.id.batch_download_close -> {
                        adapter.disableSelection()
                    }

                    R.id.explore_mass_download -> {
                        val itemsToDownload = adapter.getAllSelectedItems()
                        if (itemsToDownload.isNotEmpty()) {
                            adapter.snapshot().items.forEachIndexed { _, item ->
                                if (item is ExploreUiModel.MediaItem
                                    && itemsToDownload.contains(item)
                                ) {
                                    item.media.isDownloading = true
                                }
                            }
                            adapter.selectedItems.value.forEach { index ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    adapter.notifyItemChanged(index)
                                }
                            }
                            adapter.disableSelection()
                            viewModel.handleExploreMassDownload(itemsToDownload)
                        } else {
                            showMessage(R.string.select_items_to_start_download)
                        }
                    }
                }
                return false
            }
        })

        lifecycleScope.launch {
            viewModel.massDownloadRewarded.collect {
                if (it) {
                    (baseActivity() as HomeActivity).updateSelectedItemsCount(adapter.selectedItemsCount.value)
                    viewModel.massDownloadRewarded.tryEmit(false)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.resetSelection.collect {
                if (it) {
                    adapter.disableSelection()
                }
            }
        }

        lifecycleScope.launch {
            viewModel
                .massDownloadItemDownloadingInfo
                .collect { info ->
                    adapter.snapshot()
                        .items
                        .firstOrNull { it is ExploreUiModel.MediaItem && it.media.id == info.instagramId }
                        ?.let { mediaItem ->
                            Timber.tag("ExploreFragment").d("Progress: ${info.currentProgress}")
                            if (info.currentProgress >= 100.0) {
                                (mediaItem as ExploreUiModel.MediaItem).media.downloadProgress =
                                    100.0
                                mediaItem.media.isDownloading = true
                                adapter.notifyItemChanged(
                                    adapter.snapshot().items.indexOf(
                                        mediaItem
                                    )
                                )
                                handler.postDelayed({
                                    // set the item as download after 1s
                                    mediaItem.media.isDownloaded = true
                                    mediaItem.media.isDownloading = false
                                    adapter.notifyItemChanged(
                                        adapter.snapshot().items.indexOf(mediaItem)
                                    )
                                }, 1000)
                            } else {
                                (mediaItem as ExploreUiModel.MediaItem).media.isDownloading =
                                    true
                                mediaItem.media.downloadProgress = info.currentProgress
                                adapter.notifyItemChanged(
                                    adapter.snapshot().items.indexOf(
                                        mediaItem
                                    )
                                )
                            }
                        }
                }
        }

        lifecycleScope.launch {
            viewModel.exploreChannel.collect { event ->
                when (event) {
                    ExploreAction.OnSwipeRefresh -> adapter.refresh()
                    ExploreAction.OnShowInstagramLoginDialog -> {}
                    ExploreAction.HideAds -> {
                        // Remove ads
                    }
                }
            }
        }

        lifecycleScope.launch {
            adapter.selectedItemsCount.collect {
                if (viewModel.currentRemoteUser != null
                    && it > viewModel.currentRemoteUser?.remainingMassDownloadCount!!
                    && viewModel.currentRemoteUser?.hasValidSubscription() == false
                ) {
                    // just de-select the last item in the selected list
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
                    (activity as HomeActivity).showExploreMassDownloadMenu()
                } else {
                    (activity as HomeActivity).showDefaultMenu()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.exploreState.collect { state ->

                if (state.error != null) showMessage(state.error!!)

                if (state.isLoading) showLoading() else hideLoading()

                binding.loginRequiredSection.isVisible =
                    state.isLoading.not() && (state.loggedInUser == null || state.loggedInUser?.id == -1L)

                binding.refreshLayout.isRefreshing = state.isSwipeLoading

                if (state.loggedInUser != null && state.loggedInUser?.id != -1L && state.isSwipeLoading.not() && adapter.snapshot()
                        .isEmpty()
                )
                    initExploreItems()

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
            binding.exploreRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
        }
        super.onResume()
    }

    override fun onPause() {
        if (!adapter.isMultiSelectMode.value) {
            recyclerViewState = binding.exploreRecyclerView.layoutManager?.onSaveInstanceState()
        }
        super.onPause()
    }

    private fun initExploreItems() {
        adapter.exploreItemListener = this
        adapter.withLoadStateFooter(loadAdapter)
        val layoutManager = GridLayoutManager(requireContext(), 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    R.layout.explore_media_item -> 1
                    R.layout.player_native_ad_item -> 3
                    else -> -1
                }
            }
        }

        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        binding.exploreRecyclerView.itemAnimator = itemAnimator

        binding.exploreRecyclerView.layoutManager = layoutManager
        binding.exploreRecyclerView.adapter = adapter

        adapter.addOnPagesUpdatedListener {
            if (adapter.isMultiSelectMode.value) {
                adapter.setItemSelectionMode()
            }
        }

        adapter.addLoadStateListener { loadState ->
            binding.progressView.isVisible =
                loadState.refresh is LoadState.Loading

            binding.exploreRecyclerView.isVisible =
                (loadState.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading)
                        && adapter.itemCount != 0

            binding.errorStatus.isVisible =
                loadState.mediator?.refresh is LoadState.Error && adapter.itemCount == 0


            binding.errorStatus.isVisible =
                (loadState.source.hasError
                        || adapter.itemCount == 0
                        || loadState.append is LoadState.Error
                        || loadState.prepend is LoadState.Error
                        || loadState.refresh is LoadState.Error) && loadState.source.refresh is LoadState.NotLoading


            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
                ?: loadState.refresh as? LoadState.Error

            if (errorState?.error is HttpException) {
                when ((errorState.error as HttpException).code()) {
                    400 -> {
                        if (adapter.snapshot().isEmpty())
                            binding.errorStatus.text = getString(R.string.failed_to_load_data)

                        // TODO: may open the webView for the verification
                    }

                    401, 403 -> {
                        binding.errorStatus.isVisible = true
                        binding.errorStatus.text = getString(R.string.reached_limit)
                    }

                    else -> {
                        binding.errorStatus.text = getString(R.string.failed_to_load_data)
                    }
                }
            } else if (errorState?.error != null) {
                if (adapter.snapshot().isEmpty()) {
                    binding.errorStatus.isVisible = true
                    binding.errorStatus.text = getString(R.string.failed_to_load_data)
                }
            }
        }

        lifecycleScope.launch {
            viewModel
                .exploreDataFlow
                .catch {
                    Timber.tag(TAG).e(it)
                }
                .collect { exploreData ->
                    adapter.submitData(exploreData)
                }
        }
    }

    override fun onExploreItemClick(media: Media) {
        var explorePosts = adapter.snapshot().items.filterIsInstance<ExploreUiModel.MediaItem>()
            .map { it.media.toPlayerDataModel() }
        val scrollTo = adapter.snapshot().items.filterIsInstance<ExploreUiModel.MediaItem>()
            .indexOfFirst { it.media == media }

//        if (explorePosts.size > SIZE_TO_INCLUDE_NATIVE_PLAYER_AFTER) {
//            viewModel.appConfig.value?.advertisements?.forEach {
//                when {
//                    it.isEnabled && it.name == "player_native_1" && it.type == "native" && it.provider == "admob" -> {
//                        explorePosts = insertItemEveryNItems(explorePosts.toMutableList(), PlayerDataModel.AdMediaItem(it.provider, it.adId), PLAYER_NATIVE_AD_INTERVAL)
//                    }
//                }
//            }
//        }

        val tempData = TempPlayerDataModel(ArrayList(explorePosts))
        val sessionId = UUID.randomUUID().toString()
        DataReference<TempPlayerDataModel>(sessionId = sessionId).save(
            VideoDownloaderApp.application,
            tempData
        )
        startActivity(PlayerActivity.getIntent(baseActivity()!!, sessionId, scrollTo))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback.remove()
    }
}