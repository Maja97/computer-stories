package mmatijevic.ferit.hr.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tasks.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.adapter.RecyclerViewAdapter
import mmatijevic.ferit.hr.common.QUIZZES_COLLECTION
import mmatijevic.ferit.hr.common.TIMESTAMP
import mmatijevic.ferit.hr.common.categories
import mmatijevic.ferit.hr.ui.activities.EditDataActivity
import mmatijevic.ferit.hr.utils.SwipeToDeleteCallback


class TasksFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private var position: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setFragmentResultListener("requestKey"){ key, bundle ->
            val result = bundle.getString("bundleKey")
            val adapter = tasks_recyclerview.adapter as RecyclerViewAdapter
            if (result != null) {
                adapter.addToEnd(result)
            }
            adapter.notifyDataSetChanged()
        }

        super.onCreate(savedInstanceState)
    }

    private fun initRecyclerView(){
        tasks_recyclerview.apply {
            layoutManager =  LinearLayoutManager(activity)

            db.collection("quizzes").whereEqualTo("id",position).get().addOnSuccessListener { snapshot ->
                snapshot.documents[0].reference.collection("tasks").orderBy("task_id").get().addOnSuccessListener {
                    val list = mutableListOf<String>()
                    for(i in 0 until it.size()){
                        list.add(it.documents[i].get("task_type").toString())
                    }
                    adapter = RecyclerViewAdapter(list)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editActivity: EditDataActivity = activity as EditDataActivity
        btnAddTask.setOnClickListener {
            val currentId = editActivity.getCurrentId()
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1)
                    .setTitle("Novi zadatak - odaberi kategoriju")
                    .setNeutralButton("Odustani") { _, _ ->

                    }
                    .setPositiveButton("OK") { dialog, _ ->
                        val lw: ListView = (dialog as AlertDialog).listView
                        val checkedItem: Int = lw.checkedItemPosition
                        val fm: FragmentManager = editActivity.supportFragmentManager
                        val addTaskDialogFragment: AddTaskDialogFragment =
                            AddTaskDialogFragment.newInstance(checkedItem, currentId)
                        addTaskDialogFragment.show(fm, AddTaskDialogFragment.TAG)
                    }
                    .setSingleChoiceItems(categories, 0) { _, _ ->
                    }
                    .show()
            }
        }
        arguments?.takeIf { it.containsKey("FRAGMENT_POSITION") }?.apply {
            position = getInt("FRAGMENT_POSITION")
        }
        initRecyclerView()
        val swipeHandler = object : SwipeToDeleteCallback(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val adapter = tasks_recyclerview.adapter as RecyclerViewAdapter

                val pos = viewHolder.absoluteAdapterPosition
                db.collection("quizzes").whereEqualTo("id",position).get().addOnSuccessListener { snapshot ->
                    snapshot.documents[0].reference.collection("tasks").orderBy("task_id").get().addOnSuccessListener {
                        it.documents[pos].reference.delete()
                        if(it.size() <= 1){
                            snapshot.documents[0].reference.delete()
                            db.collection(QUIZZES_COLLECTION).orderBy(TIMESTAMP).get().addOnSuccessListener { snap ->
                                for (i in 0 until snap.size()) {
                                    snap.documents[i].reference.update("id", i + 1)
                                }
                                editActivity.addData()
                            }
                        }else {
                            for(i in pos+1 until it.size()){
                                it.documents[i].reference.update("task_id",i)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
                adapter.removeAt(viewHolder.absoluteAdapterPosition)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(tasks_recyclerview)
    }

    companion object {
        fun newInstance(): Fragment  = TasksFragment()

    }




}