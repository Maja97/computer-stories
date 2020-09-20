package mmatijevic.ferit.hr.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import mmatijevic.ferit.hr.model.Task
import mmatijevic.ferit.hr.model.Quiz
import mmatijevic.ferit.hr.utils.StringMapConverter

@Database
    (entities = [Quiz::class, Task::class], version = 1, exportSchema = false)
@TypeConverters(StringMapConverter::class)
abstract class QuizDatabase : RoomDatabase() {
    companion object {
        private var instance : QuizDatabase? = null
        fun getInstance (context: Context): QuizDatabase {
            if ( instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "QuizDb"
                )
                    .allowMainThreadQueries()
                    .build()
            }
            return instance as QuizDatabase
        }
    }

    abstract fun quizDao(): QuizDao
    abstract fun gameDao(): TaskDao
}