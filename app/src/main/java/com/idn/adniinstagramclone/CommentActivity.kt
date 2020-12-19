package com.idn.adniinstagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.idn.adniinstagramclone.adapter.CommentsAdapter
import com.idn.adniinstagramclone.model.Comment
import com.idn.adniinstagramclone.model.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    private var commentAdapter: CommentsAdapter? = null
    private var commentList: MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        val intent = intent
        postId = intent.getStringExtra("postId")
        publisherId = intent.getStringExtra("publisherId")

        firebaseUser = FirebaseAuth.getInstance().currentUser

        var recyclerView: RecyclerView? = null
        recyclerView = findViewById(R.id.recyclerview_comment)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList as ArrayList<Comment>)
        recyclerView.adapter = commentAdapter

        userInfo()
        readComments()
        getPostImageComment()

        txtpost_comments.setOnClickListener {
            if (add_comment.text.toString() == ""){

                Toast.makeText(this@CommentActivity, "Please write comment first", Toast.LENGTH_LONG).show()
            } else {
                addComment()
            }
        }

    }

    private fun readComments() {

        var commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               if (snapshot.exists()) {
                   commentList!!.clear()

                   for (snapshot in snapshot.children) {
                       val comment = snapshot.getValue(Comment::class.java)
                       commentList!!.add(comment!!)
                   }
                   commentAdapter!!.notifyDataSetChanged()
               }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun getPostImageComment() {
        var postCommentsRef = FirebaseDatabase.getInstance().reference
            .child("Posts").child(postId).child("postimage")

        postCommentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val image = snapshot.value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.ic_person)
                        .into(post_image_comment)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun addComment() {
        val commentRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId!!)

        val commentsMap = HashMap<String, Any>()
        commentsMap["comment"] = add_comment!!.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentRef.push().setValue(commentsMap)

        add_comment!!.text.clear()

    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.ic_person)
                        .into(profile_image_Comment)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}