package com.example.playlistmaker.feature.media.domain.mapper

import com.example.playlistmaker.feature.media.data.db.entity.PlaylistEntity
import com.example.playlistmaker.feature.media.domain.model.Playlist

object PlaylistMapper {
    fun mapToEntity(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverPath = playlist.coverPath,
            tracksIdsJson = PlaylistEntity.listToTracksIds(playlist.tracksIds),
            tracksCount = playlist.tracksCount
        )
    }

    fun mapFromEntityToDomain(entity: PlaylistEntity): Playlist {
        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            coverPath = entity.coverPath,
            tracksIds = PlaylistEntity.tracksIdsToList(entity.tracksIdsJson),
            tracksCount = entity.tracksCount
        )
    }

    fun mapFromEntityToDomainList(entities: List<PlaylistEntity>): List<Playlist> {
        return entities.map { mapFromEntityToDomain(it) }
    }
}