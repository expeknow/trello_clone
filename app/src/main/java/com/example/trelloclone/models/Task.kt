package com.example.trelloclone.models

import android.os.Parcel
import android.os.Parcelable


/**
 * Task represents a single list of to-dos that is present in a board. A Task can have multiple
 * Cards where one Card represents a single To-Do within that Task.
 *
 * Important value that a Task Stores it an ArrayList of Cards
 */
data class Task (
    var title : String = "",
    val createdBy: String = "",
    var cards: ArrayList<Card> = ArrayList()

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(Card.CREATOR)!!
    )
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeString(createdBy)
        writeTypedList(cards)
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }
}