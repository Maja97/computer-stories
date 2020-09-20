package mmatijevic.ferit.hr.ui.fragments

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_story.*
import kotlinx.android.synthetic.main.fragment_word_finder.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.app.QuizApplication
import mmatijevic.ferit.hr.common.loadBackground
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import mmatijevic.ferit.hr.ui.base.BaseFragment
import java.util.*


class WordFinderFragment : BaseFragment() {
    private lateinit var data: MutableMap<String,String>

    private var gridSize = 10
    private var gridItemNum = 100
    private var correct = arrayOf(0,0,0,0,0)
    private var counter = 0
    private lateinit var orientation: String
    private val markedIds = mutableListOf<Int>()
    private var currentWord = ""
    private val views = mutableListOf<View>()
     private val alphabet = arrayOf(
         "A", "B", "C", "Č", "Ć",
         "D", "Đ", "E",
         "F", "G", "H", "I", "J",
         "K", "L", "M", "N",
         "O", "P", "R", "S",
         "Š", "T", "U", "V", "Z", "Ž"
     )
    private val orientations = arrayOf("up","rightUp","right","rightDown","down","leftDown","left","leftUp")

    private val letters = mutableListOf<String>()

    private var descriptions = arrayOf<String?>()
    private var highlightColor = 0
    private var color = 0

