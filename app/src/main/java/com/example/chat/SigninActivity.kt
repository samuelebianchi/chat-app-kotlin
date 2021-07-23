package com.example.chat

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.parcel.Parcelize
import org.w3c.dom.Text
import java.util.*


class SigninActivity : AppCompatActivity() {

    var selectedPhotoUri : Uri? = null

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        Log.d("SigninActivity", "Photo has been selected")

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val photo_button = findViewById<Button>(R.id.photo_button)
        val userphoto_view = findViewById<CircleImageView>(R.id.userphoto_view)
        userphoto_view.setImageBitmap(bitmap)

        photo_button.alpha = 0f

        selectedPhotoUri = uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val button_signin = findViewById<Button>(R.id.button_signin)
        button_signin.setOnClickListener {
            performRegister()
        }

        val login_textview = findViewById<TextView>(R.id.login_textview)
        login_textview.setOnClickListener{
            Log.d("SigninActivity", "Try to show login activity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        val photo_button = findViewById<Button>(R.id.photo_button)
        photo_button.setOnClickListener {
            Log.d("SigninActivity", "Try to show photo selector")
            getContent.launch("image/*")
        }


    }


    private fun performRegister(){
        val email_signin = findViewById<EditText>(R.id.email_signin)
        val email_val = email_signin.text.toString()
        val password_signin = findViewById<EditText>(R.id.password_signin)
        val password_val = password_signin.text.toString()

        if(password_val.isEmpty() || email_val.isEmpty()) {
            Toast.makeText(this, "Per registrarti, inserisci correttamente mail e password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("SigninActivity", "Email is "+email_val)
        Log.d("SigninActivity", "Password is $password_val")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email_val, password_val)
            .addOnCompleteListener{
                if(!it.isSuccessful) return@addOnCompleteListener


                Log.d("SigninActivity", "Successfully created user with uid:")
                uploadImage()
            }
            .addOnFailureListener{
                Log.d("SigninActivity", "Failed to create a new user ${it.message}")
                Toast.makeText(this, "Per registrarti, inserisci correttamente mail e password", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImage(){

        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { it ->
                Log.d("SigninActivity", "The added image is : ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("SigninActivity", "File Location: $it")

                    insertUser(it.toString())
                }
            }
            .addOnFailureListener{

            }
    }

    private fun insertUser(image_url: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val email_signin = findViewById<EditText>(R.id.email_signin)
        val username_val = findViewById<TextView>(R.id.username_signin).text.toString()
        val email_val = email_signin.text.toString()
        val user = User(uid, username_val, image_url)

        Log.d("SigninActivity", "Uid is $uid")
        Log.d("SigninActivity", "Ref is $ref")
        Log.d("SigninActivity", "Email is $email_val")

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("SigninActivity", "Finally we saved the user to db")
                val intent = Intent(this, ChatListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d("SigninActivity", "Error insert user: {${it.message}}")
            }
    }
}

@Parcelize
class User(val uid:String, val username:String, val image_url:String): Parcelable{
    constructor(): this("", "", "")
}