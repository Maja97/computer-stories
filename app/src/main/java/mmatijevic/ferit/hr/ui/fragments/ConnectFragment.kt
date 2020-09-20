package mmatijevic.ferit.hr.ui.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_connect.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.app.QuizApplication
import mmatijevic.ferit.hr.common.loadBackground
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import mmatijevic.ferit.hr.ui.base.BaseFragment


class ConnectFragment : BaseFragment() {
    private lateinit var data: MutableMap<String,String>
    private var shuffled = mutableListOf<Array<String>>()

    private var currentShuffled = mutableListOf<Array<String>>()
    private var currentOriginal = mutableListOf<Array<String>>()
    private var current = 0
    override fun getLayoutResourceId(): Int =
        R.layout.fragment_connect

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSound(R.raw.quiz_enter)
        initUI()
        showAlert()
        for(x in 0 until currentShuffled.size)
            currentOriginal.addAll(x, listOf(currentShuffled[x]))
        currentShuffled.shuffle()
        definition.text = currentShuffled[current][1]
        initListeners()
    }

    override fun passData(data: MutableMap<String, String>) {
        this.data = data
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(context)
        builder.setMessage(data["instruction"])

        builder.setPositiveButton(android.R.string.yes) { _, _ ->
        }

        builder.show()
    }

    private fun initUI(){

        for(i in 1..data["definition_number"]?.toInt()!!){
            val arr = arrayOf(data["word$i"].toString(), data["definition$i"].toString())
            shuffled.add(arr)
        }

        currentShuffled = shuffled

        if(data["text_color"] == "white")
            definition.setTextColor(Color.WHITE)

        context?.let { loadBackground(it,
            data["background"] as String,connectDefinitionLayout) }

    }

    private fun initListeners() {

        val count = currentOriginal.count()

        for (i in 0 until count){
            val gd = GradientDrawable()
            gd.setColor(Color.WHITE)
            gd.setStroke(1,Color.BLACK)
            val btn = Button(context)
            btn.text = currentOriginal[i][0]
            btn.background = gd
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(20, 20, 20, 0)
            btn.layoutParams = params
            btn.tag = "button$i"
            buttons.addView(btn)

            btn.setOnClickListener {
                if(definition.text == currentOriginal[i][1])
                {
                    current++
                    startSound(R.raw.correct)
                    if(current>=currentOriginal.size)
                    {
                        startSound(R.raw.correct)
                        val quizActivity: QuizActivity = activity as QuizActivity
                        var questionNum: Int = QuizPrefs.getInt(QuizPrefs.KEY_QUESTION_NUM, 0)
                        questionNum++
                        QuizPrefs.store(QuizPrefs.KEY_QUESTION_NUM, questionNum)
                        quizActivity.changeFragment(questionNum)
                    }
                    else
                    definition.text = currentShuffled[current][1]
                } else startSound(R.raw.incorrect)

            }
        }
    }

    companion object {
        fun newInstance() = ConnectFragment()
    }
}