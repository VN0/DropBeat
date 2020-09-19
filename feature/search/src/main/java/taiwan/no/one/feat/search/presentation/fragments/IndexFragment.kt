/*
 * MIT License
 *
 * Copyright (c) 2020 Jieyi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package taiwan.no.one.feat.search.presentation.fragments

import android.view.KeyEvent
import android.widget.EditText
import androidx.core.net.toUri
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devrapid.kotlinknifer.hideSoftKeyboard
import com.devrapid.kotlinknifer.invisible
import com.devrapid.kotlinknifer.loge
import com.devrapid.kotlinknifer.logw
import com.devrapid.kotlinknifer.recyclerview.itemdecorator.VerticalItemDecorator
import com.devrapid.kotlinknifer.visible
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.kodein.di.instance
import org.kodein.di.provider
import taiwan.no.one.core.presentation.activity.BaseActivity
import taiwan.no.one.core.presentation.fragment.BaseFragment
import taiwan.no.one.dropbeat.core.helpers.DownloadHelper
import taiwan.no.one.dropbeat.di.UtilModules.LayoutManagerParams
import taiwan.no.one.feat.search.R
import taiwan.no.one.feat.search.data.entities.remote.CommonMusicEntity.SongEntity
import taiwan.no.one.feat.search.databinding.FragmentSearchIndexBinding
import taiwan.no.one.feat.search.databinding.MergeSearchHasNoResultBinding
import taiwan.no.one.feat.search.databinding.MergeSearchHasResultBinding
import taiwan.no.one.feat.search.presentation.recyclerviews.adapters.HistoryAdapter
import taiwan.no.one.feat.search.presentation.recyclerviews.adapters.ResultAdapter
import taiwan.no.one.feat.search.presentation.viewmodels.RecentViewModel
import taiwan.no.one.feat.search.presentation.viewmodels.ResultViewModel
import taiwan.no.one.feat.search.presentation.viewmodels.SongViewModel
import taiwan.no.one.ktx.recyclerview.contains
import taiwan.no.one.ktx.view.afterTextChanges
import taiwan.no.one.widget.WidgetResDimen
import taiwan.no.one.widget.recyclerviews.listeners.LinearLoadMoreScrollListener
import java.lang.ref.WeakReference

internal class IndexFragment : BaseFragment<BaseActivity<*>, FragmentSearchIndexBinding>() {
    private val mergeBinding get() = MergeSearchHasResultBinding.bind(binding.root)
    private val mergeNoResultBinding get() = MergeSearchHasNoResultBinding.bind(binding.root)
    private val vm by viewModels<RecentViewModel>()
    private val searchVm by viewModels<ResultViewModel>()
    private val songVm by viewModels<SongViewModel>()
    private val searchHistoryAdapter by lazy { HistoryAdapter() }
    private val musicAdapter by lazy { ResultAdapter() }
    private val musicItemDecoration by lazy {
        VerticalItemDecorator(resources.getDimension(WidgetResDimen.md_three_unit).toInt(), 0)
    }
    private val rvMusics get() = mergeBinding.rvMusics
    private val loadMoreListener by instance<LinearLoadMoreScrollListener>()
    private val linearLayoutManager: () -> LinearLayoutManager by provider {
        LayoutManagerParams(WeakReference(requireActivity()))
    }
    private val jobs = mutableListOf<Job>()

    override fun onDetach() {
        super.onDetach()
        loadMoreListener.fetchMoreBlock = null
    }

    override fun bindLiveData() {
        vm.histories.observe(this) {
            logw(it)
            if (it.isEmpty()) {
                // TODO(Jieyi): 8/5/20 The action needs to be confirmed again.
            }
            else {
                searchHistoryAdapter.data = it
                rvMusics.smoothScrollToPosition(0)
            }
        }
        searchVm.musics.observe(this) { res ->
            res.onSuccess {
                if (it.isEmpty()) {
                    binding.gNoResult.visible()
                    binding.gResult.invisible()
                    mergeNoResultBinding.mtvNoResult.text = "no \"${searchVm.curKeyword}\" result"
                }
                else {
                    binding.gNoResult.invisible()
                    binding.gResult.visible()
                    musicAdapter.addExtraEntities(it)
                }
                hideLoading()
            }.onFailure {
                loge(it)
            }
        }
    }

    override fun viewComponentBinding() {
        super.viewComponentBinding()
        mergeBinding.mtvRvTitle.doOnPreDraw {
            val halfWidth = it.width / 2
            // anchor 3 is top margin, it didn't define inside setMargin
            binding.layoutParent.getConstraintSet(R.id.expanded)?.setMargin(R.id.mtv_rv_title, 3, halfWidth)
        }
        rvMusics.apply {
            if (adapter == null) {
                adapter = searchHistoryAdapter
            }
            if (layoutManager == null) {
                layoutManager = linearLayoutManager()
            }
            addOnScrollListener(loadMoreListener)
        }
    }

    override fun componentListenersBinding() {
        searchHistoryAdapter.setOnClickListener(::clickedOnHistoryItem)
        musicAdapter.setOnClickListener(::clickedOnSongItem)
        binding.apply {
            // Click the search icon
            tilSearchBar.setEndIconOnClickListener {
                searchMusic(tietSearch.text.toString())
            }
            // Hit the enter key on the soft keyword or the physical keyboard
            tietSearch.setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    searchMusic((v as EditText).text.toString())
                    return@setOnKeyListener true
                }
                false
            }
            tietSearch.afterTextChanges().debounce(300).onEach {
                if (it.isNullOrBlank()) {
                    setAndDisplayHistory()
                }
            }.launchIn(lifecycleScope)
        }
        mergeNoResultBinding.btnClear.setOnClickListener {
            binding.tietSearch.text?.clear()
        }
        if (loadMoreListener.fetchMoreBlock == null) {
            loadMoreListener.fetchMoreBlock = ::getMoreMusics
        }
    }

    private fun searchMusic(keyword: String) {
        if (keyword.isBlank()) return
        showLoading()
        vm.add(keyword)
        jobs += searchVm.search(keyword, 0)
        setAndDisplaySearchResult()
    }

    private fun getMoreMusics() {
        if (rvMusics.adapter != musicAdapter) return
        searchVm.apply {
            goNextPage()
            jobs += search()
        }
    }

    private fun setAndDisplayHistory() {
        // 1. Pre-handle and finish the music adapter's process.
        cancelJobs()
        if (musicAdapter.data.isNotEmpty()) {
            musicAdapter.clear()
        }
        // Remove the item decoration.
        if (musicItemDecoration in rvMusics) {
            rvMusics.removeItemDecoration(musicItemDecoration)
        }
        // 2. Set the adapter for displaying the history.
        rvMusics.apply {
            if (adapter != searchHistoryAdapter) {
                adapter = searchHistoryAdapter
            }
        }
        mergeBinding.mtvRvTitle.text = "History Search "
    }

    private fun setAndDisplaySearchResult() {
        // 1. Pre-process the setting of the result recyclerview.
        musicAdapter.clear()
        // Post-action for hiding the soft keyboard.
        view?.hideSoftKeyboard()
        // Add the item decoration.
        if (musicItemDecoration !in rvMusics) {
            rvMusics.addItemDecoration(musicItemDecoration)
        }
        // Reset the status.
        loadMoreListener.reset()
        // 2. Set the adapter for displaying the result.
        rvMusics.apply {
            if (adapter != musicAdapter) {
                adapter = musicAdapter
            }
        }
        mergeBinding.mtvRvTitle.text = "Search Result "
    }

    private fun disableMotion() {
        binding.layoutParent.getTransition(R.id.transition_search_scene).apply {
            if (isEnabled) {
                setEnable(false)
            }
        }
    }

    private fun enableMotion() {
        binding.layoutParent.getTransition(R.id.transition_search_scene).apply {
            if (!isEnabled) {
                setEnable(true)
            }
        }
    }

    private fun enableMotionWhenScrollable(recyclerView: RecyclerView) {
        // down = 1; up = -1
        if (recyclerView.canScrollVertically(-1)) enableMotion() else disableMotion()
    }

    private fun cancelJobs() {
        jobs.forEach { it.cancel("The history adapter is displaying now.") }
        jobs.clear()
    }

    private fun clickedOnSongItem(song: SongEntity) {
        val filename = "${song.artist} - ${song.title}"
        DownloadHelper.downloadTrack(requireContext(), song.url.toUri(), filename, songVm.songToStream(song))
    }

    private fun clickedOnHistoryItem(keyword: String) {
        binding.tietSearch.setText(keyword)
        searchMusic(keyword)
    }
}
