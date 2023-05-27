package com.example.trelloclone.models

import android.os.Parcel
import android.os.Parcelable

/**
 * User object is used when we want to sign-in or log-in the user in the firebase or when we add a
 * member in a board. User stores details related to a particular registered user of our app
 */
// to make this class Parcelable, we add a plugin Android parcelable Code Generator
data class User (
    val id: String? = "",
    val name: String? = "" ,
    val email: String? = "",
    //image will be stored on firebase and we'll have the location of image in firebase
    val image: String? = "",
    val mobile: Long = 0,
    //A unique user token?
    val fcmToken: String? = "",
    //indicates whether a user is selected or unselected when we open card details page
    var selected: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),

    ) {
    }

    //this was changed a little but I don't think that was neccsary
    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(image)
        writeLong(mobile)
        writeString(fcmToken)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}