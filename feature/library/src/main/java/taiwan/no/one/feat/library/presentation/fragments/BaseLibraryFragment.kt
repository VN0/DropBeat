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
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.google.android.material.transition.MaterialSharedAxis
import taiwan.no.one.core.presentation.activity.BaseActivity
import taiwan.no.one.core.presentation.fragment.BaseFragment
import taiwan.no.one.dropbeat.AppResId
import taiwan.no.one.dropbeat.AppResMenu
import taiwan.no.one.dropbeat.data.entities.SimpleTrackEntity
import taiwan.no.one.feat.library.presentation.viewmodels.AnalyticsViewModel
import taiwan.no.one.ktx.intent.shareText
import taiwan.no.one.widget.popupmenu.popupMenuWithIcon

internal open class BaseLibraryFragment<out A : BaseActivity<*>, V : ViewBinding> : BaseFragment<A, V>() {
    protected val analyticsVm by viewModels<AnalyticsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    protected fun showOptionMenu(anchor: View, entity: SimpleTrackEntity, infoActionBlock: () -> Unit = {}) =
        popupMenuWithIcon(requireActivity(), anchor, AppResMenu.menu_more_track).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    AppResId.item_information -> infoActionBlock()
                    AppResId.item_share -> {
                        shareText(requireActivity(), entity.uri)
                        analyticsVm.clickedShare(entity.uri)
                    }
                }
                true
            }
        }.show()
}
