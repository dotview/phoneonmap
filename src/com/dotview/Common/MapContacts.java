package com.dotview.Common;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.content.ContextWrapper;

import com.amap.mapapi.core.AMapException;
import com.amap.mapapi.geocoder.Geocoder;
import com.dotview.MobileInfoService;

public class MapContacts {

	private static final String TAG = "MapContacts";
	private Geocoder coder;
	private Context context;

	public MapContacts(Context context_) {
		context = context_;

		coder = new Geocoder(context);
	}

	public Marker getLocatingFromPhone(final String phone,
			final String PersonName) {
		DBHelper db = new DBHelper(context);

		String addrss = db.getPhoneAddress(phone);
		// 存储
		if (addrss == "") {
			addrss = getCityByPhone(phone);
			db.InsertPhoneAddr(phone, addrss);
		}
		return getLatlon(phone, PersonName, addrss);
	}

	private Marker getLatlon(String phone, String PersonName,
			String PersonLocation) {
		try {
			// Restore preferences
			DBHelper db = new DBHelper(context);
			Marker m = db.getLocationFromAddr(PersonLocation);
			double slat = 0,slng = 0;
			if (m == null) {
				List<Address> address = new ArrayList<Address>();
				address = coder.getFromLocationName(PersonLocation, 2);
				if (address != null && address.size() > 0) {
					Address addres = address.get(0);
					slat = addres.getLatitude();
					slng = addres.getLongitude();

					m = new Marker();
					m.latitude = slat;
					m.longtitude = slng;
					
					String content = "电话：" + phone + "\r\n地址：" + PersonLocation;
					m.title = PersonName;
					m.content = content;
					
					db.InsertAddrLocation(PersonLocation, slat, slng);
				}
			}else{
				slat = m.latitude;
				slng = m.longtitude;
				Random random = new Random();
				double rInt = random.nextInt() > 0 ? 0.1 : -0.1;
				slat = (slat + Math.random() * rInt);
				double rInt2 = random.nextInt() > 0 ? 0.1 : -0.1;
				slng = (slng + Math.random() * rInt2);
				
				m.latitude = slat;
				m.longtitude = slng;
				String content = "电话：" + phone + "\r\n地址：" + PersonLocation;
				m.title = PersonName;
				m.content = content;
			}
			return m;

		} catch (AMapException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return null;
	}

	private String getCityByPhone(String phone) {
		InputStream inStream = this.getClass().getClassLoader()
				.getResourceAsStream("mobile.xml");
		String result = "";
		try {
			String sresult = MobileInfoService
					.getMobileAddress(inStream, phone);
			String[] ss = new String[3];
			ss = sresult.split(" ");
			if (ss.length >= 3) {
				result = ss[0].replace("：", ":").replace(phone + ":", "")
						+ ss[1];
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return result;
	}

	public ArrayList<User> getAllContacts(int gettype) {

		ContentResolver cr = context.getContentResolver();
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME };
		String selection = "";
		if (gettype == 1) {
			selection = ContactsContract.Contacts.TIMES_CONTACTED + " > 6";
		} else {
			selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '1'";
		}

		// String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
		// + " COLLATE LOCALIZED ASC";
		String sortOrder = ContactsContract.Contacts.LAST_TIME_CONTACTED
				+ " DESC";

		Cursor users = cr.query(uri, projection, selection, null, sortOrder);

		ArrayList<User> contacts = new ArrayList<User>();
		while (users.moveToNext()) {
			User u = new User();
			u.PhoneId = users.getInt(users
					.getColumnIndex(ContactsContract.Contacts._ID));
			u.Name = users.getString(users
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

			String homePhone = "", cellPhone = "", workPhone = "", otherPhone = "";
			Cursor contactPhones = cr.query(Phone.CONTENT_URI, null,
					Phone.CONTACT_ID + " = " + u.PhoneId, null, null);

			while (contactPhones.moveToNext()) {

				String number = contactPhones.getString(contactPhones
						.getColumnIndex(Phone.NUMBER));
				int type = contactPhones.getInt(contactPhones
						.getColumnIndex(Phone.TYPE));
				switch (type) {
				case Phone.TYPE_HOME:
					homePhone = number;
					break;
				case Phone.TYPE_MOBILE:
					cellPhone = number;
					break;
				case Phone.TYPE_WORK:
					workPhone = number;
					break;
				case Phone.TYPE_OTHER:
					otherPhone = number;
					break;
				}
			}
			u.Phone = ((cellPhone != "") ? cellPhone
					: ((homePhone != "") ? homePhone
							: ((workPhone != "") ? workPhone : otherPhone)));
			contacts.add(u);
		}

		return contacts;
	}
}
