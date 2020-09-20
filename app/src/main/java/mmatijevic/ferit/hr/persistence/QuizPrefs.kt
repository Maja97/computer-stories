package mmatijevic.ferit.hr.persistence

import androidx.preference.PreferenceManager
import com.google.firebase.firestore.QuerySnapshot
import mmatijevic.ferit.hr.app.QuizApplication

object QuizPrefs {
    const val KEY_QUESTION_NUM = "KEY_QUESTION_NUMBER"
    const val KEY_STORY_NUM = "KEY_STORY_NUM"
    const val KEY_QUIZ_ID = "KEY_QUIZ_ID"
    const val KEY_QUIZ_LOADED = "KEY_QUIZ_LOADED"
    const val KEY_IMAGE_ID = "KEY_IMAGE_ID"
    private fun sharedPrefs () =
        PreferenceManager.getDefaultSharedPreferences(QuizApplication.getAppContext())
    fun store(key: String , value: Int) {
        val editor = sharedPrefs().edit()
        editor.putInt(key, value).apply()
    }
    fun getInt (key: String , defaultValue: Int ): Int =
        sharedPrefs().getInt(key, defaultValue )
    fun storeString(key: String,value: String){
        val editor = sharedPrefs().edit()
        editor.putString(key,value).apply()
    }
    fun getString(key: String): String? = sharedPrefs().getString(key,"")
}
