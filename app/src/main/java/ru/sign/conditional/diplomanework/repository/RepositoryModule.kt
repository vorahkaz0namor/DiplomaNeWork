package ru.sign.conditional.diplomanework.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {
    @Singleton
    @Binds
    fun bindsAuthRepository(
        impl: AuthRepositoryImpl
    ) : AuthRepository

    @Singleton
    @Binds
    fun bindsUserRepository(
        impl: AuthRepositoryImpl
    ) : UserRepository

    @Singleton
    @Binds
    fun bindsPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository
}