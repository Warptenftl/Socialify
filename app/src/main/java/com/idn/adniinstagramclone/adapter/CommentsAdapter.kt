package com.idn.adniinstagramclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.idn.adniinstagramclone.R
import com.idn.adniinstagramclone.model.Comment
import com.idn.adniinstagramclone.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter(private val mContext: Context, private val mComment: MutableList<Comment>)
    : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_item_layout, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return mComment.size
    }

    override fun onBindViewHolder(holder: CommentsAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val comment = mComment[position]

        holder.commentTV.text = comment.getComment()

        getUserInfo(holder.imageProfileComment, holder.userNameCommentTV, comment.getPublisher())
    }


    class ViewHolder(@NonNull itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        var imageProfileComment: CircleImageView = itemView.findViewById(R.id.user_profile_image_comment)
        var userNameCommentTV: TextView = itemView.findViewById(R.id.user_name_comment)
        var commentTV: TextView = itemView.findViewById(R.id.comment_comment)

    }

    private fun getUserInfo(imageProfileComment: CircleImageView, userNameCommentTV: TextView, publisher: String) {

        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(publisher)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               if (snapshot.exists()) {
                   val user = snapshot.getValue(User::class.java)

                   Picasso.get().load(user!!.getImage()).placeholder(R.drawable.ic_person)
                       .into(imageProfileComment)
                   userNameCommentTV.text = user!!.getUsername()
               }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
}