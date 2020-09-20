package mmatijevic.ferit.hr.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import mmatijevic.ferit.hr.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM Task WHERE quizId= :quizId")
    fun getTasksByQuizId(quizId: Int): MutableList<Task>

    @Insert
    fun insertAllTasks(taskList:MutableList<Task>)

    @Query("DELETE FROM Task")
    fun deleteAllTasks()

    @Delete
    fun deleteTask(task: Task)

    @Query("DELETE FROM Task WHERE quizId= :quizId")
    fun deleteTaskByQuiz(quizId: Int)

    @Insert
    fun insertTask(task: Task)
}