package mmatijevic.ferit.hr.ui.fragments

import android.app.AlertDialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_word_guess.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.common.loadImage
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import mmatijevic.ferit.hr.ui.base.BaseFragment


class WordGuessFragment : BaseFragment() {
    private lateinit var data: MutableMap<String,String>

    private var word: String = ""

    private val alphabet = arrayOf(
        "A", "B", "C", "Č", "Ć",
        "D", "Đ", "E",
        "F", "G", "H", "I", "J",
        "K", "L", "M", "N",
        "O", "P", "R", "S",
        "Š", "T", "U", "V", "Z", "Ž", "Y","W"
    )
    private val lettersNum = 20
    private val rowNum = 2
    override fun getLayoutResourceId(): Int =
        R.layout.fragment_word_guess

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSound(R.raw.quiz_enter)
        initUI()
    }

    override fun passData(data: MutableMap<String, String>) {
        this.data = data
    }

    private fun initUI(){
        word = data["word"].toString().toUpperCase()
        wordGuessText.text = data["instruction"]
        context?.let { loadImage(it,
            data["image1"] as String,imageHint) }
        context?.let { loadImage(it,
            data["image2"] as String,imageHint2) }

       addRows()
        val height: Int = (0.08* Resources.getSystem().displayMetrics.heightPixels).toInt()
        val guessRow = TableRow(context)
        guessRow.tag = "guessSpace"
       guessRow.gravity = Gravity.CENTER_HORIZONTAL
        val gd = GradientDrawable()
        gd.cornerRadius = 5f
        gd.setColor(Color.argb(85,255, 255, 255))
        gd.setStroke(1, Color.BLACK)
        for(i in word.indices){
            val textView = TextView(context)
            textView.text = ""
            textView.setTextColor(Color.BLACK)
            textView.background = gd
            textView.gravity = Gravity.CENTER
            textView.height = height
            textView.tag = i.toString()
            textView.setOnClickListener {
                if (textView.text != "") {
                var found = false
                    while (!found) {
                for(j in 0 until rowNum) {
                        val view = wordGuessLayout.findViewWithTag<TableRow>("buttonRow$j")
                        for (x in 0 until lettersNum / rowNum) {
                            val btn = view.findViewWithTag<Button>("btn$j$x")
                            if (btn.text == textView.text && !btn.isEnabled) {
                                btn.isEnabled = true
                                textView.text = ""
                                found = true
                                break
                            }
                        }
                    }
                    }
                }
            }
            guessRow.addView(textView)
        }
       guessLayout.addView(guessRow)
    }

    private fun addRows(){
        val lp = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        val rowList = mutableListOf<TableRow>()
        var counter = 0
        val indexes = mutableListOf<Int>()
        for(i in 0 until lettersNum){
            indexes.add(i)
        }
        indexes.shuffle()
        val shuffledLetters = mutableListOf<String>()
        for(i in word){
            shuffledLetters.add(i.toString())
        }
        shuffledLetters.shuffle()
        var sorted = 0
        for(i in 0 until rowNum){
                val newRow = TableRow(context)
                rowList.add(newRow)
            rowList[i].tag = "buttonRow$i"
                var buttonNum = 0
                while(buttonNum < 10){
                    val myButton = Button(context)
                    var text: String
                    if(sorted < word.length){
                        text = alphabet.random()
                        for(x in word.indices){
                            if(counter == indexes[x]){
                                text = shuffledLetters[sorted]
                                sorted++
                                break
                            }
                        }
                    }
                   else {
                        text = alphabet.random()
                    }

                    myButton.text = text
                    myButton.tag = "btn$i${buttonNum}"
                    myButton.setBackgroundColor(Color.WHITE)
                    val gd = GradientDrawable()
                    gd.setColor(Color.WHITE)

                    gd.cornerRadius = 5f
                    gd.setStroke(2, Color.BLACK)
                    myButton.background = gd

                    myButton.setOnClickListener {
                        val view = wordGuessLayout.findViewWithTag<TableRow>("guessSpace")
                        for(j in word.indices){
                            val tv = view.findViewWithTag<TextView>(j.toString())
                            if(tv.text!=""){
                                if(j == word.length-1)
                                    break
                               else {
                                    continue
                                }
                            }else {
                                tv.text = myButton.text
                                myButton.isEnabled = false
                                break
                            }
                        }

                            var correct = 0
                            for(k in word.indices){
                                val textView = view.findViewWithTag<TextView>(k.toString())
                                if(textView.text == word[k].toString()){
                                    correct++
                                }
                            }
                            if(correct>=word.length){
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle(word)
                                builder.setMessage(data["dialog_text"])

                                builder.setPositiveButton(android.R.string.yes) { _, _ ->
                                }

                                builder.show()
                                var questionNum: Int = QuizPrefs.getInt(
                                    QuizPrefs.KEY_QUESTION_NUM,
                                    0
                                )
                                questionNum++
                                QuizPrefs.store(QuizPrefs.KEY_QUESTION_NUM, questionNum)
                                val quizActivity: QuizActivity = activity as QuizActivity
                                quizActivity.changeFragment(questionNum)
                                startSound(R.raw.correct)
                            }
                    }
                    rowList[i].addView(myButton)
                    buttonNum++
                    counter++
                }
                lettersLayout.addView(rowList[i], lp)
        }
    }

    companion object {
        fun newInstance() = WordGuessFragment()
    }
}