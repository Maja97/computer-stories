package mmatijevic.ferit.hr.ui.fragments

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_puzzle.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.common.loadBackground
import mmatijevic.ferit.hr.ui.activities.QuizActivity
import mmatijevic.ferit.hr.ui.base.BaseFragment


class PuzzleFragment : BaseFragment() {
    private lateinit var data: MutableMap<String,String>

    override fun getLayoutResourceId(): Int =
        R.layout.fragment_puzzle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSound(R.raw.quiz_enter)
        initUI()
        initListeners()
    }

    override fun passData(data: MutableMap<String, String>) {
        this.data = data
    }

    private fun initUI(){
        puzzleText.text = data["instruction"]
        context?.let { loadBackground(it,
            data["background"] as String,puzzle_layout) }
    }

    private fun initListeners() {
        val quizActivity: QuizActivity = activity as QuizActivity
        takePicture.setOnClickListener{
           quizActivity.dispatchCameraIntent()
        }
    }

    companion object {
        fun newInstance() = PuzzleFragment()
    }
}