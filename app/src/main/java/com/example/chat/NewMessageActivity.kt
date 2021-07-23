package com.example.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import org.w3c.dom.Text
import kotlin.math.cos

class NewMessageActivity : AppCompatActivity() {

    lateinit var adapter: GroupAdapter<GroupieViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Seleziona un utente"

        val recycler_view = findViewById<RecyclerView>(R.id.recyclerview_chatlist)
        adapter = GroupAdapter<GroupieViewHolder>()

        recycler_view.adapter = adapter
        fetchUsers("")
    }

    companion object{
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers(text: String){

        val ref = FirebaseDatabase.getInstance().getReference("/users").orderByChild("username").startAt(text).endAt(text+"\uf8ff")

        val currentUser = FirebaseAuth.getInstance().uid
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                val adapter =  GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach{
                    val user = it.getValue(User::class.java)
                    Log.d("NewMessageActivity", it.toString())
                    if(user != null && user.uid != currentUser){
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener{ item, view ->

                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatActivity::class.java)
                    //intent.putExtra(USER_KEY, userItem.user.username)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()
                }

                val recycler_view = findViewById<RecyclerView>(R.id.recyclerview_chatlist)
                recycler_view.adapter= adapter
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.new_message_menu, menu)
        var item: MenuItem? = menu?.findItem(R.id.action_search)
        var searchView: SearchView = item?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                Log.d("NewMessageActivity", newText)
                adapter.clear()
                fetchUsers(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

        })
        return super.onCreateOptionsMenu(menu)
    }
}



class UserItem(val user:User): Item<GroupieViewHolder>(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_row).text = user.username

        Picasso.get().load(user.image_url).into(viewHolder.itemView.findViewById<ImageView>(R.id.image_row))
    }

    override fun getLayout(): Int {
        return R.layout.chat_row
    }

}