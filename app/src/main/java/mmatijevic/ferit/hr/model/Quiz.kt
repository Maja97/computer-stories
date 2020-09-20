package mmatijevic.ferit.hr.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.storage.StorageReference

@Entity
data class Quiz(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var quizNumber: Int,
    val title: String,
    val image: String
)

