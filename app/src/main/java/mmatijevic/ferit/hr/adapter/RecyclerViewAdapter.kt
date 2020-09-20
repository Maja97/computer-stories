package mmatijevic.ferit.hr.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.task_item.view.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.common.inflate

class RecyclerViewAdapter(private val list: MutableList<String>): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflatedView = parent.inflate(R.layout.task_item, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTaskId.text = (position+1).toString()
        holder.tvTaskType.text = list[position]
    }

    override fun getItemCount(): Int = list.size

    fun removeAt(position: Int){
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun addToEnd(element: String){
        list.add(element)
        notifyItemInserted(list.lastIndex)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvTaskType: TextView = view.tvTaskType
        val tvTaskId: TextView = view.tvTaskId
    }

}