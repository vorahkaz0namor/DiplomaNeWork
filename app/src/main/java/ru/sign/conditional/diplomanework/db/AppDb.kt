package ru.sign.conditional.diplomanework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.sign.conditional.diplomanework.dao.AuxDao
import ru.sign.conditional.diplomanework.dao.PostDao
import ru.sign.conditional.diplomanework.dao.PostRemoteKeyDao
import ru.sign.conditional.diplomanework.entity.*

@Database(
    entities = [
        PostEntity::class,
        UserPreviewEntity::class,
        PostRemoteKeyEntity::class,
        DraftCopyEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun auxDao(): AuxDao
}