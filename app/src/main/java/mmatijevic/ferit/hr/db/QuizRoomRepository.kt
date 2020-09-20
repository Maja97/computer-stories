package mmatijevic.ferit.hr.db

import mmatijevic.ferit.hr.app.QuizApplication
import mmatijevic.ferit.hr.model.Task
import mmatijevic.ferit.hr.model.Quiz
import mmatijevic.ferit.hr.persistence.QuizRepository

class QuizRoomRepository : QuizRepository {
    private var db: QuizDatabase = QuizDatabase.getInstance(QuizApplication.getAppContext())
    private var quizDao: QuizDao = db.quizDao()
    private var taskDao : TaskDao = db.gameDao()

    override fun addAllTasks(taskList: MutableList<Task>) {
        taskDao.insertAllTasks(taskList)
    }

    override fun getTasksByQuiz(quizId: Int): MutableList<Task> = taskDao.getTasksByQuizId(quizId)


    override fun deleteTaskByQuiz(quizId: Int) {
        taskDao.deleteTaskByQuiz(quizId)
    }

    override fun removeAllTasks() {
        taskDao.deleteAllTasks()
    }

    override fun removeAllQuizzes() {
        quizDao.deleteAllQuizzes()
    }

    override fun getAllQuizzes(): MutableList<Quiz> = quizDao.getAll()


    override fun setAllQuizzes(quizList: MutableList<Quiz>) {
        quizDao.insertAll(quizList)
    }

}