package mmatijevic.ferit.hr.ui.fragments

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_story.btn_next4
import kotlinx.android.synthetic.main.fragment_story.story1_layout
import kotlinx.android.synthetic.main.fragment_story.storyText
import kotlinx.android.synthetic.main.fragment_story.storyTitle
import kotlinx.android.synthetic.main.fragment_story2.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.common.loadBackground
import mmatijevic.ferit.hr.persistence.QuizPrefs
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import mmatijevic.ferit.hr.ui.base.BaseFragment


class StoryFragment : BaseFragment() {
    private lateinit var data: MutableMap<String,String>

    override fun getLayoutResourceId(): Int = when(data["layout"]){
        "1" -> R.layout.fragment_story
        "2" -> R.layout.fragment_story2
        else -> R.layout.fragment_story
    }

    override fun passData(data: MutableMap<String,String>){
        this.data = data
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSound(R.raw.next)
        initUI()
        initListeners()
    }

    private fun initUI() {
        val title = data["title"]
        val story = data["description"]
        when(data["layout"]){
            "1" -> {
                storyTitle.text = title
                storyText.text = story
                context?.let { loadBackground(it,
                   data["background"] as String,story1_layout) }
            }
            "2" -> {
                storyTitle2.text = title
                storyText2.text = story
                context?.let { loadBackground(it,
                    data["background"] as String,story2_layout) }
            }
        }
    }

    private fun initListeners() {
        btn_next4.setOnClickListener {
            val quizActivity: QuizActivity = activity as QuizActivity
            var questionNum: Int = QuizPrefs.getInt(QuizPrefs.KEY_QUESTION_NUM, 0)
            questionNum++
            QuizPrefs.store(QuizPrefs.KEY_QUESTION_NUM, questionNum)
            quizActivity.changeFragment(questionNum)
        }
    }

    companion object {
        fun newInstance() = StoryFragment()
    }
}