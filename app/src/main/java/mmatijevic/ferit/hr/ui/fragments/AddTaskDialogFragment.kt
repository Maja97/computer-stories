package mmatijevic.ferit.hr.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.dialog_add_connect.*
import kotlinx.android.synthetic.main.dialog_add_memory.*
import kotlinx.android.synthetic.main.dialog_add_odd_one_out.*
import kotlinx.android.synthetic.main.dialog_add_puzzle.*
import kotlinx.android.synthetic.main.dialog_add_question.*
import kotlinx.android.synthetic.main.dialog_add_sort.*
import kotlinx.android.synthetic.main.dialog_add_story.*
import kotlinx.android.synthetic.main.dialog_add_word_finder.*
import kotlinx.android.synthetic.main.dialog_add_word_guess.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.common.*
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.ui.activities.EditDataActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream


class AddTaskDialogFragment : DialogFragment() {

    private lateinit var image: Uri
    private lateinit var image1: Uri
    private var numPairs = 5
    private var numDefinitions = 1
    private var numLeft = 1
    private var numRight = 1
    private var numWords = 2
    private var taskData = hashMapOf<String,Any>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return when(arguments?.getInt("index")){
            0 -> inflater.inflate(R.layout.dialog_add_story, container, false)
            1 -> inflater.inflate(R.layout.dialog_add_question, container, false)
            2 -> inflater.inflate(R.layout.dialog_add_memory, container, false)
            3 -> inflater.inflate(R.layout.dialog_add_connect, container, false)
            4 -> inflater.inflate(R.layout.dialog_add_sort, container, false)
            5 -> inflater.inflate(R.layout.dialog_add_puzzle, container, false)
            6 -> inflater.inflate(R.layout.dialog_add_word_finder, container, false)
            7 -> inflater.inflate(R.layout.dialog_add_word_guess, container, false)
            8 -> inflater.inflate(R.layout.dialog_add_odd_one_out, container, false)
            else -> return inflater.inflate(R.layout.dialog_add_story, container, false)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val index = arguments?.getInt("index")
        if (index != null) {
            initListeners(index)
        }
        dialog?.setCancelable(false)

        super.onViewCreated(view, savedInstanceState)
    }

