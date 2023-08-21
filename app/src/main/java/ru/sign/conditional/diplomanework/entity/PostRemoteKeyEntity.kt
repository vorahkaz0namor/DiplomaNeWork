package ru.sign.conditional.diplomanework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostRemoteKeyEntity(
    @PrimaryKey
    val type: RemoteKeyType,
    val key: Int
)