package ru.sign.conditional.diplomanework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.sign.conditional.diplomanework.dao.*
import ru.sign.conditional.diplomanework.entity.*

@Database(
    entities = [
        PostEntity::class,
        EventEntity::class,
        JobEntity::class,
        UserPreviewEntity::class,
        PostRemoteKeyEntity::class,
        EventRemoteKeyEntity::class,
        DraftCopyEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun jobDao(): JobDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun auxDao(): AuxDao
}