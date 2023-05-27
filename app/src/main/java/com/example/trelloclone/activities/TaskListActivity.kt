package com.example.trelloclone.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.TaskListItemsAdapter
import com.example.trelloclone.databinding.ActivityTaskListBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.Card
import com.example.trelloclone.models.Task
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import java.text.FieldPosition

/**
 * This represents the window where task Lists are shown
 */
class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails : Board
    var binding : ActivityTaskListBinding? = null
    private lateinit var mBoardDocumentId: String

    /**
     * Represents the list of members assigned to a particular Board
     */
    lateinit var mAssignedMemberDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDocumentId)
    }

    /**
     * Once any member is added or any card is edited, this function is run and it updates the board.
     * So, if any new list or task is added after adding a member or card is updated, it can be shown
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE
            || requestCode == CARD_DETAIL_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this, mBoardDocumentId)
        }else{
            Log.e("No Member Added", "User opened members page but didn't add any member")
        }
    }

    /**
     * Setup top right three dot menu which opens members activity
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Called from the TaskListItemAdapter whenever any card is clicked on to open its cardDetail page
     *
     * @param taskListPosition Task list position
     * @param cardPosition Card position
     */
    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSiTION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMemberDetailList)
        startActivityForResult(intent, CARD_DETAIL_REQUEST_CODE)
    }

    /**
     * Makes the member button on three dots open members activity when clicked
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_members -> {
                val intent  = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
            R.id.action_delete_board -> {
                if(areMembersPresentInBoard()){
                    showErrorSnackBar("Members present! Kindly remove all members first before" +
                            " deleting this board!")
                }else{
                    alertDialogForDeleteBoard(mBoardDetails.name)
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun areMembersPresentInBoard() : Boolean{
        val assignedMemberList = mBoardDetails.assignedTo
        val currentUserId = getCurrentUserId()
        var areMembersPresent = false
        for(memberId in assignedMemberList){
            if(memberId != currentUserId){
                areMembersPresent = true
                return true
            }
        }
        return false
    }

    /**
     * Confirms from user if they really want to delete the board or not
     */
    private fun alertDialogForDeleteBoard(boardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(resources.getString(R.string.confirmation_message_to_delete_board, boardName))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") {
                dialog, which ->
            dialog.dismiss()
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().deleteBoard(this, mBoardDetails)

        }.setNegativeButton("No") {
                dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog : AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    /**
     * Called from firestoreClass when board deleting is successful
     */
    fun deleteBoardSuccess() {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarTaskListActivity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }
        binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * This function setups the window for a board. It setups the actionBar and calls firebase to
     * get the list of members assigned to this board and further from there, those member are
     * made visible on this board window
     *
     * The argument here is the details of a particular board we get from firebase.
     */
    fun boardDetails(board: Board){
        mBoardDetails = board
        hideProgressDialog()
        setupActionBar()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this,
        mBoardDetails.assignedTo)
    }

    /**
     * called when addition of a list item in a baord is successful to update all boards being
     * displayed so new list items are visible
     */
    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails.documentId)
    }

    /**
     * This function takes the new list name as parameter. Updates that list name into task list
     * of current board and updates that whole board in the firebase
     */
    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FirestoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    /**
     * updates the task list name for the given task list in a board and after updating, calls
     * firestore to update details on the server
     */
    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    /**
     * Deletes a task list at the given position from the board
     */
    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    /**
     * This function adds a new card in CardList by creating a new card, adding that card in the list of
     * CardList and replaces that whole Task object with the previous one in the Task List.
     *
     * Once TaskList is updated, the whole of Board is updated in firebase and new values are shown
     */
    fun addCardToTaskList(position: Int, cardName: String){

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        //Creating a new card to add in the Card List
        val cardAssignedUserList: ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FirestoreClass().getCurrentUserId())
        val card = Card(cardName, FirestoreClass().getCurrentUserId(), cardAssignedUserList)

        //Adding that card in the Card List
        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        //Creating a new task object with updated Card List values but all other details same
        val task = Task(
            mBoardDetails.taskList[position].title,
        mBoardDetails.taskList[position].createdBy,
        cardsList)

        //updating the modified task object in TaskList
        mBoardDetails.taskList[position] = task
        showProgressDialog(resources.getString(R.string.please_wait ))

        //registering changes in firebase and re-updating the data displayed on the board.
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)

    }

    /**
     * assigns the "mAssignedMemberDetailList" variable with the list of board members and hide progressBar
     * It setups the Task List RecyclerVIew in the Board Details Window with its adapter by using
     * the mBoardDetail global variable.
     */
    fun boardMembersDetailsList(list : ArrayList<User>) {
        mAssignedMemberDetailList = list
        hideProgressDialog()

        //Since we want to show an "Add list" button also, we add that task in the board object we get
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)
        binding?.rvTaskList?.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTaskList?.setHasFixedSize(true)
        val adapter = TaskListItemsAdapter(this, mBoardDetails.taskList)
        binding?.rvTaskList?.adapter=adapter

    }

    //TODO Understand the code for drag and drop Step 2
    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>){
        //getting rid of "add card" card in the task list
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        mBoardDetails.taskList[taskListPosition].cards = cards
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }


    companion object {
        const val MEMBERS_REQUEST_CODE: Int = 13
        const val CARD_DETAIL_REQUEST_CODE : Int = 14
    }
}