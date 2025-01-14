/*
 * MIT License
 *
 * Copyright (c) 2021 Jieyi
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
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devrapid.kotlinknifer.gone
import com.devrapid.kotlinknifer.loge
import com.devrapid.kotlinknifer.logw
import com.devrapid.kotlinknifer.visible
import com.devrapid.kotlinshaver.isNotNull
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.ref.WeakReference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.kodein.di.provider
import taiwan.no.one.core.presentation.activity.BaseActivity
import taiwan.no.one.dropbeat.AppResId
import taiwan.no.one.dropbeat.di.UtilModules.LayoutManagerParams
import taiwan.no.one.dropbeat.presentation.viewmodels.PrivacyViewModel
import taiwan.no.one.entity.SimpleTrackEntity
import taiwan.no.one.entity.UserInfoEntity
import taiwan.no.one.feat.library.R
import taiwan.no.one.feat.library.databinding.FragmentMyPageBinding
import taiwan.no.one.feat.library.databinding.MergeTopControllerBinding
import taiwan.no.one.feat.library.presentation.recyclerviews.adapters.PlaylistAdapter
import taiwan.no.one.feat.library.presentation.recyclerviews.adapters.TrackAdapter
import taiwan.no.one.feat.library.presentation.viewmodels.MyHomeViewModel
import taiwan.no.one.ktx.view.find
import taiwan.no.one.widget.popupmenu.popupMenuWithIcon

internal class MyHomeFragment : BaseLibraryFragment<BaseActivity<*>, FragmentMyPageBinding>() {
    //region Variable of View Binding
    private val mergeTopControllerBinding get() = MergeTopControllerBinding.bind(binding.root)
    private val includePlaylist get() = find<ConstraintLayout>(R.id.include_playlist)
    private val includeFavorite get() = find<ConstraintLayout>(R.id.include_favorite)
    private val includeDownloaded get() = find<ConstraintLayout>(R.id.include_download)
    private val includeHistory get() = find<ConstraintLayout>(R.id.include_history)
    //endregion

    //region Variable of View Model
    private val privacyVm by activityViewModels<PrivacyViewModel>()
    private val vm by viewModels<MyHomeViewModel>()
    //endregion

    //region Variable of Recycler View
    private val linearLayoutManager: () -> LinearLayoutManager by provider {
        LayoutManagerParams(WeakReference(requireActivity()))
    }

    // private val playlistLayoutManager get() = FirstBigSizeLayoutManager()
    private val playlistLayoutManager: () -> LinearLayoutManager by provider {
        LayoutManagerParams(WeakReference(requireActivity()), RecyclerView.HORIZONTAL)
    }
    private val playlistAdapter by lazy(::PlaylistAdapter)
    //endregion

    private val userEntity get() = privacyVm.userInfo.value?.getOrNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = null
        returnTransition = null
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onDestroyView() {
        foreachSubviewPlaylist { find<RecyclerView>(AppResId.rv_musics).adapter = null }
        includePlaylist.find<RecyclerView>(AppResId.rv_musics).adapter = null
        super.onDestroyView()
    }

    override fun bindLiveData() {
        vm.playlists.observe(this) { res ->
            res.onSuccess {
                vm.eliminateDefaultPlaylist(it)
                vm.extractDefaultPlaylist(it)
            }.onFailure(::loge)
        }
        vm.downloaded.observe(this) { displayPlaylist(it, includeDownloaded) }
        vm.favorites.observe(this) { displayPlaylist(it, includeFavorite) }
        vm.histories.observe(this) { displayPlaylist(it, includeHistory) }
        vm.resultOfFavorite.observe(this) {
            if (!it) return@observe
            vm.getAllPlaylists()
        }
        vm.nonDefaultPlaylist.observe(this) {
            if (it.isEmpty()) {
                // TODO(jieyi): 6/23/21 Display no playlist.
            }
            else {
                playlistAdapter.submitList(it)
            }
        }
    }

    override fun viewComponentBinding() {
        addStatusBarHeightMarginTop(mergeTopControllerBinding.mtvTitle)
        includePlaylist.apply {
            find<TextView>(AppResId.mtv_explore_title).text = "Playlist"
            find<View>(AppResId.btn_play_all).gone()
            find<RecyclerView>(AppResId.rv_musics).apply {
                adapter = playlistAdapter
                layoutManager = playlistLayoutManager()
            }
        }
        listOf(
            includeFavorite to "Favorite",
            includeDownloaded to "Downloaded",
            includeHistory to "History",
        ).forEach { (layout, txt) ->
            layout.find<TextView>(AppResId.mtv_explore_title).text = txt
        }
        foreachSubviewPlaylist {
            find<RecyclerView>(AppResId.rv_musics).apply {
                adapter = TrackAdapter()
                layoutManager = linearLayoutManager()
            }
        }
    }

    override fun componentListenersBinding() {
        playlistAdapter.setOnClickListener {
            findNavController().navigate(MyHomeFragmentDirections.actionMyHomeToPlaylist(it.id))
            analyticsVm.navigatedToPlaylist()
        }
        mergeTopControllerBinding.apply {
            btnLogin.setOnClickListener {
                findNavController().navigate(MyHomeFragmentDirections.actionMyHomeToLoginGraph())
                analyticsVm.navigatedToLogin()
            }
            btnSetting.setOnClickListener {
                findNavController().navigate(MyHomeFragmentDirections.actionMyHomeToSettingGraph())
                analyticsVm.navigatedToSetting()
            }
        }
        listOf(
            includeFavorite.find<Button>(AppResId.btn_more) to 2,
            includeDownloaded.find<Button>(AppResId.btn_more) to 1,
            includeHistory.find<Button>(AppResId.btn_more) to 3,
        ).forEach { (button, id) ->
            button.setOnClickListener {
                findNavController().navigate(MyHomeFragmentDirections.actionMyHomeToPlaylist(id))
                analyticsVm.navigatedToPlaylist()
            }
        }
        listOf(
            includeFavorite.find<RecyclerView>(AppResId.rv_musics).adapter as? TrackAdapter,
            includeDownloaded.find<RecyclerView>(AppResId.rv_musics).adapter as? TrackAdapter,
            includeHistory.find<RecyclerView>(AppResId.rv_musics).adapter as? TrackAdapter,
        ).forEach { it?.let(::setListClickListener) }
        mergeTopControllerBinding.btnMore.setOnClickListener {
            showMenu(it)
            analyticsVm.clickedMore()
        }
    }

    override fun rendered(savedInstanceState: Bundle?) {
        userEntity.takeIf { it?.uid.isNotNull() }?.let {
            mergeTopControllerBinding.mtvTitle.text = it.displayName ?: it.email
            mergeTopControllerBinding.btnLogin.gone()
            // doSync(it)
            if (privacyVm.shouldDisplaySyncDialog) {
                privacyVm.shouldDisplaySyncDialog = false
                // Show sync dialog
                showSyncDialog()
                vm.createAccountOnRemote(it)
                vm.createPlaylistOnRemote(requireNotNull(userEntity))
            }
        }
        vm.getAllPlaylists()
        // vm.test()
    }

    private fun setListClickListener(trackAdapter: TrackAdapter) {
        trackAdapter.apply {
            setOnClickListener {
                analyticsVm.clickedPlayAMusic(it.obtainTrackAndArtistName())
            }
            setOptionClickListener { v, entity ->
                showOptionMenu(v, entity) { navigateToArtist(entity) }
                analyticsVm.clickedOption(entity.obtainTrackAndArtistName())
            }
            setFavoriteClickListener {
                vm.updateSong(it, it.isFavorite)
                analyticsVm.clickedFavorite(it.isFavorite, it.obtainTrackAndArtistName())
            }
        }
    }

    private fun foreachSubviewPlaylist(block: ConstraintLayout.() -> Unit) {
        listOf(includeDownloaded, includeFavorite, includeHistory).forEach { it.block() }
    }

    private fun displayPlaylist(songs: List<SimpleTrackEntity>, layout: ConstraintLayout) {
        if (songs.isEmpty()) {
            layout.find<TextView>(AppResId.mtv_no_music).visible()
        }
        else {
            (layout.find<RecyclerView>(AppResId.rv_musics).adapter as? TrackAdapter)?.apply {
                data = songs
                // It doesn't have many data, just reset all.
                notifyDataSetChanged()
            }
        }
        layout.find<View>(AppResId.pb_progress).gone()
    }

    private fun navigateToArtist(entity: SimpleTrackEntity) {
        findNavController().navigate(MyHomeFragmentDirections.actionMyHomeToNavArtist(entity))
        analyticsVm.navigatedFromPlaylistToArtist(entity.artist)
    }

    private fun showSyncDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Information")
            .setMessage("Would you like to sync your data")
            .setNegativeButton(resources.getString(android.R.string.cancel)) { dialog, which ->
                // Respond to negative button press
            }
            .setPositiveButton(resources.getString(android.R.string.ok)) { dialog, which ->
                // Respond to positive button press
            }
            .show()
    }

    private fun showMenu(anchor: View) =
        popupMenuWithIcon(requireActivity(), anchor, R.menu.menu_more_setting).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item_playlist -> Unit
                    R.id.item_favorite -> {
                        findNavController().navigate(MyHomeFragmentDirections.actionMyHomeToPlaylist(2))
                        analyticsVm.navigatedToPlaylist()
                    }
                    R.id.item_downloaded -> {
                        findNavController().navigate(MyHomeFragmentDirections.actionMyHomeToPlaylist(1))
                        analyticsVm.navigatedToPlaylist()
                    }
                    R.id.item_local_music -> Unit
                    R.id.item_setting -> {
                        findNavController().navigate(MyHomeFragmentDirections.actionMyHomeToSettingGraph())
                        analyticsVm.navigatedToSetting()
                    }
                    R.id.item_about -> Unit
                }
                true
            }
        }.show()

    private fun doSync(entity: UserInfoEntity) {
        launch {
            Firebase.firestore.collection("provider")
                .document("firebase")
                .collection("email")
                .document("test@test.test")
                .get()
                .addOnSuccessListener {
                    logw(it)
                    GlobalScope.launch {
                        val a = (it["playlists"] as List<DocumentReference>).map {
//                            suspendCancellableCoroutine<Map<String, Any>> { continuation ->
//                                it.get().addOnSuccessListener {
//                                    continuation.resume(it.data.orEmpty())
//                                }.addOnFailureListener(continuation::resumeWithException)
//                            }
                            it.get().await().data
                        }
                        logw(a)
                    }
                }
        }
    }
}