    override fun getLayoutResourceId(): Int =
        R.layout.fragment_word_finder

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startSound(R.raw.quiz_enter)
        initUI()
        initListeners()
    }

    override fun passData(data: MutableMap<String, String>) {
        this.data = data
    }

    private fun initUI() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(QuizApplication.getRes().getString(R.string.osmosmjerka))
        builder.setMessage(data["message"])

        builder.setPositiveButton(android.R.string.yes) { _, _ ->
        }

        builder.show()
        val height: Int = (0.8*Resources.getSystem().displayMetrics.heightPixels).toInt()
        wordFinder.layoutParams.width = height

        for(i in 0 until gridItemNum)letters.add("")
        color = Color.WHITE
        val words = arrayOf(data["word1"]?.toUpperCase(Locale.ROOT),data["word2"]?.toUpperCase(Locale.ROOT),
            data["word3"]?.toUpperCase(Locale.ROOT),data["word4"]?.toUpperCase(Locale.ROOT),data["word5"]?.toUpperCase(Locale.ROOT))

        word1.text = data["word1"]?.toUpperCase(Locale.ROOT)
        word2.text = data["word2"]?.toUpperCase(Locale.ROOT)
        word3.text = data["word3"]?.toUpperCase(Locale.ROOT)
        word4.text = data["word4"]?.toUpperCase(Locale.ROOT)
        word5.text = data["word5"]?.toUpperCase(Locale.ROOT)
        setWords(words)

        val desc = arrayOf(data["description1"],data["description2"],data["description3"],data["description4"],data["description5"])

        descriptions = desc
        highlightColor = Color.parseColor("#FAFA2A")
        wordsBackground.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#382452"))
        wordFinder.setBackgroundColor(color)

        context?.let { loadBackground(it,
            data["background"] as String,wordFinderLayout) }

        val gridHeight = wordContainer.height -40

        val adapter= object : ArrayAdapter<String>(requireContext(), R.layout.custom_style, letters){
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tvCell = super.getView(position, convertView, parent)
                tvCell.layoutParams.height = gridHeight/gridSize
                return tvCell
            }
        }

        wordFinder.adapter = adapter
        adapter.notifyDataSetChanged()
        wordFinder.isVerticalScrollBarEnabled = false

    }

    private fun setWords(wordArray: Array<String?>){
        for(word in wordArray){
            val wordLength = (word?.length ?: 1) -1
            var x = 0
            var y = 0
            var placed = false

            while(!placed){

                when(orientations.random()){
                    "up" -> {
                        x = 0
                        y = -1
                    }
                    "down" -> {
                        x = 0
                        y = 1
                    }
                    "left" ->{
                        x = -1
                        y = 0
                    }
                    "right" ->{
                        x = 1
                        y = 0
                    }
                    "leftUp" ->{
                        x = -1
                        y = -1
                    }
                    "rightUp" ->{
                        x = 1
                        y = -1
                    }
                    "leftDown" ->{
                        x = -1
                        y = 1
                    }
                    "rightDown" ->{
                        x = 1
                        y = 1
                    }
                }

                val index = (0 until gridItemNum).random()
                val xPosition = index % 10
                val yPosition = index / 10

                val xEnd = xPosition + wordLength * x
                val yEnd = yPosition + wordLength * y

                if(xEnd < 0 || xEnd >= gridSize) continue
                if(yEnd < 0 || yEnd >= gridSize) continue

                var failed = false

                for(i in 0..wordLength){
                    val char = word?.get(i)

                    val newX = xPosition + i*x
                    val newY = yPosition + i*y
                    val newIndex = newY * 10 + newX

                    if(letters[newIndex]!=""){
                        if(letters[newIndex]==char.toString()){
                            continue
                        }
                        failed = true
                        break
                    }else
                        continue
                }

                if(failed){
                    continue
                }else{
                    for(i in 0..wordLength){
                        val char = word?.get(i)

                        val newX = xPosition + i*x
                        val newY = yPosition + i*y
                        val newIndex = newY * 10 + newX

                        letters[newIndex] = char.toString()
                    }
                    placed = true
                }

            }
        }

        for(i in 0 until gridItemNum){
            if(letters[i] == "")
                letters[i] = alphabet.random()
        }
    }

    private fun initListeners() {
        val quizActivity: QuizActivity = activity as QuizActivity

        wordFinder.setOnItemClickListener { parent, view, position, _ ->
            if(counter==0){
                view.setBackgroundColor(highlightColor)
                markedIds.add(position)
                views.add(view)
                counter++
                currentWord += parent.getItemAtPosition(position)
            }else {
                val prev = markedIds[markedIds.lastIndex]
                if(counter==1){
                    if(isNear(position,prev)){
                        calculateOrientation(position,prev)
                        appendChoice(parent,position,view)
                    }else {
                        clearChoice(parent,position,view)
                    }
                }else{
                    if(isNear(position,prev)){
                        when(orientation){
                            "up" -> if(position == prev-gridSize)appendChoice(parent,position,view) else clearChoice(parent,position,view)
                            "rightUp" -> if(position == prev-(gridSize-1))appendChoice(parent,position,view) else clearChoice(parent,position,view)
                            "right" -> if(position == prev+1)appendChoice(parent,position,view) else clearChoice(parent,position,view)
                            "rightDown" -> if(position == prev+gridSize+1)appendChoice(parent,position,view) else clearChoice(parent,position,view)
                            "down" -> if(position == prev+gridSize)appendChoice(parent,position,view) else clearChoice(parent,position,view)
                            "leftDown" -> if(position == prev+(gridSize-1))appendChoice(parent,position,view) else clearChoice(parent,position,view)
                            "left" -> if(position == prev-1)appendChoice(parent,position,view) else clearChoice(parent,position,view)
                            "leftUp" -> if(position == prev-(gridSize+1))appendChoice(parent,position,view) else clearChoice(parent,position,view)
                        }
                        when(currentWord){
                            word1.text -> {
                                wordGuessed(parent,position,view)
                                alert(word1.text as String, 0)
                                word1.paintFlags = word1.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                correct[0]++
                            }
                            word2.text -> {
                                wordGuessed(parent,position,view)
                                alert(word2.text as String, 1)
                                word2.paintFlags = word2.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                correct[1]++
                            }
                            word3.text -> {
                                wordGuessed(parent,position,view)
                                alert(word3.text as String, 2)
                                word3.paintFlags = word3.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                correct[2]++
                            }
                            word4.text -> {
                                wordGuessed(parent,position,view)
                                alert(word4.text as String, 3)
                                word4.paintFlags = word4.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                correct[3]++
                            }
                            word5.text -> {
                                if(correct[4]==0){
                                    wordGuessed(parent,position,view)
                                    alert(word5.text as String, 4)
                                    word5.paintFlags = word5.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                    correct[4]++
                                }
                            }
                        }
                        var won = 0
                        for(i in correct){
                            if(i>0){
                                won++
                            }
                        }
                        if(won==descriptions.size){
                            var questionNum: Int = QuizPrefs.getInt(QuizPrefs.KEY_QUESTION_NUM, 0)
                            questionNum++
                            QuizPrefs.store(QuizPrefs.KEY_QUESTION_NUM, questionNum)
                            quizActivity.changeFragment(questionNum)
                            startSound(R.raw.correct)
                        }

                    }else {
                        clearChoice(parent,position,view)
                    }
                }
            }
        }
    }

    private fun wordGuessed(parent: AdapterView<*>, position: Int, view: View){
        val handler = Handler()
        startSound(R.raw.correct)
        views.forEach { it.setBackgroundColor(Color.GREEN) }
        handler.postDelayed(Runnable {
            clearChoice(parent,position,view)

        }, 1000)
    }

    private fun alert(word:String,description: Int){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(word)
        builder.setMessage(descriptions[description])

        builder.setPositiveButton(android.R.string.yes) { _, _ ->
        }

        builder.show()
    }

    private fun appendChoice(parent: AdapterView<*>, position: Int, view: View){
        markedIds.add(position)
        views.add(view)
        counter++
        view.setBackgroundColor(highlightColor)
        currentWord += parent.getItemAtPosition(position)
    }

    private fun clearChoice(parent: AdapterView<*>, position: Int, view: View){
        views.forEach { it.setBackgroundColor(color) }
        markedIds.clear()
        views.clear()
        counter = 0
        markedIds.add(position)
        views.add(view)
        view.setBackgroundColor(highlightColor
        )
        counter++
        currentWord = ""
        currentWord += parent.getItemAtPosition(position)
    }
    private fun isNear(position:Int, prev:Int): Boolean {
        val prevRow = prev / 10
        val currentRow = position / 10
        if(position == prev+1 || position == prev-1){
            return prevRow == currentRow
        }else if(position == prev+(gridSize-1) || position == prev-(gridSize-1)){
            return prevRow != currentRow
        } else if(position == prev +gridSize || position==prev-gridSize || position==prev+gridSize+1 || position==prev-(gridSize+1))
            return true
        return false
    }

    private fun calculateOrientation(position: Int, prev: Int){
        when (position) {
            prev-gridSize -> orientation = orientations[0]
            prev-(gridSize-1) -> orientation = orientations[1]
            prev+1 -> orientation = orientations[2]
            prev+gridSize+1 -> orientation = orientations[3]
            prev+gridSize ->orientation = orientations[4]
            prev+(gridSize-1) -> orientation = orientations[5]
            prev-1 ->orientation = orientations[6]
            prev-(gridSize+1) ->orientation = orientations[7]
        }
    }

    companion object {
        fun newInstance() = WordFinderFragment()
    }

}