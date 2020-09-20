package mmatijevic.ferit.hr.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.view.DragEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_sort.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.app.QuizApplication
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import mmatijevic.ferit.hr.ui.base.BaseFragment


class SortFragment : BaseFragment(), View.OnDragListener{
    private lateinit var data: MutableMap<String,String>

    private var current = mutableListOf<MutableList<String>>()
    private var currentShuffled = mutableListOf<String>()
    private var currentOriginal = mutableListOf<MutableList<String>>()
    override fun getLayoutResourceId(): Int =
        R.layout.fragment_sort

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSound(R.raw.quiz_enter)
        initUI()
        showAlert()
        for(i in current.indices){
            currentOriginal.addAll(i, listOf(current[i]))
        }
        currentShuffled.shuffle()
        initListeners()
    }

    override fun passData(data: MutableMap<String, String>) {
        this.data = data
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(context)
        builder.setMessage(data["message"])

        builder.setPositiveButton(android.R.string.yes) { _, _ ->
        }

        builder.show()
    }

    private fun initUI(){
        left_side_text.text = data["left_text"]
        right_side_text.text = data["right_text"]
        left_side.setBackgroundColor(Color.parseColor("#56A3A6"))
        unsorted.setBackgroundColor(Color.parseColor("#4F6D7A"))
        right_side.setBackgroundColor(Color.parseColor("#F5853F"))
        val left = mutableListOf<String>()
        for (i in 1..data["left_size"].toString().toInt()){
            left.add(data["left$i"].toString())
        }
        val right = mutableListOf<String>()
        for (i in 1..data["right_size"].toString().toInt()){
            right.add(data["right$i"].toString())
        }
        val array = mutableListOf<MutableList<String>>()
        array.add(left)
        array.add(right)
        current = array
        for (i in 0 until array.size){
            for (element in array[i]){
                currentShuffled.add(element)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        var count = 0
        var buttonNum = 0
        for(i in 0 until currentOriginal.count()){
            for(j in 0 until currentOriginal[i].count()){
                count++
            }
        }
        for(i in 0 until currentOriginal.size){
            for(j in currentOriginal[i].indices){
                val btn = Button(context)
                btn.tag = "button$i$j"
                btn.text = currentShuffled[buttonNum]
                val gd = GradientDrawable()
                gd.setColor(Color.WHITE)
                gd.setStroke(1,Color.BLACK)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 20, 0, 0)
                btn.layoutParams = params
                btn.background = gd
                buttonNum++

               btn.setOnTouchListener { v, event ->
                   val item = ClipData.Item(v.tag as CharSequence)

                   val mimeTypes =
                       arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                   val data = ClipData(v.tag.toString(), mimeTypes, item)

                   val dragshadow = DragShadowBuilder(v)

                   v.startDrag(data, dragshadow, v, 0)
                   true
               }
                unsorted.weightSum = count.toFloat()
                val p = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                p.weight = count-(count-1).toFloat()
                unsorted.addView(btn,p)
            }
        }

        left_side.weightSum = count.toFloat()
        right_side.weightSum = count.toFloat()
        left_side.setOnDragListener(this)
        right_side.setOnDragListener(this)
        unsorted.setOnDragListener(this)

        checkResult.setOnClickListener {
            var left = 0
            var otherLeft = 0
            var right = 0
            var otherRight = 0
            for(i in 0 until left_side.childCount){
                val btn = left_side.getChildAt(i) as Button
                if(currentOriginal[0].contains(btn.text)){
                    left++
                }else otherLeft++
            }
            for(i in 0 until right_side.childCount){
                val btn = right_side.getChildAt(i) as Button
                if(currentOriginal[1].contains(btn.text)){
                    right++
                }else otherRight++
            }
            if(currentOriginal[0].size == left && otherLeft == 0 && currentOriginal[1].size == right && otherRight == 0){
                startSound(R.raw.correct)
                val handler = Handler()
                handler.postDelayed( {
                    left_side.removeAllViews()
                    right_side.removeAllViews()
                    unsorted.setBackgroundResource(R.drawable.thumbs_up)
                    left_side.setBackgroundColor(Color.parseColor("#9A94BC"))
                    right_side.setBackgroundColor(Color.parseColor("#9A94BC"))
                    checkResult.text = QuizApplication.getRes().getString(R.string.nastavi)
                    checkResult.setOnClickListener {
                        val quizActivity: QuizActivity = activity as QuizActivity
                        var questionNum: Int = QuizPrefs.getInt(QuizPrefs.KEY_QUESTION_NUM, 0)
                        questionNum++
                        QuizPrefs.store(QuizPrefs.KEY_QUESTION_NUM, questionNum)
                        quizActivity.changeFragment(questionNum)
                    }
                }, 1000)

            }
            else {
                startSound(R.raw.incorrect)
                Toast.makeText(context,"Pokušaj ponovno", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance() = SortFragment()
    }

    override fun onDrag(v: View, event: DragEvent): Boolean {
       return when (event.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                v.background.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
                v.invalidate()
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                v.background.clearColorFilter()
                v.invalidate()
                true
            }
            DragEvent.ACTION_DROP -> {
                v.background.clearColorFilter()
                v.invalidate()
                val vw : View = event.localState as View
                val owner: ViewGroup=  vw.parent as ViewGroup
                owner.removeView(vw)
                val container : LinearLayout = v as LinearLayout
                container.addView(vw)
                vw.visibility = View.VISIBLE
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                v.background.clearColorFilter()
                v.invalidate()
                true
            }
            else -> false
        }
    }
}