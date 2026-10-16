package com.example.data.db

import android.content.Context
import android.os.Build
import android.os.UserManager
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.SnippetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CategoryEntity::class, SnippetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun snippetDao(): SnippetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
                val isUnlocked = Build.VERSION.SDK_INT < Build.VERSION_CODES.N || (userManager?.isUserUnlocked == true)
                val safeContext = if (!isUnlocked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.createDeviceProtectedStorageContext()
                } else {
                    context.applicationContext
                }

                val instance = Room.databaseBuilder(
                    safeContext,
                    AppDatabase::class.java,
                    "autopaste_keyboard_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate data on first creation
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val database = getDatabase(context)
                                    database.categoryDao().insertCategories(PrepopulatedData.defaultCategories)
                                    database.snippetDao().insertSnippets(PrepopulatedData.defaultSnippets)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
