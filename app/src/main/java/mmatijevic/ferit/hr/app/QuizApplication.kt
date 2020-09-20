package mmatijevic.ferit.hr.app

import android.app.Application
import android.content.Context
import android.content.res.Resources

class QuizApplication : Application() {

    companion object {
        private lateinit var instance: QuizApplication

        fun getAppContext(): Context = instance.applicationContext
        fun getRes(): Resources = instance.resources
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

}