package com.example.trelloclone.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.adapters.BoardItemsAdapter
import com.example.trelloclone.databinding.ActivityMainBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.internal.NavigationMenuItemView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener{

    companion object {
        const val MY_PROFILE_REQUEST_CODE : Int = 11
        const val CREATE_BOARD_REQUEST_CODE : Int = 12
    }
    lateinit var adapter : BoardItemsAdapter
    private lateinit var mUserName: String

    //for notification manager
    lateinit var mSharedPreferences: SharedPreferences

    var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        //it has "this" because we are inheriting from onNavigationItemSelectedListener in parent class
        binding?.navView?.setNavigationItemSelectedListener (this)

        mSharedPreferences = this.getSharedPreferences(Constants.TRELLO_PREFERENCES,
        Context.MODE_PRIVATE)

        //tells whether the FCM token is updated in the database or not
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        //If we have updated token, load the  user data
        if(tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        //else get an updated token
        }else{
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity) {
                instanceIdResult ->
                    updateFCMToken(instanceIdResult.token)
            }
        }

        FirestoreClass().loadUserData(this, true)

        val fabCreateBoard = findViewById<FloatingActionButton>(R.id.fab_create_board)
        fabCreateBoard.setOnClickListener{
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }



    }

    private fun setupActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }

    }

    /**
     * This function just sets the recycler View's adapter with the boards that are available.
     * All the downloaded boards list need to be passed for this to work
     */
    fun populateBoardListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()
        val rvBoardsList = findViewById<RecyclerView>(R.id.rv_board_list)
        val tvNoBoardsAvaiable = findViewById<TextView>(R.id.tv_no_boards_available)

        if(boardsList.size > 0) {
            rvBoardsList.visibility = View.VISIBLE
            tvNoBoardsAvaiable.visibility = View.GONE
            rvBoardsList.layoutManager = LinearLayoutManager(this)
            rvBoardsList.setHasFixedSize(true)
            adapter = BoardItemsAdapter(this, boardsList)
            rvBoardsList.adapter = adapter

            //STEP 4: Setting OnClickListener for the whole adapter
            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }


            })


        }else{
            rvBoardsList.visibility = View.GONE
            tvNoBoardsAvaiable.visibility = View.VISIBLE
        }
    }

    /**
     * Closes of opens the drawer when burger icon is clicked on main activity
     */
    private fun toggleDrawer() {

        //Gravity start pushes the drawer back to its hiding position
        if(binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if(binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    /**
     * Taking the user object as argument, this functions updates the user details in left drawer
     * in mainActivity window. If readBoardList is true, the list of all boards created by user is
     * fetched from firestore.
     */
    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        hideProgressDialog()
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.nav_user_image))

        mUserName = user.name!!
        findViewById<TextView>(R.id.tv_username).text = user.name
//        findViewById<NavigationMenuItemView>(R.id.nav_my_profile)

        if(readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardList(this)
        }

    }

    /**
     * When an option in the left nevigation drawer is clicked, this function is called to start
     * the corresponding activity
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                //resets our shares preferences because user has logged out here
                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                //flags
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK &&
                requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }
        else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardList(this)
        }
        else{
            Log.e("Cancelled", "Profile Not Updated. User probably pressed back")
        }
    }

    /**
     * called from firestoreClass once the user registers or log in. It saves the user's
     * FCM Token in sharePreferences and then calls FireStore again to load user data
     */
    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().loadUserData(this, true)
    }

    /**
     * Updates the fcm token of user inside our firestore database
     */
    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this, userHashMap)

    }
}