    private fun initListeners(index: Int){
        val id = arguments?.getInt("id")
        val db = FirebaseFirestore.getInstance()
        val storageRef = FirebaseStorage.getInstance().reference.child("images/tasks/")
        when(index){
            0 -> {
                ivAddStoryImage.setOnClickListener { openGallery(REQUEST_CODE_PICK_IMAGE) }
                btnAddStory.setOnClickListener {
                    val title = etAddStoryTitle.text.toString()
                    val description = etAddStoryDesc.text.toString()
                    val checked: RadioButton? = view?.findViewById(storyThemeRadiogroup.checkedRadioButtonId)
                    var layout = "1"
                    if(checked != null && description.isNotBlank()){
                        if(checked.text == getString(R.string.middle)){
                            layout = "2"
                        }
                        db.collection(QUIZZES_COLLECTION).whereEqualTo("id", id).get()
                            .addOnSuccessListener { snapshot ->
                                snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                    .orderBy("task_id").get().addOnSuccessListener { querySnapshot ->
                                        val data = hashMapOf(
                                            "task_id" to querySnapshot.size() + 1,
                                            "task_type" to "story",
                                            "title" to title,
                                            "description" to description,
                                            "story_layout" to layout
                                        )
                                        if (this::image.isInitialized) {
                                            val uId = uniqueId()
                                            val imgData = getByteArray(image)
                                            storageRef.child("$uId.jpg").putBytes(imgData)
                                                .addOnSuccessListener {
                                                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                                                        data["background"] = uri.toString()
                                                        snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                                    }
                                                }
                                        } else {
                                            data["background"] = ""
                                            snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                        }
                                        refreshData("story", querySnapshot.size())
                                        dialog?.dismiss()
                                    }
                            }



                    }else Toast.makeText(context,"Unesi tekst priče i odaberi položaj teksta",Toast.LENGTH_SHORT).show()
                }
            }
            1 -> {
                ivAddQuestionImage.setOnClickListener { openGallery(REQUEST_CODE_PICK_IMAGE) }
                btnAddQuestion.setOnClickListener {
                    val question = etAddQuestion.text.toString()
                    val answer1 = etAddAnswer1.text.toString()
                    val answer2 = etAddAnswer2.text.toString()
                    val answer3 = etAddAnswer3.text.toString()
                    val answer4 = etAddAnswer4.text.toString()
                    val checked: RadioButton? = view?.findViewById(answersThemeRadiogroup.checkedRadioButtonId)
                    var theme = ""
                    if (checked != null && question.isNotBlank() && answer1.isNotBlank() && answer2.isNotBlank() && answer3.isNotBlank() && answer4.isNotBlank()) {
                        if(checked.text==getString(R.string.white)){
                            theme = "white"
                        }
                        db.collection(QUIZZES_COLLECTION).whereEqualTo("id", id).get()
                                .addOnSuccessListener { snapshot ->
                                    snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                        .orderBy("task_id").get().addOnSuccessListener { querySnapshot ->
                                            val data = hashMapOf(
                                                "task_id" to querySnapshot.size() + 1,
                                                "task_type" to "question",
                                                "question" to question,
                                                "answer1" to answer1,
                                                "answer2" to answer2,
                                                "answer3" to answer3,
                                                "answer4" to answer4,
                                                "answers_theme" to theme
                                            )
                                            if (this::image.isInitialized) {
                                                val uId = uniqueId()
                                                val imgData = getByteArray(image)
                                                storageRef.child("$uId.jpg").putBytes(imgData)
                                                    .addOnSuccessListener {
                                                        it.storage.downloadUrl.addOnSuccessListener { uri ->
                                                            data["background"] = uri.toString()
                                                            snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                                        }
                                                    }
                                            } else {
                                                data["background"] = ""
                                                snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                            }
                                            refreshData("question", querySnapshot.size())
                                            dialog?.dismiss()
                                        }
                                }
                    }else Toast.makeText(context,getString(R.string.enterAllQuestions),Toast.LENGTH_SHORT).show()
                }

            }
            2 -> {
                val numbers = listOf(5, 6, 7, 8, 9, 10)
                val adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, numbers)
                }
                val spinner = view?.findViewById<Spinner>(R.id.spinner)
                if (spinner != null) {
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            numPairs = numbers[position]
                            taskData.clear()
                            loadPairsLayout.removeAllViews()
                            for (i in 0 until numbers[position]) {
                                val layout = LinearLayout(context)
                                layout.orientation = LinearLayout.HORIZONTAL
                                val img1 = ImageView(context)
                                img1.setImageResource(R.drawable.ic_baseline_add_photo_alternate_24)
                                val lp = LinearLayout.LayoutParams(50, 50)
                                lp.setMargins(20, 20, 20, 20)
                                img1.layoutParams = lp
                                val img1Str = "image$i"+"0"
                                img1.setOnClickListener { openGallery(REQUEST_CODE_PICK_PAIRS)
                                    QuizPrefs.storeString(QuizPrefs.KEY_IMAGE_ID,img1Str)
                                }
                                val path1 = TextView(context)
                                path1.text = "-"
                                path1.setTextColor(Color.RED)
                                path1.tag = "image$i"+"0txt"
                                val img2 = ImageView(context)
                                img2.setImageResource(R.drawable.ic_baseline_add_photo_alternate_24)
                                img2.layoutParams = lp
                                val img2Str = "image$i"+"1"
                                img2.setOnClickListener { openGallery(REQUEST_CODE_PICK_PAIRS)
                                    QuizPrefs.storeString(QuizPrefs.KEY_IMAGE_ID,img2Str)
                                }
                                val path2 = TextView(context)
                                path2.text = "-"
                                path2.setTextColor(Color.RED)
                                path2.tag = "image$i"+"1txt"
                                layout.addView(path1)
                                layout.addView(img1)
                                layout.addView(img2)
                                layout.addView(path2)
                                loadPairsLayout.addView(layout)
                            }
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                }

