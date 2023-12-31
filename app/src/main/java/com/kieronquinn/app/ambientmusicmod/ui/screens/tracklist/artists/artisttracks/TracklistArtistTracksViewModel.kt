package com.kieronquinn.app.ambientmusicmod.ui.screens.tracklist.artists.artisttracks

import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.ambientmusicmod.R
import com.kieronquinn.app.ambientmusicmod.components.navigation.TracklistNavigation
import com.kieronquinn.app.ambientmusicmod.model.shards.ShardTrack
import com.kieronquinn.app.ambientmusicmod.repositories.ShardsListRepository
import com.kieronquinn.app.ambientmusicmod.ui.screens.tracklist.generic.GenericTracklistViewModelImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class TracklistArtistTracksViewModel(
    shardsListRepository: ShardsListRepository
): GenericTracklistViewModelImpl<ShardTrack>(shardsListRepository) {

    abstract fun setArtist(artist: String)
    abstract fun onBackPressed()
    abstract fun onTrackClicked(track: ShardTrack)

}

class TracklistArtistTracksViewModelImpl(
    shardsListRepository: ShardsListRepository,
    private val navigation: TracklistNavigation
): TracklistArtistTracksViewModel(shardsListRepository) {

    companion object {
        private const val SEARCH_TERM_ONDEMAND = "ondemand"
    }

    private val artist = MutableStateFlow<String?>(null)

    override fun setArtist(artist: String) {
        viewModelScope.launch {
            this@TracklistArtistTracksViewModelImpl.artist.emit(artist)
        }
    }

    override suspend fun createList(tracks: List<ShardTrack>): List<ShardTrack> {
        val artist = artist.filterNotNull().first()
        return tracks.filter { it.artist == artist }.sortedBy { it.trackName.lowercase() }
    }

    override fun filterList(items: List<ShardTrack>, searchTerm: String): List<ShardTrack> {
        if(searchTerm.isBlank()) return items
        val linearOnly = searchTerm.equals(SEARCH_TERM_ONDEMAND, true)
        return if(linearOnly) {
            items.filter { it.isLinear }
        }else{
            items.filter {
                it.trackName.lowercase().trim().contains(searchTerm.lowercase().trim())
                        || it.album?.lowercase()?.contains(searchTerm.lowercase().trim()) ?: false
            }
        }
    }

    override fun onBackPressed() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onTrackClicked(track: ShardTrack) {
        viewModelScope.launch {
            //Collisions mean we have to form this manually
            navigation.navigate(
                R.id.action_tracklistArtistTracksFragment_to_trackInfoBottomSheetFragment,
                bundleOf(
                    "track" to track,
                    "from_artists" to true
                )
            )
        }
    }

}