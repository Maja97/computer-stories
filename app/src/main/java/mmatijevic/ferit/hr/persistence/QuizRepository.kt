package mmatijevic.ferit.hr.persistence


import mmatijevic.ferit.hr.model.Task
import mmatijevic.ferit.hr.model.Quiz

interface QuizRepository {
    fun getAllQuizzes(): MutableList<Quiz>
    fun setAllQuizzes(quizList:MutableList<Quiz>)
    fun removeAllQuizzes()
    fun addAllTasks(taskList: MutableList<Task>)
    fun removeAllTasks()
    fun deleteTaskByQuiz(quizId: Int)
    fun getTasksByQuiz(quizId: Int): MutableList<Task>
}