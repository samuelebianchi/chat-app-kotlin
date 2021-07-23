package com.example.chat

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.left_row.view.*
import kotlinx.android.synthetic.main.right_row.view.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ChatActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    var receiver : User? = null

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAA1W9758Q:APA91bF6LVs03Ue2RRyMU9kCCXECwLRsMPk6GwpyIpk3TCHPOznfLCFkHiOEs8b90AaS-ixEuuddpdyXUmWjb84T4CQSYGhDL89LOlkbGcv3rz1-wQjE3mogIII9drsUZaJVpzoL_1Wu"
    private val contentType = "application/json"

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }

    companion object{
        var date_object: Message? = null
        var sender_name :String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val recycler_view = findViewById<RecyclerView>(R.id.recycler_view_messages)
        recycler_view.adapter = adapter
        receiver = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = receiver?.username

        val speechButton = findViewById<ImageButton>(R.id.button_speech_to_text)

        speechButton.setOnClickListener {
            speechFunction()
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}"+"/username").addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                sender_name = snapshot.getValue(String::class.java)
                Log.d("ChatActivity", "Got user:  "+sender_name)
            }})

        listen()

        val send_button = findViewById<ImageButton>(R.id.button_send)
        send_button.setOnClickListener {
            Log.d("Chat", "Button clicked")
            sendMessage()

            val new_message = findViewById<EditText>(R.id.edittext_newmessage).text.toString()

            val topic = "/topics/${receiver?.uid}"
            val notification = JSONObject()
            val notifcationBody = JSONObject()

            try {
                notifcationBody.put("title", sender_name)
                notifcationBody.put("message", new_message)
                notification.put("to", topic)
                notification.put("data", notifcationBody)
                Log.e("TAG", "try")
            } catch (e: JSONException) {
                Log.e("TAG", "onCreate: " + e.message)
            }

            sendNotification(notification)
        }

    }

    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(this@ChatActivity, "Request error", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }


    private fun listen(){
        val id_sender = FirebaseAuth.getInstance().uid
        var id_receiver = receiver?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$id_sender/$id_receiver")

        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if(message != null) {

                    if(date_object == null || !compareDates(date_object?.datatime, message.datatime)){
                        adapter.add(Datatime(message.datatime))
                    }

                    if(message.id_sender == FirebaseAuth.getInstance().uid){
                        val currentUser = ChatListActivity.current_user ?: return
                        adapter.add(RightRow(message, currentUser))
                    }else {
                        adapter.add(LeftRow(message, receiver!!))
                    }

                    date_object = message
                }

                findViewById<RecyclerView>(R.id.recycler_view_messages).scrollToPosition(adapter.itemCount-1)
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

        })

    }

    override fun onPause() {
        super.onPause()
        date_object = null
    }

    class Message(val id: String, val text: String, val id_sender:String, val id_receiver:String, val datatime:Long){
        constructor() : this("", "", "", "", -1)
    }

    private fun compareDates(date1: Long?, date2:Long):Boolean{

        val day_format = SimpleDateFormat("dd.MM.yy")
        if (date1 != null) {
            if(day_format.format(date1*1000 ) == day_format.format(date2*1000))
                return true
        }
        return false
    }

    private fun sendMessage(){

        val new_message = findViewById<EditText>(R.id.edittext_newmessage).text.toString()

        val id_sender = FirebaseAuth.getInstance().uid
        val id_receiver =  intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)?.uid

        if(id_sender == null)return

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$id_sender/$id_receiver").push()

        val ref2 = FirebaseDatabase.getInstance().getReference("/user-messages/$id_receiver/$id_sender").push()

        val message = Message(ref.key!!, new_message, id_sender, id_receiver!!, System.currentTimeMillis() / 1000)

        ref.setValue(message)
            .addOnSuccessListener {
                Log.d("Chat", "Messaggio inviato")
                findViewById<EditText>(R.id.edittext_newmessage).text.clear()
                recycler_view_messages.scrollToPosition(adapter.itemCount -1)
            }

        ref2.setValue(message)
                .addOnSuccessListener {
                    Log.d("Chat", "Messaggio inviato")
                }

        val latest_ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$id_sender/$id_receiver")
        latest_ref.setValue(message)
        val latest_ref2 = FirebaseDatabase.getInstance().getReference("/latest-messages/$id_receiver/$id_sender")
        latest_ref2.setValue(message)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val res: ArrayList<String> = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
            val edit = findViewById<EditText>(R.id.edittext_newmessage)
            edit.setText(res[0])
        }
    }


    fun speechFunction(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ITALY.toString())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di qualcosa...")

        try {
            resultLauncher.launch(intent)
        }catch(exp:ActivityNotFoundException){
            Toast.makeText(applicationContext, "Speech not supported", Toast.LENGTH_LONG).show()
        }
    }

}

class LeftRow(val message: ChatActivity.Message, val user:User): Item<GroupieViewHolder>(){

    val day_format = SimpleDateFormat("hh.mm")

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.left_row).text = message.text
        val date = java.util.Date(message.datatime * 1000)
        viewHolder.itemView.findViewById<TextView>(R.id.time_left).text = day_format.format(date)

        val uri = user.image_url
        val targetImageView = viewHolder.itemView.imageView_left
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.left_row
    }

}

class RightRow(val message: ChatActivity.Message,  val user:User): Item<GroupieViewHolder>(){

    val day_format = SimpleDateFormat("hh.mm")

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.right_row).text = message.text
        val date = java.util.Date(message.datatime * 1000)
        viewHolder.itemView.findViewById<TextView>(R.id.time_right).text = day_format.format(date)

        val uri = user.image_url
        val targetImageView = viewHolder.itemView.imageView_right
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.right_row
    }

}

class Datatime(val datatime: Long): Item<GroupieViewHolder>(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val day_format = SimpleDateFormat("dd.MM.yy")
        val date = java.util.Date(datatime * 1000)
        viewHolder.itemView.findViewById<TextView>(R.id.datatime).text = day_format.format(date)
    }

    override fun getLayout(): Int {
        return R.layout.datatime
    }

}