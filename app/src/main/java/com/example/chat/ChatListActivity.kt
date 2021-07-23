package com.example.chat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.NewMessageActivity.Companion.USER_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.sortedByDescending


class ChatListActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    val list = ArrayList<ChatActivity.Message>()
    var orderedList = ArrayList<ChatActivity.Message>()

    companion object{
        var current_user: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        supportActionBar?.title="ChatApp"

        val recycler_view_latest = findViewById<RecyclerView>(R.id.recycler_view_latest)
        recycler_view_latest.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view_latest.adapter = adapter

        adapter.setOnItemClickListener{item, view ->
            val intent = Intent(this, ChatActivity::class.java)
            //safecasting
            val row = item as MessageRow
            intent.putExtra(USER_KEY,  row.user_to)
            startActivity(intent)
        }

        getLatestMessages()
        getCurrentUser()
        checkUserisLogged()
    }


    private fun updateChatList(){
        adapter.clear()
        orderedList.forEach {
            adapter.add(MessageRow(it))
        }

    }

    private fun getLatestMessages(){
        val id_sender = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference(("/latest-messages/$id_sender"))
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatActivity.Message::class.java) ?: return

                var id_persona = if(id_sender == message.id_sender) message.id_receiver else message.id_sender

                var ind = 0
                for(i in list.indices){
                    if(list[i].id_sender == id_persona || list[i].id_receiver == id_persona){
                        list[i]=message
                        ind=1
                        break
                    }
                }
                if(ind==0) list.add(message)
                orderedList = ArrayList(list.sortedByDescending { it -> it.datatime })
                updateChatList()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatActivity.Message::class.java) ?: return
                //map[snapshot.key!!] = message

                var id_persona = if(id_sender == message.id_sender) message.id_receiver else message.id_sender

                var ind = 0
                for(i in list.indices){
                    if(list[i].id_sender == id_persona || list[i].id_receiver == id_persona){
                        list[i]=message
                        ind=1
                        break
                    }
                }
                if(ind==0) list.add(message)
                orderedList = ArrayList(list.sortedByDescending { it -> it.datatime })
                updateChatList()
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

        })
    }

    private fun getCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                current_user = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkUserisLogged(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intent = Intent(this, SigninActivity::class.java)
            //clear the backstack
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out ->{
                FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/${FirebaseAuth.getInstance().uid}")
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, SigninActivity::class.java)
                //clear the backstack
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.menu_profile -> {
                val intent = Intent(this, EditProfile::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}