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

package taiwan.no.one.feat.library.presentation.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devrapid.kotlinknifer.gone
import com.devrapid.kotlinknifer.loge
import com.devrapid.kotlinknifer.visible
import org.kodein.di.factory
import taiwan.no.one.core.presentation.activity.BaseActivity
import taiwan.no.one.core.presentation.fragment.BaseFragment
import taiwan.no.one.dropbeat.AppResId
import taiwan.no.one.dropbeat.di.UtilModules.LayoutManagerParams
import taiwan.no.one.feat.library.R
import taiwan.no.one.feat.library.data.entities.local.LibraryEntity.PlayListEntity
import taiwan.no.one.feat.library.databinding.FragmentPlaylistBinding
import taiwan.no.one.feat.library.databinding.StubNoSongsBinding
import taiwan.no.one.feat.library.presentation.recyclerviews.adapters.PlaylistAdapter
import taiwan.no.one.feat.library.presentation.viewmodels.PlaylistViewModel
import taiwan.no.one.ktx.view.find
import java.lang.ref.WeakReference

internal class PlaylistFragment : BaseFragment<BaseActivity<*>, FragmentPlaylistBinding>() {
    private val vm by viewModels<PlaylistViewModel>()
    private val playlistAdapter by lazy { PlaylistAdapter() }
    private val layoutManager: (LayoutManagerParams) -> LinearLayoutManager by factory()
    private val navArgs by navArgs<PlaylistFragmentArgs>()
    private val noSongsBinding by lazy { StubNoSongsBinding.bind(binding.root) }

    override fun bindLiveData() {
        vm.playlist.observe(this) { res ->
            res.onSuccess {
                binding.mtvTitle.text = it.name
                if (it.songs.isEmpty()) {
                    displayNoSongs()
                }
                else {
                    displaySongs(it)
                }
            }.onFailure {
                loge(it)
            }
        }
    }

    override fun rendered(savedInstanceState: Bundle?) {
        vm.getSongs(navArgs.playlistId)
    }

    private fun displaySongs(playlist: PlayListEntity) {
        find<View>(R.id.include_favorite).visible()
        // Set the recycler view.
        val songs = playlist.songs
        find<RecyclerView>(AppResId.rv_musics).apply {
            if (adapter == null) {
                adapter = playlistAdapter
            }
            if (layoutManager == null) {
                layoutManager = layoutManager(LayoutManagerParams(WeakReference(requireActivity())))
            }
            (adapter as? PlaylistAdapter)?.data = songs
        }
        // Set the section title.
        find<TextView>(AppResId.mtv_explore_title).text = "All Songs"
        // Hide the view more button.
        find<View>(AppResId.btn_more).gone()
        val duration = songs.fold(0) { acc, song -> acc + song.duration }
        // Set the visibility for this fragment.
        binding.mtvSubtitle.text = "${songs.size} Songs・$duration min・30 mins ago played"
        binding.btnPlayAll.visible()
    }

    private fun displayNoSongs() {
        binding.vsNoSongs.takeIf { !it.isVisible }?.inflate()
        noSongsBinding.btnSearch.setOnClickListener {
            // Go to the search page.
        }
        binding.mtvSubtitle.text = "0 Songs"
    }
}
