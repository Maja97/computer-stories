package mmatijevic.ferit.hr.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_puzzle.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.adapter.ImageAdapter
import mmatijevic.ferit.hr.common.EXTRA_QUIZ_ID
import mmatijevic.ferit.hr.common.isOnline
import mmatijevic.ferit.hr.common.rotate
import mmatijevic.ferit.hr.db.QuizRoomRepository
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.persistence.QuizRepository
import mmatijevic.ferit.hr.ui.base.BaseActivity
import mmatijevic.ferit.hr.ui.fragments.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.shuffle

class QuizActivity : BaseActivity() {
    private val repository: QuizRepository = QuizRoomRepository()
    private val REQUEST_IMAGE_CAPTURE = 100
    lateinit var currentPath: String
    override fun getLayoutResourceId() = R.layout.activity_quiz
    private var id: Int = 0
    private val rowNum = 3

    private var clicked: Boolean = false
    private var currentIndex:Int = 0

    private var prev: Bitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888)
    private var current: Bitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888)

    private val db = FirebaseFirestore.getInstance()
    override fun setUpUi() {
        id = intent?.getIntExtra(EXTRA_QUIZ_ID, -1) ?: -1
        QuizPrefs.store(QuizPrefs.KEY_QUIZ_ID,id)
        QuizPrefs.store(QuizPrefs.KEY_STORY_NUM,0)
        changeFragment(1)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.backToMenu))
            .setPositiveButton(getString(R.string.yes)){ _, _ -> finish() }
            .setNegativeButton(getString(R.string.no)){ dialog, _ -> dialog.cancel()}
            .show()
    }

    fun changeFragment(questionNum: Int) {
            val tasks = repository.getTasksByQuiz(id)

        if(questionNum > tasks.size){
            QuizPrefs.store(QuizPrefs.KEY_QUIZ_LOADED + id,1)
            finish()
        }else{
            if(isOnline(applicationContext) || QuizPrefs.getInt(QuizPrefs.KEY_QUIZ_LOADED + id, 0)!=0){
                var data = mutableMapOf<String,String>()
                for(i in 0 until tasks.size){
                    if(tasks[i].taskNumber == questionNum){

                        data = tasks[i].map
                        break
                    }
                }

                when(data["type"]){
                    "story" -> {
                        val newFragment = StoryFragment.newInstance()
                        newFragment.passData(data)
                        showFragment(newFragment)
                    }
                    "question" -> {
                        val newFragment = QuestionFragment.newInstance()
                        newFragment.passData(data)
                        showFragment(newFragment)
                    }
                    "puzzle" -> {
                        val newFragment = PuzzleFragment.newInstance()
                        newFragment.passData(data)
                        showFragment(newFragment)
                    }
                    "word_guess" -> {
                        val newFragment = WordGuessFragment.newInstance()
                        newFragment.passData(data)
                        showFragment(newFragment)
                    }
                    "odd_one_out" -> {
                        val newFragment = OddOneOutFragment.newInstance()
                        newFragment.passData(data)
                        showFragment(newFragment)
                    }
                    "connect" -> {
                        val newFragment = ConnectFragment.newInstance()
                        newFragment.passData(data)
                        showFragment(newFragment)
                    }
                    "memory" -> {
                        val newFragment = MemoryFragment.newInstance()
                        newFragment.passData(data)
                        showFragment(newFragment)
                    }
                    "sort" -> {
                        val newFragment = SortFragment.newInstance()
                        newFragment.passData(data)
                        showFragment(newFragment)
                    }
                    "word_finder" -> {
                        val newFragment = WordFinderFragment.newInstance()
                        newFragment.passData(data)
                        showFragment(newFragment)
                    }
                }
            }else if(QuizPrefs.getInt(QuizPrefs.KEY_QUIZ_LOADED + id,0)==0){
                Toast.makeText(applicationContext,"Internet needed to load the next game",Toast.LENGTH_SHORT).show()
            }

        }


    }

    fun dispatchCameraIntent(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null){
            var photoFile: File? = null
            try{
                photoFile = createImage()
            }catch (e:IOException){
                e.printStackTrace()
            }
            if(photoFile != null){
                val photoUri = FileProvider.getUriForFile(this, "hr.ferit.fileprovider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
                try {
                    val file = File(currentPath)
                    val uri = Uri.fromFile(file)
                    val bitmap1 = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    if(bitmap1.width < bitmap1.height){
                        bitmap1.rotate(90)
                    }
                    val bitmapHeight = (0.7 * puzzle_layout.height).toInt()
                    val bitmapWidth = ((bitmap1.width.toDouble()/bitmap1.height.toDouble()) * bitmapHeight).toInt()
                    val bitmap = Bitmap.createScaledBitmap(bitmap1, bitmapWidth, bitmapHeight, true)
                    val bitmaps = mutableListOf<Bitmap>()
                    val bitmapsOrdered = mutableListOf<Bitmap>()
                    for(i in 0..rowNum){
                        for(j in 0..rowNum){
                            val bm = Bitmap.createBitmap(
                                bitmap,
                                j*(bitmapWidth/4),
                                i*(bitmapHeight/4),
                                bitmapWidth/4,
                                bitmapHeight/4
                            )
                            bitmaps.add(bm)
                            bitmapsOrdered.add(bm)
                        }
                    }

                    shuffle(bitmaps)
                    val adapter = ImageAdapter(this, bitmaps)
                    gridView.layoutParams.width = bitmapWidth
                    gridView.layoutParams.height = bitmapHeight
                    gridView.isVerticalScrollBarEnabled = false
                    gridView.adapter = adapter
                    puzzleText.text = ""
                    gridView.onItemClickListener =
                        AdapterView.OnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
                            if (!clicked) {
                                currentIndex = position
                                clicked = true
                                prev = adapter.getItem(position)
                            } else {
                                clicked = false
                                current = adapter.getItem(position)
                                bitmaps.remove(prev)
                                bitmaps.add(position,prev)
                                bitmaps.remove(current)
                                bitmaps.add(currentIndex, current)
                                adapter.notifyDataSetChanged()
                                var counter = 0
                                bitmaps.forEachIndexed{ index, element ->
                                    if(element == bitmapsOrdered[index]){
                                        counter ++
                                    }
                                }
                                if(counter == bitmaps.size) {
                                    var questionNum: Int = QuizPrefs.getInt(QuizPrefs.KEY_QUESTION_NUM, 0)
                                    questionNum++
                                    QuizPrefs.store(QuizPrefs.KEY_QUESTION_NUM, questionNum)
                                    changeFragment(questionNum)
                                    MediaPlayer.create(this, R.raw.correct)?.start()
                                }
                            }
                        }
                }
                catch (e: IOException){
                    e.printStackTrace()
                }
            }
    }

    private fun createImage():File{
        val timeStamp = SimpleDateFormat("yyMMddHHmmssZ", Locale.ENGLISH).format(Date())
        val imageName = "JPEG" + timeStamp+ "_"
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageName,".jpg", dir)
        currentPath = image.absolutePath
        return image
    }

}