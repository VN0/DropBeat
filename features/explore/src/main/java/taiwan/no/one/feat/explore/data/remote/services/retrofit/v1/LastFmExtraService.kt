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

package taiwan.no.one.feat.explore.data.remote.services.retrofit.v1

import taiwan.no.one.entity.SimpleTrackEntity
import taiwan.no.one.feat.explore.data.entities.remote.ArtistMoreDetailEntity
import taiwan.no.one.feat.explore.data.entities.remote.ArtistPhotosEntity
import taiwan.no.one.feat.explore.data.entities.remote.TrackInfoEntity.TrackEntity

/**
 * We will implement those them by [org.jsoup.Jsoup] because we can't use api from
 * lastfm.
 * Using prefix name (retrieve), (insert), (replace), (release)
 */
internal interface LastFmExtraService {
    suspend fun retrieveArtistPhotosInfo(artistName: String, page: Int): ArtistPhotosEntity

    suspend fun retrieveArtistMoreDetail(artistName: String): ArtistMoreDetailEntity

    suspend fun retrieveTrackCover(url: String, trackEntity: TrackEntity): TrackEntity

    suspend fun retrieveTrackCover(url: String, simpleTrackEntity: SimpleTrackEntity): SimpleTrackEntity
}
