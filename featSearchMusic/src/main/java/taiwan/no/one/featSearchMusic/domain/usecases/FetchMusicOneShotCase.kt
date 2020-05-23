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

package taiwan.no.one.featSearchMusic.domain.usecases

import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import taiwan.no.one.core.domain.usecase.Usecase
import taiwan.no.one.featSearchMusic.domain.repositories.SearchMusicRepo

internal class FetchMusicOneShotCase(
    private val searchMusicRepo: SearchMusicRepo
) : FetchMusicCase() {
    override suspend fun acquireCase(parameter: Request?) = searchMusicRepo.fetchMusic().map {
        // Fix the track with 0 duration.
        if (it.length == 0) {
            val retriever = MediaMetadataRetriever().apply {
                setDataSource(it.url, hashMapOf())
            }
            val time = retriever.extractMetadata(METADATA_KEY_DURATION).toLong() / 1000
            it.copy(length = time.toInt())
        }
        else {
            it
        }
    }

    data class Request(val id: Int) : Usecase.RequestValues
}