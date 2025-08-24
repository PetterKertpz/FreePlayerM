package com.example.freeplayerm.di // Asegúrate de que tu paquete coincida

import android.app.Application
import androidx.room.Room
import com.example.freeplayerm.data.local.AppDatabase
import com.example.freeplayerm.data.repository.UsuarioRepository
import com.example.freeplayerm.data.repository.UsuarioRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @Module: Esta anotación le dice a Hilt que este objeto contiene "recetas" para construir dependencias.
 *
 * @InstallIn(SingletonComponent::class): Esto es muy importante. Le dice a Hilt que las dependencias
 * definidas en este módulo deben vivir mientras la aplicación esté viva. Es decir, se crearán
 * una sola vez y se reutilizarán en toda la app. Esto es perfecto para nuestra base de datos
 * y nuestro repositorio.
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuloDeAplicacion {

    /**
     * @Provides: Marca esta función como una "receta". Hilt la usará cuando necesite
     * construir un objeto del tipo que la función devuelve (en este caso, AppDatabase).
     *
     * @Singleton: Le dice a Hilt que la instancia creada por esta receta debe ser un Singleton,
     * es decir, solo debe crearse UNA VEZ en toda la vida de la aplicación. Es crucial para
     * la base de datos, para que no abramos múltiples conexiones innecesariamente.
     */
    @Provides
    @Singleton
    fun proveerBaseDeDatos(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app, // El contexto de la aplicación, que Hilt nos proporciona automáticamente.
            AppDatabase::class.java, // La clase de nuestra base de datos.
            "freeplayer.db" // El nombre del archivo de la base de datos que se creará en el dispositivo.
        ).build()
    }

    /**
     * Esta es la receta para nuestro UsuarioRepositorio.
     * Hilt es inteligente: ve que esta función necesita un 'AppDatabase' como parámetro.
     * Automáticamente buscará otra receta que sepa cómo proveer un 'AppDatabase' (la que está justo arriba),
     * la ejecutará, y nos pasará el resultado.
     */
    @Provides
    @Singleton
    fun proveerRepositorioDeUsuario(db: AppDatabase): UsuarioRepository {
        // Creamos la implementación concreta del repositorio, pasándole el DAO
        // que obtenemos de nuestra instancia de la base de datos.
        // Hilt sabrá que cuando alguien pida un 'UsuarioRepositorio', debe devolver esta implementación.
        return UsuarioRepositoryImpl(db.usuarioDao())
    }
}