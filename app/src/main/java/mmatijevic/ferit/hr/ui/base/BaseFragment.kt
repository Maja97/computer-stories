package mmatijevic.ferit.hr.ui.base

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mmatijevic.ferit.hr.db.QuizRoomRepository
import mmatijevic.ferit.hr.persistence.QuizRepository

abstract class BaseFragment: androidx.fragment.app.Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutResourceId(), container, false)
    }

    abstract fun getLayoutResourceId() : Int
    abstract fun passData(data: MutableMap<String,String>)


    fun startSound(resId:Int){
        MediaPlayer.create(context, resId)?.start()
    }

}