package mmatijevic.ferit.hr.ui.main

import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.adapter.MenuAdapter
import mmatijevic.ferit.hr.common.*
import mmatijevic.ferit.hr.db.QuizDatabase
import mmatijevic.ferit.hr.db.QuizRoomRepository
import mmatijevic.ferit.hr.model.Quiz
import mmatijevic.ferit.hr.model.Task
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.persistence.QuizRepository
import mmatijevic.ferit.hr.ui.activities.LoginActivity
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import mmatijevic.ferit.hr.ui.base.BaseActivity
import mmatijevic.ferit.hr.utils.DepthPageTransformer


class MainActivity : BaseActivity() {
    private val repository: QuizRepository = QuizRoomRepository()

    private lateinit var quizList: MutableList<Quiz>
    private lateinit var taskList: MutableList<Task>

    override fun getLayoutResourceId() =
        R.layout.activity_main

    override fun setUpUi() {
        refreshData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refreshData -> {
                refreshData()
                true
            }
            R.id.login -> {
                val intent = Intent(this, LoginActivity::class.java)
                this.startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshData() {
        if (isOnline(applicationContext)) {
            val list = mutableListOf<Quiz>()
            val tasks = mutableListOf<Task>()
            val db = FirebaseFirestore.getInstance()
            db.collection(QUIZZES_COLLECTION).orderBy(TIMESTAMP).get().addOnSuccessListener { snapshot ->
                for (i in 0 until snapshot.size()) {
                    snapshot.documents[i].reference.update("id", i + 1)
                }

                for (i in 0 until snapshot.size()) {
                    val uri = snapshot.documents[i].get("image")
                    val id = snapshot.documents[i].get("id") as Long

                    list.add(
                        i, Quiz(
                            quizNumber = id.toInt(),
                            title = snapshot.documents[i].get("title") as String,
                            image = uri as String
                        )
                    )
                    snapshot.documents[i].reference.collection(TASKS_COLLECTION).orderBy("task_id")
                        .get().addOnSuccessListener {
                            for(x in 0 until it.size()){
                                val map = mutableMapOf<String, String>()
                                val d= it.documents[x]
                                when(d.get("task_type")){
                                    "story" -> {
                                        map["title"] = d.get("title").toString()
                                        map["description"] = d.get("description").toString()
                                        map["background"] = d.get("background").toString()
                                        map["layout"] = d.get("story_layout").toString()
                                        map["type"] = d.get("task_type").toString()
                                    }
                                    "question" -> {
                                        map["question"] = d.get("question").toString()
                                        map["answer1"] = d.get("answer1").toString()
                                        map["answer2"] = d.get("answer2").toString()
                                        map["answer3"] = d.get("answer3").toString()
                                        map["answer4"] = d.get("answer4").toString()
                                        map["background"] = d.get("background").toString()
                                        map["answers_theme"] = d.get("answers_theme").toString()
                                        map["type"] = d.get("task_type").toString()
                                    }
                                    "puzzle" -> {
                                        map["instruction"] = d.get("instruction").toString()
                                        map["background"] = d.get("background").toString()
                                        map["type"] = d.get("task_type").toString()
                                    }
                                    "word_guess" -> {
                                        map["dialog_text"] = d.get("dialog_text").toString()
                                        map["instruction"] = d.get("instruction").toString()
                                        map["image1"] = d.get("image1").toString()
                                        map["image2"] = d.get("image2").toString()
                                        map["word"] = d.get("word").toString()
                                        map["type"] = d.get("task_type").toString()
                                    }
                                    "odd_one_out" -> {
                                        for(k in 1..(d.get("word_number")as Long).toInt()){
                                            map["word$k"] = d.get("word$k").toString()
                                        }
                                        map["instruction"] = d.get("instruction").toString()
                                        map["background"] = d.get("background").toString()
                                        map["type"] = d.get("task_type").toString()
                                        map["word_number"] = d.get("word_number").toString()
                                    }
                                    "connect" -> {
                                        for(l in 1..(d.get("definition_number")as Long).toInt()){
                                            map["word$l"] = d.get("word$l").toString()
                                            map["definition$l"] = d.get("definition$l").toString()
                                        }
                                        map["background"] = d.get("background").toString()
                                        map["instruction"] = d.get("instruction").toString()
                                        map["text_color"] = d.get("text_color").toString()
                                        map["definition_number"] = d.get("definition_number").toString()
                                        map["type"] = d.get("task_type").toString()
                                    }
                                    "memory" -> {
                                        map["instruction"] = d.get("instruction").toString()
                                        map["num_pairs"] = d.get("num_pairs").toString()
                                        map["type"] = d.get("task_type").toString()
                                        for(k in 0 until (d.get("num_pairs")as Long).toInt()){
                                            for(m in 0 until 2){
                                                map["image$k$m"] = d.get("image$k$m").toString()
                                            }
                                        }
                                    }
                                    "sort" -> {
                                        map["type"] = d.get("task_type").toString()
                                        map["message"] = d.get("message").toString()
                                        map["left_text"] = d.get("left_text").toString()
                                        map["right_text"] = d.get("right_text").toString()
                                        map["left_size"] = d.get("left_size").toString()
                                        map["right_size"] = d.get("right_size").toString()
                                        for(k in 1..(d.get("left_size")as Long).toInt()){
                                                map["left$k"] = d.get("left$k").toString()
                                        }
                                        for(j in 1..(d.get("right_size")as Long).toInt()){
                                            map["right$j"] = d.get("right$j").toString()
                                        }
                                    }
                                    "word_finder" -> {
                                        map["message"] = d.get("message").toString()
                                        map["background"] = d.get("background").toString()
                                        for(k in 1..5){
                                            map["word$k"] = d.get("word$k").toString()
                                            map["description$k"] = d.get("description$k").toString()
                                        }
                                        map["type"] = d.get("task_type").toString()
                                    }
                                }
                                tasks.add(
                                    Task(
                                        quizId = id.toInt(),
                                        taskNumber = (d.get("task_id") as Long).toInt(),
                                        map = map
                                    )
                                )
                            }
                            if(id.toInt() == snapshot.size()){
                                taskList = tasks
                                repository.removeAllTasks()
                                repository.addAllTasks(taskList)
                            }

                        }

                }

                quizList = list
                repository.removeAllQuizzes()
                repository.setAllQuizzes(quizList)
                initMenu()
            }
    } else
    {
        quizList = getQuizzes(repository)
        if (quizList.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "Internet connection needed to fetch data",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            initMenu()
        }
    }
}

    override fun onResume() {
        super.onResume()
        QuizPrefs.store(QuizPrefs.KEY_QUESTION_NUM, 1)
        refreshData()
    }

    private fun getQuizzes(repository: QuizRepository): MutableList<Quiz> = repository.getAllQuizzes()


    private fun initMenu() {
        val adapter =
            MenuAdapter(this@MainActivity, quizList)
        viewPager.adapter = adapter
        viewPager.setPageTransformer(
            true,
            DepthPageTransformer()
        )
        viewPager.offscreenPageLimit = 2
    }

}
