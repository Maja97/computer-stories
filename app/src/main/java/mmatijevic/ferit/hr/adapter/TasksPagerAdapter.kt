package mmatijevic.ferit.hr.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import mmatijevic.ferit.hr.ui.fragments.TasksFragment

class TasksPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    private var titles = mutableListOf<String>()

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = TasksFragment.newInstance()

        fragment.arguments = Bundle().apply {
            putInt("FRAGMENT_POSITION", position + 1)
        }
        return fragment
    }

    fun addTitles(titles: MutableList<String>){
        this.titles= titles
    }

    fun deleteAll(){
        titles.clear()
    }

    fun getTitle(position: Int): String = titles[position]

}