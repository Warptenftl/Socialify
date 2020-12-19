package com.idn.adniinstagramclone.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.idn.adniinstagramclone.AccountSettingActivity
import com.idn.adniinstagramclone.R
import com.idn.adniinstagramclone.adapter.MyImagesAdapter
import com.idn.adniinstagramclone.model.Post
import com.idn.adniinstagramclone.model.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postListGrid: MutableList<Post>? = null
    var myImagesAdapter: MyImagesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var viewProfile = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none")!!
        }
        if (profileId == firebaseUser.uid) {

            view?.btn_edit_account?.text = "Edit Profile"

        } else if (profileId != firebaseUser.uid) {
            cekFollowAndFollowingButtonStatus()
        }

        viewProfile.btn_edit_account.setOnClickListener {
            startActivity(Intent(context, AccountSettingActivity::class.java))
        }

        var recyclerViewUploadImages: RecyclerView = viewProfile.findViewById(R.id.recyclerview_upload_picimage)
        recyclerViewUploadImages.setHasFixedSize(true)
        val linearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImages.layoutManager = linearLayoutManager

        postListGrid = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it, postListGrid as ArrayList<Post>) }
        recyclerViewUploadImages.adapter = myImagesAdapter


        getFollowers()
        getFollowings()
        userInfo()
        myPost()

        return viewProfile


    }

    private fun myPost() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    (postListGrid as ArrayList<Post>).clear()

                    for (snapshot in snapshot.children) {
                        val post = snapshot.getValue(Post::class.java)
                        if (post?.getPublisher().equals(profileId)) {

                            (postListGrid as ArrayList<Post>).add(post!!)

                        }
//                        postListGrid?.reverse()
                        Collections.reverse(postListGrid)
                        myImagesAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    private fun cekFollowAndFollowingButtonStatus() {

        view?.btn_edit_account?.setOnClickListener {
            val getButtonText = view?.btn_edit_account?.text.toString()

            when{
                //jika button text = Edit Profile maka intent ke AccountSettingActivity
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingActivity::class.java))

                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it.toString())
                            .child("Following").child(profileId).setValue(true)
                    }

                    firebaseUser?.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it.toString()).setValue(true)
                    }
                }

                getButtonText == "Following" -> {
                    firebaseUser?.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it.toString())
                            .child("Following").child(profileId).removeValue()
                    }

                    firebaseUser?.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it.toString()).removeValue()
                    }
                }
            }
        }


    }

    private fun getFollowers() {

        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.txt_totalFollowers?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowings() {

        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.txt_totalFollowing?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference()
            .child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)

                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.ic_person)
                        .into(view?.profile_image)
                    view?.profile_fragment_username?.text = user?.getUsername()
                    view?.txt_full_namaprofile?.text = user?.getFullname()
                    view?.txt_bio_profile?.text = user?.getBio()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()
        val pref= context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId,", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref= context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId,", firebaseUser.uid)
        pref?.apply()
    }






}