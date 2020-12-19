package com.idn.adniinstagramclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.idn.adniinstagramclone.R
import com.idn.adniinstagramclone.fragments.ProfileFragment
import com.idn.adniinstagramclone.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private var mContext: Context, private val mUser: List<User>,
                  private var isFragment: Boolean = false): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //memanggil layout user_item_layout.xml
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]
        holder.userNametxtView.text = user.getUsername()
        holder.fullNametxtView.text = user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_person)
            .into(holder.userProfileImage)
//        Picasso.get().load(R.drawable.ic_person).placeholder(R.drawable.ic_person).into(holder.userProfileImage)

        //method untuk mengetahui status user
        checkFollowingStatus(user.getUID(), holder.followButton)

        //Intent ke FragmentUser
        holder.itemView.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", user.getUID())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.followButton.setOnClickListener {
            if (holder.followButton.text.toString() == "Follow") {
                firebaseUser?.uid.let { it ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it.toString())
                        .child("Following").child(user.getUID())
                        .setValue(true)

                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let {
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it.toString())
                                        .setValue(true)
                                        .addOnCompleteListener {
                                            it
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            } else {
                firebaseUser!!.uid.let {
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it.toString())
                        .child("Following").child(user.getUID())
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                            }
                        }
                }
            }
        }


    }

    private fun checkFollowingStatus(uid: String, followButton: Button) {

        val followingRef = firebaseUser?.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.child(uid).exists()) {
                    followButton.text = "Following"
                } else {
                    followButton.text = "Follow"
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }



    class ViewHolder(@NonNull itemView: View):
        RecyclerView.ViewHolder(itemView){

        var userNametxtView: TextView = itemView.findViewById(R.id.user_name_search)
        var fullNametxtView: TextView = itemView.findViewById(R.id.user_fullname_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_search)
        var followButton: Button = itemView.findViewById(R.id.follow_btnsearch)

    }
}