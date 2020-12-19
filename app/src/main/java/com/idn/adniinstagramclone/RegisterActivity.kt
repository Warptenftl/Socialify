package com.idn.adniinstagramclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_signin_link.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            createAccount()
        }

    }

    private fun createAccount() {
        val fullName = fullname_register.text.toString()
        val userName = username_register.text.toString()
        val email = email_register.text.toString().trim()
        val password = password_register.text.toString()

        when {
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Fullname is requiered",
                Toast.LENGTH_LONG).show()

            TextUtils.isEmpty(userName) -> Toast.makeText(this, "Username is requiered",
                Toast.LENGTH_LONG).show()

            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is requiered",
                Toast.LENGTH_LONG).show()

            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is requiered",
                Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Register")
                progressDialog.setMessage("Please wait....")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            saveUserInfo(fullName, userName, email, progressDialog)
                            //method untuk menyimpan data user setelah user signUp
                        } else {
                            //setelah membuat pesan saat error selanjutnya create ProgressDialog
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()

                        }

                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String,
                             progressDialog: ProgressDialog) {

        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email.toLowerCase()
        userMap["bio"] = "Hey, I'm a student at IDN Boarding School!"
        //create default image profile
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/adniinstagramclone.appspot.com/o/Default%20Images%2Fic_profile.png?alt=media&token=d599c18e-759f-4177-ab0e-f0e3ef6bb97c"

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener {task ->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account Telah Dibuat", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }

    }
}