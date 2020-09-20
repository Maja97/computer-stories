package mmatijevic.ferit.hr.ui.fragments

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.fragment_memory.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.common.loadBackground
import mmatijevic.ferit.hr.ui.base.BaseFragment
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import kotlin.math.abs


class MemoryFragment : BaseFragment() {
    private lateinit var data: MutableMap<String,String>

    private var numRows = 0
    private var numColumns = 0
    private var numCards = 0
    private var indexFirst : Int = 0
    private var firstI = 0
    private var firstJ = 0
    private var paired = 0
    private var front = arrayOf<Array<Boolean>>()
    private var current = mutableListOf<MutableList<String>>()
    override fun getLayoutResourceId(): Int =
        R.layout.fragment_memory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSound(R.raw.quiz_enter)
        initUI()
        initListeners()
    }

    private fun initUI(){
        memoryText.text = data["instruction"]
        val arr = mutableListOf<MutableList<String>>()
        for (i in 0 until data["num_pairs"].toString().toInt()){
            val pair = mutableListOf<String>()
            for(j in 0 until 2){
                pair.add(data["image$i$j"].toString())
            }
            arr.add(pair)
        }
        current = arr
        when(data["num_pairs"].toString().toInt()){
            5 -> {
                numRows = 2
                numColumns = 5
            }
            6 -> {
                numRows = 3
                numColumns = 4
            }
            7 -> {
                numRows = 2
                numColumns = 7
            }
            8 -> {
                numRows = 4
                numColumns = 4
            }
            9 -> {
                numRows = 3
                numColumns = 6
            }
            10 -> {
                numRows = 4
                numColumns = 5
            }
        }
        numCards = numColumns*numRows

    }

    override fun passData(data: MutableMap<String, String>) {
        this.data = data
    }

    private fun initListeners() {
        for (i in 0 until numRows) {
            var array = arrayOf<Boolean>()
            for (j in 0 until numColumns) {
                array += false
            }
            front += array
        }
        val indexes = mutableListOf<Int>()
        for(i in 0 until numCards){
            indexes.add(i)
        }
        indexes.shuffle()

        for(i in 0 until numRows){
            val row = TableRow(context)
            row.gravity = Gravity.CENTER
            row.tag = "row$i"
            for(j in 0 until numColumns){
                val tv = TextView(context)
                tv.gravity = Gravity.CENTER
                tv.setBackgroundResource(R.drawable.memory_backside)
                tv.tag ="back$i$j"
                val iv = ImageView(context)
                val index = indexes[numColumns*i+j]
                if(index<numCards/2) {
                    context?.let { loadBackground(it,
                        current[index][0],iv) }
                    iv.adjustViewBounds = true
                    iv.scaleType = ImageView.ScaleType.FIT_XY
                    iv.tag = "front$i$j"
                }else {
                    context?.let { loadBackground(it,
                        current[index%(numCards/2)][1],iv) }
                    iv.adjustViewBounds = true
                    iv.scaleType = ImageView.ScaleType.FIT_XY
                    iv.tag = "front$i$j"
                }

                val layout = context?.let { FrameLayout(it) }
                iv.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT)
                tv.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT)
                layout?.addView(iv)
                layout?.addView(tv)

                layout?.setPadding(10,10,10,10)
                row.addView(layout, 0,FrameLayout.LayoutParams.MATCH_PARENT)
                layout?.setOnClickListener {
                    val r = memoryLayout.findViewWithTag<TableRow>("row$i")
                    val back = r.findViewWithTag<TextView>("back$i$j")
                    if(back.isEnabled){
                        var count = 0
                        for(x in 0 until numColumns){
                            count += front.count {
                                it[x]
                            }
                        }
                        val animOut = AnimatorInflater.loadAnimator(context, R.animator.animation_out) as AnimatorSet
                        val animIn = AnimatorInflater.loadAnimator(context, R.animator.animation_in) as AnimatorSet
                        if(count>=2){
                            for(k in 0 until numRows){
                                for(l in 0 until numColumns) {
                                    val aOut = AnimatorInflater.loadAnimator(context, R.animator.animation_out) as AnimatorSet
                                    val aIn = AnimatorInflater.loadAnimator(context, R.animator.animation_in) as AnimatorSet
                                    if(front[k][l]){
                                        val r1 = memoryLayout.findViewWithTag<TableRow>("row$k")
                                        aOut.setTarget(r1.findViewWithTag("front$k$l"))
                                        aIn.setTarget(r1.findViewWithTag("back$k$l"))
                                        aOut.start()
                                        aIn.start()
                                        front[k][l] = false
                                    }
                                }
                            }
                            if(!front[i][j]){
                                firstI = i
                                firstJ = j
                                indexFirst = indexes[numColumns*i+j]
                                animOut.setTarget(tv)
                                animIn.setTarget(iv)
                                animOut.start()
                                animIn.start()
                                front[i][j]= true
                            }
                        } else {
                            if(!front[i][j]){
                                animOut.setTarget(tv)
                                animIn.setTarget(iv)
                                animOut.start()
                                animIn.start()
                                front[i][j]= true
                            }

                            if(count==0){
                                firstI = i
                                firstJ = j
                                indexFirst = indexes[numColumns*i+j]
                            }
                            if(count == 1){
                                if(abs(indexes[numColumns*i+j]-indexFirst)==numCards/2) {
                                    val row1 = memoryLayout.findViewWithTag<TableRow>("row$i")
                                    val row2 = memoryLayout.findViewWithTag<TableRow>("row$firstI")
                                    val view =  row1.findViewWithTag<TextView>("back$i$j")
                                    val view2 = row2.findViewWithTag<TextView>("back$firstI$firstJ")
                                    view2.isEnabled = false
                                    front[firstI][firstJ] = false
                                    view.isEnabled = false
                                    front[i][j] = false
                                    paired++
                                    startSound(R.raw.correct)
                                    if(paired >= numCards/2){
                                        val handler = Handler()
                                        handler.postDelayed({
                                            val quizActivity: QuizActivity = activity as QuizActivity
                                            var questionNum: Int = QuizPrefs.getInt(QuizPrefs.KEY_QUESTION_NUM, 0)
                                            questionNum++
                                            QuizPrefs.store(QuizPrefs.KEY_QUESTION_NUM, questionNum)
                                            quizActivity.changeFragment(questionNum)
                                        }, 1500)
                                    }
                                }else{
                                    for(x in 0 until numRows){
                                        for(y in 0 until numColumns) {
                                            val r2 = memoryLayout.findViewWithTag<TableRow>("row$x")
                                            r2.getChildAt(y).isClickable = false
                                        }
                                    }

                                    Handler().postDelayed({
                                        startSound(R.raw.incorrect)
                                        for(k in 0 until numRows){
                                            for(l in 0 until numColumns) {
                                                val aOut = AnimatorInflater.loadAnimator(context, R.animator.animation_out) as AnimatorSet
                                                val aIn = AnimatorInflater.loadAnimator(context, R.animator.animation_in) as AnimatorSet
                                                if(front[k][l]){
                                                    val r1 = memoryLayout.findViewWithTag<TableRow>("row$k")
                                                    aOut.setTarget(r1.findViewWithTag("front$k$l"))
                                                    aIn.setTarget(r1.findViewWithTag("back$k$l"))
                                                    aOut.start()
                                                    aIn.start()
                                                    front[k][l] = false
                                                }
                                            }
                                        }
                                        for(x in 0 until numRows){
                                            for(y in 0 until numColumns) {
                                                val r2 = memoryLayout.findViewWithTag<TableRow>("row$x")
                                                r2.getChildAt(y).isClickable = true
                                            }
                                        }
                                    }, 2000)

                                }
                            }
                        }
                    }
                }
            }
            val lp = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 0.5f)
            memoryLayout.addView(row,lp)
        }
    }

    companion object {
        fun newInstance() = MemoryFragment()
    }
}