package com.example.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.chatlist_row.view.*
import java.net.URI
import java.net.URL
import java.util.*


class EditProfile : AppCompatActivity() {

    var selectedPhotoUri : Uri? = null

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        Log.d("EditProfile", "Photo has been selected")

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val photo_button = findViewById<Button>(R.id.photo_button_edit)
        val userphoto_view = findViewById<CircleImageView>(R.id.userphoto_view_edit)
        userphoto_view.setImageBitmap(bitmap)

        photo_button.alpha = 0f

        selectedPhotoUri = uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Modifica Profilo"

        val photo_button = findViewById<Button>(R.id.photo_button_edit)

        photo_button.setOnClickListener {
            Log.d("EditProfile", "Try to show photo selector")
            getContent.launch("image/*")
        }

        loadProfile()

        val button_edit = findViewById<Button>(R.id.button_edit)
        button_edit.setOnClickListener {
           editProfile()
        }
    }

    private fun loadProfile(){

        val id_user = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("users/$id_user").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val email_edit = findViewById<EditText>(R.id.email_edit)
                val password_edit = findViewById<EditText>(R.id.password_edit)
                val username_edit = findViewById<EditText>(R.id.username_edit)
                val password_edit_new = findViewById<EditText>(R.id.password_edit_new)
                val password_edit_new2 = findViewById<EditText>(R.id.password_edit_new2)

                val user = snapshot.getValue(User::class.java)
                val photo_button = findViewById<Button>(R.id.photo_button_edit)
                val userphoto_view: CircleImageView = findViewById(R.id.userphoto_view_edit)
                Picasso.get().load(user?.image_url?.toUri()).into(userphoto_view)
                photo_button.alpha = 0f

                username_edit.setText(user?.username)
                email_edit.setText( FirebaseAuth.getInstance().currentUser?.email)
            }

        })

    }

    private fun editProfile(){

        val email_edit = findViewById<EditText>(R.id.email_edit)
        val password_edit = findViewById<EditText>(R.id.password_edit)
        val username_edit = findViewById<EditText>(R.id.username_edit)
        val password_edit_new = findViewById<EditText>(R.id.password_edit_new)
        val password_edit_new2 = findViewById<EditText>(R.id.password_edit_new2)

        val email_edit_val = email_edit.text.toString()
        val password_edit_val = password_edit.text.toString()
        val username_edit_val = username_edit.text.toString()
        val password_edit_new_val = password_edit_new.text.toString()
        val password_edit_new2_val = password_edit_new2.text.toString()

        var errors = ""
        var exit = 0

        if(password_edit_new_val != password_edit_new2_val){
            errors= "$errors Le due password non corrispondono. "
            exit = 1
        }
        if(password_edit_new_val.isEmpty() || password_edit_new2_val.isEmpty()){
            errors= "$errors La nuova passwword non può essere vuota. "
            exit = 1
        }

        FirebaseAuth.getInstance().currentUser?.let {
            val credential = EmailAuthProvider.getCredential(email_edit_val, password_edit_val)
            it.reauthenticate(credential)
                    .addOnCompleteListener{task ->
                        when{
                            task.isSuccessful ->{
                                if(exit == 0){
                                    it.updateEmail(email_edit_val)
                                    it.updatePassword(password_edit_new_val)
                                    Toast.makeText(this, "Le tue credenziali sono state modificate con successo.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            task.exception is FirebaseAuthInvalidCredentialsException ->{
                                //errors = "$errors Le credenziali attuali non sono corrette. "
                                Toast.makeText(this, "Le credenziali attuali non sono corrette.", Toast.LENGTH_LONG).show()
                            }
                            else -> Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
        }
        if(!errors.isEmpty()){
            Toast.makeText(this, errors, Toast.LENGTH_LONG).show()
        }
    }

}
