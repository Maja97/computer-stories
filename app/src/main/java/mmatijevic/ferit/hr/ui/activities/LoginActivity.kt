package mmatijevic.ferit.hr.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import mmatijevic.ferit.hr.R
import mmatijevic.ferit.hr.ui.base.BaseActivity
import org.w3c.dom.Text

class LoginActivity : BaseActivity() {
    override fun getLayoutResourceId(): Int = R.layout.activity_login

    override fun setUpUi() {
        initListeners()
    }

    private fun initListeners(){
        loginBtn.setOnClickListener {
            when {
                TextUtils.isEmpty(username.text) -> {
                    username.error = getString(R.string.etUsernameError)
                }
                TextUtils.isEmpty(password.text) -> {
                    password.error = getString(R.string.etPasswordError)
                }
                else -> {
                    val etUsername = username.text.toString()
                    val etPwd = password.text.toString()
                    val db = FirebaseFirestore.getInstance()
                    db.collection("admin")
                        .get().addOnSuccessListener {
                            val username = it.documents[0].get("username").toString()
                            val password = it.documents[0].get("password").toString()
                            if(etUsername.trim()==username && etPwd.trim()==password){
                                val intent = Intent(this,EditDataActivity::class.java)
                                this.startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(this,getString(R.string.incorrectData),Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }

}