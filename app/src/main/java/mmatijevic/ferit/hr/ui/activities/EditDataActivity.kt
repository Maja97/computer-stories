package mmatijevic.ferit.hr.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_edit_data.*
import kotlinx.android.synthetic.main.dialog_add_quiz.view.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.adapter.TasksPagerAdapter
import mmatijevic.ferit.hr.common.*
import mmatijevic.ferit.hr.ui.fragments.AddTaskDialogFragment
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*


class EditDataActivity : AppCompatActivity() {
    private var quizImage: Uri? = null
    private lateinit var subView: View

    private  val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppThemeNoActionBar)
        setContentView(R.layout.activity_edit_data)
        toolbar.inflateMenu(R.menu.admin_menu)
        addData()
        initListeners()
    }

    fun getCurrentId(): Int = viewPagerData.currentItem + 1

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun initListeners(){

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.addQuiz -> {
                    val inflater = LayoutInflater.from(this)
                    subView = inflater.inflate(R.layout.dialog_add_quiz, null)
                    subView.ivAddQuizImage.setOnClickListener { openGallery() }
                  val dialog =  AlertDialog.Builder(this)
                        .setView(subView)
                        .setPositiveButton(getString(R.string.nastavi)) { _, _ -> }
                        .show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            if (quizImage!=null){

                                db.collection(QUIZZES_COLLECTION).get().addOnSuccessListener { snap ->
                                    val id = snap.size() + 1
                                    val title = subView.etQuizTitle.text.toString()
                                    val timestamp = Date()

                                    val data = hashMapOf(
                                        "id" to id,
                                        "title" to title,
                                        "timestamp" to timestamp
                                    )
                                        val storageRef = FirebaseStorage.getInstance().reference.child("images/menu/")

                                        val uId = uniqueId()
                                        val imgData = getByteArray()

                                        storageRef.child("$uId.jpg").putBytes(imgData)
                                            .addOnSuccessListener {
                                                it.storage.downloadUrl.addOnSuccessListener {uri ->
                                                    data["image"] = uri.toString()
                                                    db.collection(QUIZZES_COLLECTION).document().set(data)
                                                }
                                            }
                                    quizImage = null
                                    MaterialAlertDialogBuilder(this)
                                        .setTitle(getString(R.string.newTask))
                                        .setPositiveButton("OK") { dialog, _ ->
                                            val lw: ListView = (dialog as AlertDialog).listView
                                            val checkedItem: Int = lw.checkedItemPosition
                                            val fm: FragmentManager = supportFragmentManager
                                            val addTaskDialogFragment: AddTaskDialogFragment =
                                                AddTaskDialogFragment.newInstance(checkedItem, id)
                                            addTaskDialogFragment.show(fm, AddTaskDialogFragment.TAG)
                                        }
                                        .setSingleChoiceItems(categories, 0) { _, _ ->
                                        }
                                        .setCancelable(false)
                                        .show()
                                }
                                dialog.dismiss()
                            }else {
                                Toast.makeText(this,getString(R.string.imageNotLoaded),Toast.LENGTH_SHORT).show()
                            }
                        }
                    true
                }
                R.id.deleteCurrent -> {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.deleteStory))
                        .setPositiveButton(R.string.yes) { _, _ ->
                            db.collection(QUIZZES_COLLECTION).whereEqualTo(
                                "id",
                                viewPagerData.currentItem + 1
                            ).get().addOnSuccessListener { snapshot ->
                                snapshot.documents[0].reference.collection(TASKS_COLLECTION).get()
                                    .addOnSuccessListener {
                                        for (i in 0 until it.size()) {
                                            it.documents[i].reference.delete()
                                        }
                                        snapshot.documents[0].reference.delete()
                                        db.collection(QUIZZES_COLLECTION).orderBy(TIMESTAMP).get()
                                            .addOnSuccessListener { snapshot ->
                                                for (i in 0 until snapshot.size()) {
                                                    snapshot.documents[i].reference.update(
                                                        "id",
                                                        i + 1
                                                    )
                                                }
                                            }
                                        addData()
                                    }
                            }
                        }
                        .setNegativeButton(R.string.no) { dialog, _ -> dialog.cancel() }
                        .show()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE) {
            data?.data?.let {
                quizImage = it
                val text = subView.findViewById(R.id.quizImageLoadedPath) as TextView
                text.text = it.path
            }

        }
        else if(resultCode == Activity.RESULT_CANCELED){}
    }

    private fun getByteArray(): ByteArray {
        val ins : InputStream? = quizImage?.let { contentResolver.openInputStream(it) };
        val img : Bitmap? = BitmapFactory.decodeStream(ins);
        ins?.close();
        val baos = ByteArrayOutputStream()
        img?.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        return baos.toByteArray()
    }

     fun addData(){

        viewPagerData.isUserInputEnabled = false
        db.collection(QUIZZES_COLLECTION).orderBy("id").get().addOnSuccessListener {

            val titles = mutableListOf<String>()
            for(i in 0 until it.size()){
                titles.add(it.documents[i].get("title").toString())
            }

            val adapter = TasksPagerAdapter(this)
            adapter.deleteAll()
            adapter.addTitles(titles)
            viewPagerData.adapter = adapter
            TabLayoutMediator(tabLayout, viewPagerData) { tab, position ->
                tab.text = adapter.getTitle(position)
                viewPagerData.setCurrentItem(tab.position, true)
            }.attach()

        }

    }

    override fun onBackPressed() {
        finish()
    }
}