package com.example.trelloclone.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.trelloclone.activities.*
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


/**
 * This class manages all the function related to firebase data storage.
 *
 * Note: In firebase, apparently we can't make changes to elements within a field without adjusting
 * the whole field
 * Ex: In our board structure, we can't edit any Card from taskList directly. We'll have to replace
 * the taskList to make any changes. This is why we keep replacing things here and there. This is
 * How we are doing here.
 */
class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()
    /**
     * This function is called when the user sign up is successful and
     * helps in storing the data of new user in the fire store collection
     */
    fun registerUser(activity: SignupActivity, userInfo: User){
        //creating a collection named Constants.users in firestore, We can also do this on console
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener {
                Log.e("Saving Error", "Couldn't save user details in firestore database")
            }
    }

    /**
     * Creates a board in firestore with all the details passed as a board object in this function
     */
    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e("Success", "Board Created Successfully")
                Toast.makeText(activity, "Board created successfully", Toast.LENGTH_LONG ).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, it.message.toString())
            }
    }


    /**
     * Ger current user id from firebase. User IDs are automatically generated when a user signs up
     */
    fun getCurrentUserId(): String {

        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if(currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    /**
     * Gets the list of tasks assigned in a board. Invoked when a board is opened to view all tasks
     */
    fun getBoardDetails(activity: TaskListActivity, documentId: String){
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document ->
                Log.e(activity.javaClass.simpleName, document.toString())
                val board = document.toObject(Board::class.java)
                //this is to set each board object's document ID equal to the document ID we get
                //for the newly created board
                board?.documentId = document.id
                activity.boardDetails(board!!)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while fetching board from servers")
            }
    }

    /**
     * Fetches the list of all boards created by current user (uses current user's ID for that)
     * and creates a board list
     */
    fun getBoardList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            //here we are using queries that only firestore offers which real time database lacks
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val boardList: ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardListToUI(boardList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while fetching board from servers")
            }
    }

    /**
     * Delete a board
     */
    fun deleteBoard(activity: TaskListActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .delete()
            .addOnSuccessListener {
                activity.deleteBoardSuccess()
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while fetching deleting board" +
                        " from servers - "+it.message)

            }
    }

    /**
     * Adds a new list item in a board. Use's board's document ID to find the relevant board and
     * put the task in that board. Once success, addUpdateTaskListSuccess() is called to update boards
     */
    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Task List updated successfully")

                if(activity is TaskListActivity){
                    activity.addUpdateTaskListSuccess()
                }
                else if(activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()

            }.addOnFailureListener {
                exception ->
                if(activity is TaskListActivity){
                    activity.hideProgressDialog()
                }
                else if(activity is CardDetailsActivity)
                    activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName, "Error white adding task ${exception.message}")
            }
    }

    /**
     * Update user profile data into the firebase Database
     *
     * @param activity Activity
     * @param userHashMap HashMap<String, Any>
     */
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Profile data updated successfully")
                Toast.makeText(activity, "Profile updated successfully", Toast.LENGTH_LONG).show()
                when(activity){
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }

            }.addOnFailureListener {
                e ->
                when(activity){
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board")
                Toast.makeText(activity, "Error when updating profile", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * For SignInActivity: This function is called when the user signIn is successful and it
     * gets the data of logging user from our firestore USER collection
     *
     * For MainActivity: it sets the details of user in navigation drawer
     *
     * For MyProfileActivity: it gives back the user data
     *
     * Parameter: An activity from where this function is called
     *
     * Note: This function is reused for many tasks so it'd be good to analyse it well
     */
    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {

        //to access the collection, we do this
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener {document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when(activity){
                    is SigninActivity  -> {
                        activity.signInSuccess()
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                    is SignupActivity -> {
                        activity.signUpSuccess()
                    }

                }
            }.addOnFailureListener {
                e ->
                when(activity){
                    is SigninActivity  -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is SignupActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("Fetching Error", "Couldn't retrieve user details in firestore database")
            }
    }

    /**
     * Gets the list of all members that are assigned to a particular board and calls another function
     * to show the list of members as per the activity where this function is called
     */
    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val usersList : ArrayList<User> = ArrayList()
                for(i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                if(activity is MembersActivity)
                    activity.setupMembersList(usersList)
                else if(activity is TaskListActivity)
                    activity.boardMembersDetailsList(usersList)

            }.addOnFailureListener {
                exception ->

                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                    activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName,
                    exception.message.toString())
            }
    }

    /**
     * This function takes an email as argument and returns the details of user who has registered with
     * the same email address.
     */
    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore
            .collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document ->
                if(document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found!")
                }
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting user details"+it.message)
            }
    }

    /**
     * Updates the list of members who are assigned to a particular board. Used when a new member is
     * added in a board. Takes member activity, current board and new userInfo objects as parameter
    */
    fun assignedMemberToBoard(activity: MembersActivity, board: Board, userInfo: User) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(userInfo)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while assigning member to board. "+it.message)
            }
    }

}