package mmatijevic.ferit.hr.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_question.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.common.loadBackground
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import mmatijevic.ferit.hr.ui.base.BaseFragment

class QuestionFragment : BaseFragment() {
    private val quizId: Int = QuizPrefs.getInt(QuizPrefs.KEY_QUIZ_ID, 0)
    private lateinit var data: MutableMap<String,String>
    private var correctAnswer: String = ""
    override fun getLayoutResourceId() =
        R.layout.fragment_question

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initListeners()
        startSound(R.raw.quiz_enter)
    }

    override fun passData(data: MutableMap<String,String>){
        this.data = data
    }

    private fun initUI(){
        val answers = mutableListOf(data["answer1"], data["answer2"],data["answer3"],data["answer4"])
        answers.shuffle()
        answer1.text = answers[0]
        answer2.text = answers[1]
        answer3.text = answers[2]
        answer4.text = answers[3]
        question.text = data["question"]
        correctAnswer = data["answer1"].toString()
        when(data["answers_theme"]){
            "white" -> {
                answer1.buttonTintList=ColorStateList.valueOf(resources.getColor(R.color.white))
                answer1.setTextColor(Color.WHITE)
                answer2.buttonTintList=ColorStateList.valueOf(resources.getColor(R.color.white))
                answer2.setTextColor(Color.WHITE)
                answer3.buttonTintList=ColorStateList.valueOf(resources.getColor(R.color.white))
                answer3.setTextColor(Color.WHITE)
                answer4.buttonTintList=ColorStateList.valueOf(resources.getColor(R.color.white))
                answer4.setTextColor(Color.WHITE)
                context?.let { loadBackground(it,
                    data["background"] as String,questionLayout) }
            }
            else -> {
                context?.let { loadBackground(it,
                    data["background"] as String,questionLayout) }
            }
        }
    }

    private fun initListeners(){
        var questionNum: Int = QuizPrefs.getInt(QuizPrefs.KEY_QUESTION_NUM, 0)
        btn_next.setOnClickListener{
            if(radioGroup.checkedRadioButtonId != -1) {
                val checked: RadioButton? = view?.findViewById(radioGroup.checkedRadioButtonId)
                if(checked?.text == correctAnswer){
                questionNum++
                QuizPrefs.store(QuizPrefs.KEY_QUESTION_NUM, questionNum)
                val quizActivity: QuizActivity = activity as QuizActivity
                quizActivity.changeFragment(questionNum)
                startSound(R.raw.correct)
                }
                else {
                        Toast.makeText(
                            context,
                            "Netočan odgovor, pokušaj ponovno",
                            Toast.LENGTH_SHORT
                        ).show()
                        startSound(R.raw.incorrect)
                }
            }
            else {
                Toast.makeText(context,"Odaberi neki od ponuđenih odgovora",Toast.LENGTH_SHORT).show()
            }
            radioGroup.clearCheck()
        }
    }

    companion object {
        fun newInstance() = QuestionFragment()
    }
}