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

package taiwan.no.one.feat.explore.data.contracts

import taiwan.no.one.feat.explore.data.entities.remote.AlbumInfoEntity
import taiwan.no.one.feat.explore.data.entities.remote.ArtistInfoEntity
import taiwan.no.one.feat.explore.data.entities.remote.ArtistPhotosEntity
import taiwan.no.one.feat.explore.data.entities.remote.ArtistSimilarEntity
import taiwan.no.one.feat.explore.data.entities.remote.ArtistTopAlbumInfoEntity
import taiwan.no.one.feat.explore.data.entities.remote.ArtistTopTrackInfoEntity
import taiwan.no.one.feat.explore.data.entities.remote.TagInfoEntity
import taiwan.no.one.feat.explore.data.entities.remote.TagTopArtistEntity
import taiwan.no.one.feat.explore.data.entities.remote.TopAlbumInfoEntity
import taiwan.no.one.feat.explore.data.entities.remote.TopArtistInfoEntity
import taiwan.no.one.feat.explore.data.entities.remote.TopTagInfoEntity
import taiwan.no.one.feat.explore.data.entities.remote.TopTrackInfoEntity
import taiwan.no.one.feat.explore.data.entities.remote.TrackInfoEntity
import taiwan.no.one.feat.explore.data.entities.remote.TrackSimilarEntity

/**
 * This interface will common the all data stores.
 * Using prefix name (get), (create), (modify), (remove), (store)
 */
internal interface DataStore {
    //region Album Data
    suspend fun getAlbumInfo(): AlbumInfoEntity
    //endregion

    //region Artist Data
    suspend fun getArtistInfo(): ArtistInfoEntity

    suspend fun getArtistTopAlbum(): ArtistTopAlbumInfoEntity

    suspend fun getArtistTopTrack(): ArtistTopTrackInfoEntity

    suspend fun getSimilarArtistInfo(): ArtistSimilarEntity

    suspend fun getArtistPhotosInfo(): ArtistPhotosEntity
    //endregion

    //region Track Data
    suspend fun getTrackInfo(): TrackInfoEntity

    suspend fun getSimilarTrackInfo(): TrackSimilarEntity
    //endregion

    //region Chart
    suspend fun getChartTopTrack(): TopTrackInfoEntity

    suspend fun getChartTopArtist(): TopArtistInfoEntity

    suspend fun getChartTopTag(): TopTagInfoEntity
    //endregion

    //region Tag Data
    suspend fun getTagInfo(): TagInfoEntity

    suspend fun getTagTopAlbum(): TopAlbumInfoEntity

    suspend fun getTagTopArtist(): TagTopArtistEntity

    suspend fun getTagTopTrack(): TopTrackInfoEntity
    //endregion
}