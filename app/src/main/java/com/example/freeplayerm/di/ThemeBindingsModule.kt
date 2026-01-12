package com.example.freeplayerm.di

import com.example.freeplayerm.ui.theme.ThemeManager
import com.example.freeplayerm.ui.theme.ThemeManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeBindingsModule {
   
   @Binds
   @Singleton
   abstract fun bindThemeManager(impl: ThemeManagerImpl): ThemeManager
}