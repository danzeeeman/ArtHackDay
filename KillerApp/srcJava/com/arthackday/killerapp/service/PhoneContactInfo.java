package com.arthackday.killerapp.service;

public class PhoneContactInfo {

	private String mName;
	private String mNumber;
	private int mID;

	public PhoneContactInfo() {
		mName = "";
		mNumber = "";
		mID = 0;
	}

	public void setPhoneContactID(int phoneContactID) {
		mID = phoneContactID;

	}

	public void setContactName(String contactName) {
		mName = contactName;

	}

	public void setContactNumber(String contactNumber) {
		mNumber = contactNumber;

	}

	public int getID() {
		return mID;
	}

	public String getName() {
		return mName;
	}

	public String getNumber() {
		return mNumber;
	}
}
