package com.example.chat

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chatlist_row.view.*
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*

class MessageRow(val message:ChatActivity.Message): Item<GroupieViewHolder>(){

    var user_to : User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_text_view.text = message.text
        viewHolder.itemView.time_textview.text = convertLongToTime(message.datatime)
        Log.d("MessageRow",message.datatime.toString())

        val id: String ?
        if(message.id_sender == FirebaseAuth.getInstance().uid){
            id = message.id_receiver
        }else{
            id = message.id_sender
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$id")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                user_to = snapshot.getValue(User::class.java)
                viewHolder.itemView.username_text_view.text = user_to?.username

                Picasso.get().load(user_to?.image_url).into(viewHolder.itemView.imageView)
            }

        })


    }

    override fun getLayout(): Int {
        return R.layout.chatlist_row
    }

    private fun convertLongToTime(time: Long): String{
        val date = java.util.Date(time * 1000)
        val currentTimestamp = System.currentTimeMillis()

        Log.d("MessageRow",currentTimestamp.toString())

        val day_format = SimpleDateFormat("dd.MM.yy")

        val format = java.text.SimpleDateFormat("dd.MM")

        if(day_format.format(date) == day_format.format(currentTimestamp)) {
            val format = java.text.SimpleDateFormat("HH:mm")
            return format.format(date)
        }
        return format.format(date)
    }

}