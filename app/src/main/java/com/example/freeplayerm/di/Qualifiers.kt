package com.example.freeplayerm.di

import javax.inject.Qualifier

// ==================== NETWORK CLIENTS ====================

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class NetworkClient

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class ImageClient

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class ApiClient

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class ScraperClient

// ==================== INTERCEPTORS ====================

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AuthInterceptor

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class RetryInterceptor
