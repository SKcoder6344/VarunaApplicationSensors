package com.varuna.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        WaterQualityEntity::class,
        DiseaseRiskEntity::class,
        AlertEntity::class,
        HelpRequestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VarunaDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun waterQualityDao(): WaterQualityDao
    abstract fun diseaseRiskDao(): DiseaseRiskDao
    abstract fun alertDao(): AlertDao
    abstract fun helpRequestDao(): HelpRequestDao

    companion object {
        @Volatile
        private var INSTANCE: VarunaDatabase? = null

        fun getDatabase(context: Context): VarunaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VarunaDatabase::class.java,
                    "varuna_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate database with demo admin user
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        // Create default admin account
                        // Password: admin123 (hashed using simple hash for demo)
                        val adminUser = UserEntity(
                            name = "Admin User",
                            email = "admin@varuna.com",
                            password = hashPassword("admin123"),
                            village = "Admin Village",
                            role = "admin"
                        )
                        database.userDao().insert(adminUser)
                    }
                }
            }
        }

        // Simple password hashing (in production, use bcrypt or similar)
        fun hashPassword(password: String): String {
            return password.hashCode().toString()
        }

        fun verifyPassword(inputPassword: String, hashedPassword: String): Boolean {
            return hashPassword(inputPassword) == hashedPassword
        }
    }
}
