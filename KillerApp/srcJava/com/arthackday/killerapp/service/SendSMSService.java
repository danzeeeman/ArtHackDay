package com.arthackday.killerapp.service;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

public class SendSMSService extends IntentService {

	public SendSMSService(String name) {
		super(name);

	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		Log.d("START", "Getting all Contacts");
		ArrayList<PhoneContactInfo> arrContacts = new ArrayList<PhoneContactInfo>();
		PhoneContactInfo phoneContactInfo = null;
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		Cursor cursor = getContentResolver().query(
				uri,
				new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER,
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
						ContactsContract.CommonDataKinds.Phone._ID }, null,
				null,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			String contactNumber = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			String contactName = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			int phoneContactID = cursor
					.getInt(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));

			phoneContactInfo = new PhoneContactInfo();
			phoneContactInfo.setPhoneContactID(phoneContactID);
			phoneContactInfo.setContactName(contactName);
			phoneContactInfo.setContactNumber(contactNumber);
			if (phoneContactInfo != null) {
				arrContacts.add(phoneContactInfo);
			}
			phoneContactInfo = null;
			cursor.moveToNext();
		}
		cursor.close();
		cursor = null;
		Log.d("END", "Got all Contacts");

	}

}
