package com.example.shopapp.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.shopapp.activities.LoginActivity
import com.example.shopapp.activities.RegisterActivity
import com.example.shopapp.activities.UserProfileActivity
import com.example.shopapp.models.User
import com.example.shopapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.dialog_progress.*

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: RegisterActivity, userInfo: User) {

        //If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.USERS)
        //Document ID for users fields. Here the document it is the User ID.
            .document(userInfo.id)
            //Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

                //Here call a function of base activity for transferring the result to it.
                activity.userRegistrationSuccess()

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registering the user." ,
                    e
                )
            }
    }

    fun getCurrentUserID(): String {
        //An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        //A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if(currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun getUserDetails(activity: Activity) {

        //Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
           // The document id to get the Fields of user.
                .document(getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->

                    Log.i(activity.javaClass.simpleName, document.toString())

                    //Here we have received the document snapshot which is converted into the User Data model object.
                    val user = document.toObject(User:: class.java)!!

                    val sharedPreferences =
                            activity.getSharedPreferences(
                                    Constants.SHOPPAPP_PREFERENCES,
                                    Context.MODE_PRIVATE
                            )

                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    //Key: logged_in_username        (Key:Value)
                    //Value: firstName, lastName
                    editor.putString(
                            Constants.LOGGED_IN_USERNAME,
                            "${user.firstName} ${user.lastName}"
                    )
                    editor.apply()

                    // START
                    when (activity) {
                        is LoginActivity -> {
                            //Call a function of base activity for transferring the result to it.
                            activity.userLoggedInSuccess(user)

                        }
                    }
                    //END
                }
                .addOnFailureListener { e ->

                }

    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {

        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                when (activity) {
                    is UserProfileActivity -> {
                        activity.userProfileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is UserProfileActivity -> {
                        // Hide the progress dialog if there is any error. And print the error in log.
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating the user details.",
                    e
                )
            }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?) {
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            Constants.USER_PROFILE_IMAGE + System.currentTimeMillis() + "."
                      + Constants.getFileExtension(
                activity,
                imageFileURI
            )
        )

        sRef.putFile(imageFileURI!!)
            .addOnSuccessListener { taskSnapshot ->
                //The image upload is success
                Log.e("Firebase Image URI",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                //Get the downloadable url frm the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())
                        when(activity) {
                            is UserProfileActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }
                        }
                    }
            }
             .addOnFailureListener { exception ->

                when(activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, exception.message, exception)
             }
    }
}