package mmatijevic.ferit.hr.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import mmatijevic.ferit.hr.model.Quiz

@Dao
interface QuizDao {
    @Query("SELECT * FROM Quiz")
    fun getAll(): MutableList<Quiz>

    @Query( "SELECT * FROM Quiz WHERE id = :quizId")
    fun getQuiz (quizId: Int): Quiz

    @Insert
    fun insertAll(quizList:MutableList<Quiz>)

    @Delete
    fun deleteQuiz (quiz: Quiz)

    @Query( "DELETE FROM Quiz")
    fun deleteAllQuizzes ()
}