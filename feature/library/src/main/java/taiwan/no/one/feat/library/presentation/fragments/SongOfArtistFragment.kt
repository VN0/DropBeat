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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.loadAny
import com.devrapid.kotlinknifer.loge
import com.devrapid.kotlinknifer.logw
import com.google.android.material.transition.MaterialSharedAxis
import taiwan.no.one.core.presentation.activity.BaseActivity
import taiwan.no.one.core.presentation.fragment.BaseFragment
import taiwan.no.one.dropbeat.data.entities.SimpleArtistEntity
import taiwan.no.one.feat.library.databinding.FragmentSongsOfArticleBinding
import taiwan.no.one.feat.library.databinding.MergeArticleInformationBinding
import taiwan.no.one.feat.library.presentation.viewmodels.AnalyticsViewModel
import taiwan.no.one.feat.library.presentation.viewmodels.SongOfArtistViewModel

class SongOfArtistFragment : BaseFragment<BaseActivity<*>, FragmentSongsOfArticleBinding>() {
    //region Variable of View Binding
    private val mergeArticleInformationBinding get() = MergeArticleInformationBinding.bind(binding.root)
    //endregion

    //region Variable of View Model
    private val vm by viewModels<SongOfArtistViewModel>()
    private val analyticsVm by viewModels<AnalyticsViewModel>()
    //endregion

    private val navArgs by navArgs<SongOfArtistFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun bindLiveData() {
        vm.artistInfo.observe(this) { res ->
            res.onSuccess {
                logw(it)
                displayArtistInfo(it)
            }.onFailure(::loge)
        }
    }

    override fun viewComponentBinding() {
        addStatusBarHeightMarginTop(binding.btnBack)
        binding.mtvTitle.text = navArgs.track.artist
    }

    override fun componentListenersBinding() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
            analyticsVm.navigatedGoBackFromArtist()
        }
    }

    override fun rendered(savedInstanceState: Bundle?) {
        vm.getArtistInfo(navArgs.track.artist)
    }

    private fun displayArtistInfo(entity: SimpleArtistEntity) {
        binding.sivBackdrop.loadAny(entity.topAlbums.firstOrNull()?.thumbnail)
        mergeArticleInformationBinding.apply {
            mtvPlaylist.text = entity.topAlbums.size.toString()
            mtvFollower.text = entity.listener.toString()
        }
    }
}
