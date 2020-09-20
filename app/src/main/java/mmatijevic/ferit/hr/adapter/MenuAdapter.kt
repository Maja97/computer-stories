package mmatijevic.ferit.hr.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.common.EXTRA_QUIZ_ID
import mmatijevic.ferit.hr.common.isOnline
import mmatijevic.ferit.hr.model.Quiz
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import mmatijevic.ferit.hr.utils.GlideApp

class MenuAdapter(private var context: Context, private var quizList: List<Quiz>): androidx.viewpager.widget.PagerAdapter(){

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return p0 == p1
    }

    override fun getCount(): Int {
        return quizList.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_item, container, false)
        val name = view.findViewById<View>(R.id.quizName) as TextView
        val image = view.findViewById<View>(R.id.image) as ImageView
        val description = view.findViewById<View>(R.id.description) as TextView

        name.text= quizList[position].quizNumber.toString()
        description.text = quizList[position].title

        GlideApp.with(context).load(quizList[position].image).into(image)

        view.setOnClickListener{
            if(isOnline(context)) {
               loadActivity(position)
            }else {
                if(QuizPrefs.getInt(QuizPrefs.KEY_QUIZ_LOADED + quizList[position].quizNumber,0) != 0){
                   loadActivity(position)
                }
                else Toast.makeText(context,"Internet connection needed to fetch quiz data",Toast.LENGTH_SHORT).show()
            }
        }

        container.addView(view)
        return view
    }

    private fun loadActivity(position: Int){
        val questionIntent = Intent(context, QuizActivity::class.java)
        questionIntent.putExtra(EXTRA_QUIZ_ID, quizList[position].quizNumber)
        context.startActivity(questionIntent)
    }


}