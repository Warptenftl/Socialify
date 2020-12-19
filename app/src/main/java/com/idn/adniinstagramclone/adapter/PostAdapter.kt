package com.idn.adniinstagramclone.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.idn.adniinstagramclone.CommentActivity
import com.idn.adniinstagramclone.MainActivity
import com.idn.adniinstagramclone.R
import com.idn.adniinstagramclone.model.Post
import com.idn.adniinstagramclone.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext: Context, private val mPost: List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return mPost.size
    }

    override fun onBindViewHolder(holder: PostAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostImage()).into(holder.postImage)

        if (post.getDescription().equals("")){
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.setText(post.getDescription())
        }

        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())

        //method untuk setting icon
        isLikes(post.getPostId(), holder.likeButton)

        //method untuk melihat beberapa user yang like
        numberOfLikes(holder.likes, post.getPostId())

        //method untuk menampilkan total user comment
        getTotalComment(holder.comments, post.getPostId())


        holder.likeButton.setOnClickListener {
            if (holder.likeButton.tag == "Like") {
                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(post.getPostId()).child(firebaseUser!!.uid)
                    .setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(post.getPostId()).child(firebaseUser!!.uid)
                    .removeValue()

                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)
            }
        }

        holder.commentButton.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java)
            intentComment.putExtra("postId", post.getPostId())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)
        }

        holder.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java)
            intentComment.putExtra("postId", post.getPostId())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)
        }

    }

    private fun getTotalComment(comments: TextView, postId: String) {

        val commentRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)

        commentRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    comments.text = "view all " + snapshot.childrenCount.toString() + " comments"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun numberOfLikes(likes: TextView, postId: String) {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    likes.text = p0.childrenCount.toString() + " likes"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun isLikes(postId: String, likeButton: ImageView) {

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(firebaseUser!!.uid).exists()){
                    likeButton.setImageResource(R.drawable.ic_favorite_clicked)
                    likeButton.tag = "Liked"
                }
                else{
                    likeButton.setImageResource(R.drawable.ic_favorite)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

    }


    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_post)
        var postImage: ImageView = itemView.findViewById(R.id.post_image_home)
        var likeButton: ImageView = itemView.findViewById(R.id.post_image_like_btn)
        var commentButton: ImageView = itemView.findViewById(R.id.post_image_comment_btn)
        var saveButton: ImageView = itemView.findViewById(R.id.post_save_btn)
        var userName: TextView = itemView.findViewById(R.id.post_user_name)
        var likes: TextView = itemView.findViewById(R.id.post_likes)
        var publisher: TextView = itemView.findViewById(R.id.post_publisher)
        var description: TextView = itemView.findViewById(R.id.post_description)
        var comments: TextView = itemView.findViewById(R.id.post_comments)

    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisher1: String) {

        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisher1)
        usersRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user?.getImage()).into(profileImage)
                    userName.text = user?.getUsername()
                    publisher.text = user?.getFullname()

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }
}