                btnAddMemory.setOnClickListener {
                    val instruction = etMemoryInstruction.text.toString()
                    if(taskData.size >= numPairs*2){
                        db.collection(QUIZZES_COLLECTION).whereEqualTo("id", id).get()
                            .addOnSuccessListener { snapshot ->
                                snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                    .orderBy("task_id").get().addOnSuccessListener { querySnapshot ->
                                        val data = hashMapOf(
                                            "task_id" to querySnapshot.size() + 1,
                                            "task_type" to "memory",
                                            "instruction" to instruction,
                                            "num_pairs" to numPairs
                                        )
                                        for(i in 0 until numPairs){
                                            for(j in 0 until 2){
                                                val uId = uniqueId()
                                                val imgData = getByteArray(taskData["image$i$j"] as Uri)
                                                storageRef.child("$uId.jpg").putBytes(imgData)
                                                    .addOnSuccessListener {
                                                        it.storage.downloadUrl.addOnSuccessListener { uri ->
                                                            data["image$i$j"] = uri.toString()
                                                            if(i == numPairs-1 && j==1){
                                                                snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                                                    .document().set(data)
                                                            }
                                                        }
                                                    }
                                            }
                                        }
                                        refreshData("memory", querySnapshot.size())
                                        dialog?.dismiss()
                                    }
                            }
                    }else
                    Toast.makeText(context,"Učitaj sve parove",Toast.LENGTH_SHORT).show()
                }
            }
            3 -> {
                ivAddConnectImage.setOnClickListener { openGallery(REQUEST_CODE_PICK_IMAGE) }

                val numbers = listOf(1,2,3,4,5,6,7,8,9,10)
                val adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, numbers)
                }
                val spinner = view?.findViewById<Spinner>(R.id.spinnerConnect)
                if (spinner != null) {
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            numDefinitions = numbers[position]
                            taskData.clear()
                            loadDefinitionsLayout.removeAllViews()
                            for (i in 1..numbers[position]) {
                                val layout = LinearLayout(context)
                                layout.orientation = LinearLayout.HORIZONTAL
                                val lp1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,5f)

                                val word1 = EditText(context)
                                word1.tag = "etWord$i"
                                word1.layoutParams = lp1
                                val tv1 = TextView(context)
                                tv1.text = "Pojam $i"

                                val definition1 = EditText(context)
                                definition1.tag = "etDefinition$i"
                                definition1.layoutParams = lp1
                                val tv2 = TextView(context)
                                tv2.text = "Definicija $i"
                                layout.weightSum = 10f
                                layout.addView(tv1)
                                layout.addView(word1)
                                layout.addView(tv2)
                                layout.addView(definition1)
                                loadDefinitionsLayout.addView(layout)
                            }
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                }

                btnAddConnect.setOnClickListener {
                    val instruction = etAddConnectInstruction.text.toString()
                    val checked: RadioButton? = view?.findViewById(connectThemeRadiogroup.checkedRadioButtonId)
                    var allFieldsFilled = true
                    for(i in 1..numDefinitions){
                        taskData["word$i"] = view?.findViewWithTag<EditText>("etWord$i")?.text.toString()
                        taskData["definition$i"] = view?.findViewWithTag<EditText>("etDefinition$i")?.text.toString()
                        if(taskData["word$i"].toString().isBlank() || taskData["definition$i"].toString().isBlank())
                            allFieldsFilled = false
                    }
                    var textColor = ""
                    if (checked != null && instruction.isNotBlank() && allFieldsFilled) {
                        if (checked.text == getString(R.string.white)) {
                            textColor = "white"
                        }
                        db.collection(QUIZZES_COLLECTION).whereEqualTo("id", id).get()
                            .addOnSuccessListener { snapshot ->
                                snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                    .orderBy("task_id").get().addOnSuccessListener { querySnapshot ->
                                        val data = hashMapOf(
                                            "task_id" to querySnapshot.size() + 1,
                                            "task_type" to "connect",
                                            "instruction" to instruction,
                                            "text_color" to textColor,
                                            "definition_number" to numDefinitions
                                        )
                                        for(i in 1..numDefinitions){
                                            data["word$i"] = taskData["word$i"].toString()
                                            data["definition$i"] = taskData["definition$i"].toString()
                                        }
                                        if (this::image.isInitialized) {
                                            val uId = uniqueId()
                                            val imgData = getByteArray(image)

                                            storageRef.child("$uId.jpg").putBytes(imgData)
                                                .addOnSuccessListener {
                                                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                                                        data["background"] = uri.toString()
                                                        snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                                    }
                                                }
                                        } else {
                                            data["background"] = ""
                                            snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                        }
                                        refreshData("connect", querySnapshot.size())
                                        dialog?.dismiss()
                                    }
                            }
                } else Toast.makeText(context,getString(R.string.enterAllData),Toast.LENGTH_SHORT).show()
            }
            }
            4 -> {
                val numbers = listOf(1,2,3,4,5,6,7,8,9,10)
                val adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, numbers)
                }
                val spinnerLeft = view?.findViewById<Spinner>(R.id.spinnerSortLeft)
                if (spinnerLeft != null) {
                    spinnerLeft.adapter = adapter
                    spinnerLeft.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            numLeft = numbers[position]
                            taskData.clear()
                            loadSortLeft.removeAllViews()
                            for (i in 1..numbers[position]) {
                                val layout = LinearLayout(context)
                                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f)

                                layout.orientation = LinearLayout.HORIZONTAL
                                val txt = TextView(context)
                                val left = EditText(context)
                                txt.text = "Riječ $i"
                                left.tag = "left$i"
                                left.layoutParams = lp
                                layout.weightSum = 1f
                                layout.addView(txt)
                                layout.addView(left)
                                loadSortLeft.addView(layout)
                            }
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                }
                val spinnerRight = view?.findViewById<Spinner>(R.id.spinnerSortRight)
                if (spinnerRight != null) {
                    spinnerRight.adapter = adapter
                    spinnerRight.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            numRight = numbers[position]
                            taskData.clear()
                            loadSortRight.removeAllViews()
                            for (i in 1..numbers[position]) {
                                val layout = LinearLayout(context)
                                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f)

                                layout.orientation = LinearLayout.HORIZONTAL
                                val txt = TextView(context)
                                val right = EditText(context)
                                txt.text = "Riječ $i"
                                right.tag = "right$i"
                                right.layoutParams = lp
                                layout.weightSum = 1f
                                layout.addView(txt)
                                layout.addView(right)
                                loadSortRight.addView(layout)
                            }
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                }



                btnAddSort.setOnClickListener {
                    val message = etAddSortMessage.text.toString()
                    var allFieldsFilled = true
                    val leftText = etAddSortCategory1.text.toString()
                    val rightText = etAddSortCategory2.text.toString()
                    for(i in 1..numLeft){
                        taskData["left$i"] = view?.findViewWithTag<EditText>("left$i")?.text.toString()
                        if(taskData["left$i"].toString().isBlank())
                            allFieldsFilled = false
                    }
                    for(i in 1..numRight){
                        taskData["right$i"] = view?.findViewWithTag<EditText>("right$i")?.text.toString()
                        if(taskData["right$i"].toString().isBlank())
                            allFieldsFilled = false
                    }
                    if(allFieldsFilled && message.isNotBlank() && leftText.isNotBlank() && rightText.isNotBlank()) {
                        db.collection(QUIZZES_COLLECTION).whereEqualTo("id", id).get()
                            .addOnSuccessListener { snapshot ->
                                snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                    .orderBy("task_id").get()
                                    .addOnSuccessListener { querySnapshot ->
                                        val data = hashMapOf(
                                            "task_id" to querySnapshot.size() + 1,
                                            "task_type" to "sort",
                                            "message" to message,
                                            "left_size" to numLeft,
                                            "right_size" to numRight,
                                            "left_text" to leftText,
                                            "right_text" to rightText
                                        )
                                        for(i in 1..numLeft){
                                           data["left$i"] = taskData["left$i"].toString()
                                        }
                                        for(i in 1..numRight){
                                            data["right$i"] = taskData["right$i"].toString()
                                        }
                                        snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                            .document().set(data)

                                        refreshData("sort", querySnapshot.size())
                                        dialog?.dismiss()
                                    }
                            }
                    } else Toast.makeText(context,getString(R.string.enterAllData),Toast.LENGTH_SHORT).show()
                }
            }
            5 -> {
                ivAddPuzzleImage.setOnClickListener {
                    openGallery(REQUEST_CODE_PICK_IMAGE)
                }
                btnAddQuiz.setOnClickListener {
                    val instruction = etQuizTitle.text.toString()
                    db.collection(QUIZZES_COLLECTION).whereEqualTo("id", id).get()
                        .addOnSuccessListener { snapshot ->
                            snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                .orderBy("task_id").get().addOnSuccessListener { querySnapshot ->
                                    val data = hashMapOf(
                                        "task_id" to querySnapshot.size() + 1,
                                        "task_type" to "puzzle",
                                        "instruction" to instruction
                                    )
                                    if (this::image.isInitialized) {
                                        val uId = uniqueId()
                                        val imgData = getByteArray(image)

                                        storageRef.child("$uId.jpg").putBytes(imgData)
                                            .addOnSuccessListener {
                                                it.storage.downloadUrl.addOnSuccessListener { uri ->
                                                    data["background"] = uri.toString()
                                                    snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                                }
                                            }
                                    } else {
                                        data["background"] = ""
                                        snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                    }
                                    refreshData("puzzle", querySnapshot.size())
                                    dialog?.dismiss()
                                }
                        }
                }
            }
            6 -> {
                ivAddWordFinderImage.setOnClickListener { openGallery(REQUEST_CODE_PICK_IMAGE) }
                btnAddWordFinder.setOnClickListener {
                    val message = etAddWordFinderMessage.text.toString()
                    val word1 = etAddWord1.text.toString().toUpperCase()
                    val word2 = etAddWord2.text.toString().toUpperCase()
                    val word3 = etAddWord3.text.toString().toUpperCase()
                    val word4 = etAddWord4.text.toString().toUpperCase()
                    val word5 = etAddWord5.text.toString().toUpperCase()
                    val desc1 = etAddDescription1.text.toString()
                    val desc2 = etAddDescription2.text.toString()
                    val desc3 = etAddDescription3.text.toString()
                    val desc4 = etAddDescription4.text.toString()
                    val desc5 = etAddDescription5.text.toString()
                    if(message.isNotBlank() && word1.isNotBlank()&& word2.isNotBlank()&& word3.isNotBlank()&& word4.isNotBlank()
                        && word5.isNotBlank() && desc1.isNotBlank()  && desc2.isNotBlank()  && desc3.isNotBlank()
                        && desc4.isNotBlank() && desc5.isNotBlank()){
                        db.collection(QUIZZES_COLLECTION).whereEqualTo("id", id).get()
                            .addOnSuccessListener { snapshot ->
                                snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                    .orderBy("task_id").get().addOnSuccessListener { querySnapshot ->
                                        val data = hashMapOf(
                                            "task_id" to querySnapshot.size() + 1,
                                            "task_type" to "word_finder",
                                            "message" to message,
                                            "word1" to word1,
                                            "word2" to word2,
                                            "word3" to word3,
                                            "word4" to word4,
                                            "word5" to word5,
                                            "description1" to desc1,
                                            "description2" to desc2,
                                            "description3" to desc3,
                                            "description4" to desc4,
                                            "description5" to desc5
                                        )
                                        if (this::image.isInitialized) {
                                            val uId = uniqueId()
                                            val imgData = getByteArray(image)

                                            storageRef.child("$uId.jpg").putBytes(imgData)
                                                .addOnSuccessListener {
                                                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                                                        data["background"] = uri.toString()
                                                        snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                                    }
                                                }
                                        } else {
                                            data["background"] = ""
                                            snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                        }
                                        refreshData("word_finder", querySnapshot.size())
                                        dialog?.dismiss()
                                    }
                            }
                    } else Toast.makeText(context,getString(R.string.enterAllData),Toast.LENGTH_SHORT).show()
                }
            }
            7 -> {
                ivAddWordGuessImage1.setOnClickListener { openGallery(REQUEST_CODE_PICK_IMAGE) }
                ivAddWordGuessImage2.setOnClickListener { openGallery(REQUEST_CODE_PICK_IMAGE1) }
                btnAddWordGuess.setOnClickListener {
                    val word = etAddWord.text.toString().toUpperCase()
                    val instruction = etAddWordInstruction.text.toString()
                    val dialogText = etAddWordExplanation.text.toString()
                    if (this::image.isInitialized && this::image1.isInitialized && word.isNotBlank() && dialogText.isNotBlank()) {
                        db.collection(QUIZZES_COLLECTION).whereEqualTo("id", id).get()
                            .addOnSuccessListener { snapshot ->
                                snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                    .orderBy("task_id").get().addOnSuccessListener { querySnapshot ->
                                        val data = hashMapOf(
                                            "task_id" to querySnapshot.size() + 1,
                                            "task_type" to "word_guess",
                                            "word" to word,
                                            "instruction" to instruction,
                                            "dialog_text" to dialogText
                                        )
                                        for(i in 1..2) {
                                            val uId = uniqueId()
                                            var img : Uri = image
                                            if(i == 1) img=image
                                            else if(i == 2) img = image1
                                            val imgData = getByteArray(img)
                                            storageRef.child("$uId.jpg").putBytes(imgData)
                                                .addOnSuccessListener {
                                                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                                                        data["image$i"] = uri.toString()
                                                        if(i==2)
                                                        snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                                    }
                                                }
                                        }
                                        refreshData("word_guess", querySnapshot.size())
                                        dialog?.dismiss()
                                    }
                            }
                    }else Toast.makeText(context,getString(R.string.enterAllData),Toast.LENGTH_SHORT).show()
                }
            }
            8 -> {
                val numbers = listOf(2,3,4,5,6,7,8,9,10)
                val adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, numbers)
                }
                val spinner = view?.findViewById<Spinner>(R.id.spinnerOddOneOut)
                if (spinner != null) {
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            numWords = numbers[position]
                            taskData.clear()
                            loadOddOneOutLayout.removeAllViews()
                            for (i in 1..numbers[position]) {
                                val layout = LinearLayout(context)
                                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f)

                                layout.orientation = LinearLayout.HORIZONTAL
                                val txt = TextView(context)
                                val et = EditText(context)
                                if(i==1)
                                    txt.text = "Riječ $i (uljez)"
                                else txt.text = "Riječ $i"
                                et.tag = "word$i"
                                et.layoutParams = lp
                                layout.weightSum = 1f

                                layout.addView(txt)
                                layout.addView(et)
                                loadOddOneOutLayout.addView(layout)
                            }
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                }

                ivAddOddOneOutImage.setOnClickListener { openGallery(REQUEST_CODE_PICK_IMAGE) }
                btnAddOddOneOut.setOnClickListener {
                    val instruction = etAddOddOneOutInstruction.text.toString()
                    var allFieldsFilled = true
                    for (i in 1..numWords){
                        taskData["word$i"] = view?.findViewWithTag<EditText>("word$i")?.text.toString()
                        if(taskData["word$i"].toString().isBlank())
                            allFieldsFilled = false
                    }
                    if (instruction.isNotBlank() && allFieldsFilled) {
                        db.collection(QUIZZES_COLLECTION).whereEqualTo("id", id).get()
                            .addOnSuccessListener { snapshot ->
                                snapshot.documents[0].reference.collection(TASKS_COLLECTION)
                                    .orderBy("task_id").get().addOnSuccessListener { querySnapshot ->
                                        val data = hashMapOf(
                                            "task_id" to querySnapshot.size() + 1,
                                            "task_type" to "odd_one_out",
                                            "instruction" to instruction,
                                            "word_number" to numWords
                                        )
                                        for(i in 1..numWords){
                                            data["word$i"] = taskData["word$i"].toString()
                                        }
                                        if (this::image.isInitialized) {
                                            val uId = uniqueId()
                                            val imgData = getByteArray(image)

                                            storageRef.child("$uId.jpg").putBytes(imgData)
                                                .addOnSuccessListener {
                                                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                                                        data["background"] = uri.toString()
                                                        snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                                    }
                                                }
                                        } else {
                                            data["background"] = ""
                                            snapshot.documents[0].reference.collection(TASKS_COLLECTION).document().set(data)
                                        }
                                        refreshData("odd_one_out", querySnapshot.size())
                                        dialog?.dismiss()
                                    }
                            }
                    }else Toast.makeText(context,getString(R.string.enterAllData),Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun refreshData(result: String, size: Int){
        setFragmentResult("requestKey", bundleOf("bundleKey" to result))
        if (size == 0) {
            val editActivity: EditDataActivity =
                activity as EditDataActivity
            editActivity.addData()
        }
    }

    private fun getByteArray(uri: Uri): ByteArray {
        val ins : InputStream? = activity?.contentResolver?.openInputStream(uri);
        val img : Bitmap? = BitmapFactory.decodeStream(ins);
        ins?.close();
        val baos = ByteArrayOutputStream()
        img?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        return baos.toByteArray()
    }

    private fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE) {
            data?.data?.let {
                image = it
                when(arguments?.getInt("index")){
                    0 -> storyImageLoadedPath.text = it.path
                    3 -> connectImageLoadedPath.text = it.path
                    5 -> puzzleImageLoadedPath.text = it.path
                    6 -> wordFinderImageLoadedPath.text = it.path
                    7 -> wordGuessImageLoadedPath1.text = it.path
                }
            }
        }else if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK_PAIRS){
            val imgId = QuizPrefs.getString(QuizPrefs.KEY_IMAGE_ID)
            data?.data?.let {
                taskData["$imgId"] = it
            }
            val text = view?.findViewWithTag<TextView>(imgId+"txt")
            if (text != null) {
                text.text = "+"
                text.setTextColor(Color.GREEN)
            }
        }else if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE1) {
            data?.data?.let {
                image1 = it
                when(arguments?.getInt("index")){
                    7 -> wordGuessImageLoadedPath2.text = it.path
                }
            }
        }
    }

    companion object{
        const val TAG = "AddTaskDialog"

        fun newInstance(index: Int, id: Int): AddTaskDialogFragment {

            val f = AddTaskDialogFragment()

            val args = Bundle()
            args.putInt("index", index)
            args.putInt("id", id)
            f.arguments = args
            return f
        }
    }

}