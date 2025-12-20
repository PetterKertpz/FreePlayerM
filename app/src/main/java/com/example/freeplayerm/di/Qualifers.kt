package com.example.freeplayerm.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ImageClient