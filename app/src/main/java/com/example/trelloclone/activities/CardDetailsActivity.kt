package com.example.trelloclone.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.CardMemberListItemsAdapter
import com.example.trelloclone.adapters.MembersListItemAdapter
import com.example.trelloclone.databinding.ActivityCardDetailsBinding
import com.example.trelloclone.dialogs.CardMembersListDialog
import com.example.trelloclone.dialogs.LabelColorListDialog
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.*
import com.example.trelloclone.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    var binding : ActivityCardDetailsBinding? = null
    private lateinit var mBoardDetails : Board
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor = ""
    private var mSelectedDueDateMilliSeconds: Long = 0

    /**
     * Represents list of members assigned to a particular Board
     */
    private lateinit var mMembersDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        getIntentData()
        setupToolbar()

        binding?.etNameCardDetails?.setText(mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition]
            .name)

        binding?.etNameCardDetails?.setSelection(
            binding?.etNameCardDetails?.text.toString().length
        )

        binding?.btnUpdateCardDetails?.setOnClickListener {
            if(binding?.etNameCardDetails?.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
                showErrorSnackBar("Please enter a card name!")
            }
        }

        binding?.tvSelectLabelColor?.setOnClickListener {
            labelColorListDialog()
        }

        //Get the current saved label color for opened card.
        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        binding?.tvSelectMembers?.setOnClickListener {
            cardMembersListDialog()
        }


        binding?.tvSelectDueDate?.setOnClickListener {
            showDatePicker()
        }
        setupSelectedMembersList()

        mSelectedDueDateMilliSeconds = mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardPosition].dueDate

        if(mSelectedDueDateMilliSeconds > 0) {
            val simpleDateFormat = SimpleDateFormat("dd//MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            binding?.tvSelectDueDate?.text = selectedDate

        }

    }

    /**
     * Returns the list of color that user can use for his cards
     */
    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    /**
     * Sets the background color of color selector option item
     */
    internal fun setColor() {
        binding?.tvSelectLabelColor?.text = ""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    /**
     * function to trigger our color picker dialog
     */
    private fun labelColorListDialog() {
        val colorsList: ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()

    }

    /**
     * Show card member list dialog when the user clicks on itemView of member's recycler View.
     * Also updates whether a member is selected of not in database.
     */
    fun cardMembersListDialog() {
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardPosition].assignedTo

        /**
         * Only those members that are assigned to a particular board can be assigned to a card inside
         * that board so we have to check for members of board and members for that card.
         */
        //check if any assigned member is present in the list of card members or not
        if(cardAssignedMembersList.size > 0 ){
            //go through all of the members in the list of Board Assignee
            for(i in mMembersDetailList.indices){
                //go through all the assignee of the card
                for(j in cardAssignedMembersList) {
                    //if board assignee is same as card assignee
                    if(mMembersDetailList[i].id == j){
                        //that assignee's selected property is set true so in future
                        //when we run this app, the member list can show the tickMark
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        }else{
            //if no one is assigned to a particular card then is selected is set false for all
            for(i in mMembersDetailList.indices){
                mMembersDetailList[i].selected = false
            }
        }
        val listDialog = object : CardMembersListDialog(
            this,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ){
            override fun onItemSelected(user: User, action: String) {
                //when the user clicks on a member and we want to select it
                if(action == Constants.SELECT){
                    //if that user's ID is not current present in list of assignee for this card
                    if(!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
                            .contains(user.id)){
                        //add the user id in the list of assignee
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
                            .add(user.id!!)
                    }
                //if we want to deselect that member
                }else{
                    //remove the id of that member from the list of assignee for that card
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
                        .remove(user.id)

                    //Also in the list of board assignees, make that user's selected status false
                    //so next time loading that data doesn't show this user as selected.
                    for(i in mMembersDetailList.indices){
                        if(mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                //after selection of member for card is complete
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    /**
     * Show the rounded selected member RecyclerView in Card Details page. Finds all selected members
     * for a card and setups the recycler View to show that list
     */
    internal fun setupSelectedMembersList() {
        val cardAssignedMemberList = mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardPosition].assignedTo

        /**
         * List of members selected by user as Card Member.
         */
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        //go through all of the members in the list of Board Assignee
        for(i in mMembersDetailList.indices){
            //go through all the assignee of the card
            for(j in cardAssignedMemberList) {
                //if board assignee is same as card assignee
                if(mMembersDetailList[i].id == j){
                    //create a SelectedMember Object with that user's ID and Image
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id!!,
                        mMembersDetailList[i].image!!
                    )
                    //add that object in the list created for the same purpose
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        //When selected members for a given card are present
        if(selectedMembersList.size > 0){
            //Add a dummy selectedMember for our "add member" button
            selectedMembersList.add(SelectedMembers("", ""))

            //Setup the layout for recycler View
            binding?.tvSelectMembers?.visibility = View.GONE
            binding?.rvSelectedMembersList?.visibility = View.VISIBLE
            binding?.rvSelectedMembersList?.layoutManager = GridLayoutManager(
                this, 6)

            val adapter = CardMemberListItemsAdapter(this, selectedMembersList, true)
            binding?.rvSelectedMembersList?.adapter = adapter

            adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                override fun onClick() {
                    cardMembersListDialog()
                }
            })
        //When no selected members are present for the given card
        }else{
            binding?.tvSelectMembers?.visibility = View.VISIBLE
            binding?.rvSelectedMembersList?.visibility = View.GONE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        val toolbar = binding?.toolbarCardDetailsActivity
        setSupportActionBar(toolbar)
        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            supportActionBar?.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        }
        binding?.toolbarCardDetailsActivity?.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    /**
     * Hide's progress dialog, give back result_ok to calling intent and closes CardDetailsActivity
     */
    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * Get intent data when this activity is launched
     */
    private fun getIntentData() {
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSiTION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSiTION, -1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            /**
             * Represents a list of all members assigned to a particular Board
             */
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    /**
     * Creates a new card with updated name and replaces that card with previous card and updates
     * the board in firebase
     */
    private fun updateCardDetails() {
        val card = Card(
            binding?.etNameCardDetails?.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        //To update task List
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        //removing add new member button
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    /**
     * Deletes the card from list of cards and called firebase to update database
     */
    private fun deleteCard() {
        //Take current card List
        val cardList : ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        //remove card at current position
        cardList.removeAt(mCardPosition)
        //take the current TaskList from board
        val taskList : ArrayList<Task> = mBoardDetails.taskList
        /**
         * In our Task List, There is an "Add Card" button. That is also a card internally and
         * we use below line to remove that entry from the list
         */
        taskList.removeAt(taskList.size-1)
        //replace the Task at current TaskListPosition with the one where selected card is deleted
        taskList[mTaskListPosition].cards = cardList

        //Update database
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    /**
     * Confirms from user if they really want to delete the card or not
     */
    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(resources.getString(R.string.confirmation_message_to_delete_card, cardName))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") {
                dialog, which ->
            dialog.dismiss()
            deleteCard()
        }.setNegativeButton("No") {
                dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog : AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    /**
     * Shows the date picker dialog to select card due date
     */
    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day


        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                /*
                  The listener used to indicate the user has finished selecting a date.
                  Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.*/

                // Here we have appended 0 if the selected day is smaller than 10 to make it double digit value.
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                // once date is Selected, it is set to the TextView to make it visible to user.
                binding?.tvSelectDueDate?.text = selectedDate

                /**
                 * We have the selected date as string but we want to store it in firebase as
                 * Unix epoch time so we have to get the Date object from that string.
                 */

                //We know the format of the date string we have so create this object
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                // The formatter will parse the selected date in to Date object
                val theDate = sdf.parse(selectedDate)

                //Here we have get the time in milliSeconds from Date object
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show() // It is used to show the datePicker Dialog.
    }


}