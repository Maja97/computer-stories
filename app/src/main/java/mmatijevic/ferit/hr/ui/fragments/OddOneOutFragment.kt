package mmatijevic.ferit.hr.ui.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_odd_one_out.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.common.loadBackground
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import mmatijevic.ferit.hr.ui.base.BaseFragment


class OddOneOutFragment : BaseFragment() {
    private lateinit var data: MutableMap<String,String>

    private var words = mutableListOf<String>()
    private var correct = ""
    override fun getLayoutResourceId(): Int =
        R.layout.fragment_odd_one_out

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSound(R.raw.quiz_enter)
        initUI()
        correct = words[0]
        words.shuffle()
        initListeners()
    }

    override fun passData(data: MutableMap<String, String>) {
        this.data = data
    }

    private fun initUI(){
        for(i in 1..(data["word_number"]?.toInt()!!)){
            words.add(data["word$i"].toString())
        }
        oddOneOutText.text = data["instruction"]
        oddOneOutText.setBackgroundColor(Color.argb(85,0,0,0))
        oddOneOutText.setTextColor(Color.WHITE)
        context?.let { loadBackground(it,
            data["background"] as String,oddOneOutLayout) }
    }

    private fun initListeners() {
        for(i in 0 until words.size){
            val btn = Button(context)
            btn.text = words[i]
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val gd = GradientDrawable()
            gd.setColor(Color.WHITE)
            gd.setStroke(1,Color.BLACK)
            btn.background = gd
            params.setMargins(0, 20, 30, 0)
            btn.setPadding(15,0,15,0)

            btn.layoutParams = params

            btn.setBackgroundColor(Color.WHITE)

            choices.addView(btn)
            btn.setOnClickListener {
                if(btn.text == correct){
                    startSound(R.raw.correct)
                    val quizActivity: QuizActivity = activity as QuizActivity
                    var questionNum: Int = QuizPrefs.getInt(QuizPrefs.KEY_QUESTION_NUM, 0)
                    questionNum++
                    QuizPrefs.store(QuizPrefs.KEY_QUESTION_NUM, questionNum)
                    quizActivity.changeFragment(questionNum)
                } else startSound(R.raw.incorrect)

            }
        }

    }

    companion object {
        fun newInstance() = OddOneOutFragment()
    }
}