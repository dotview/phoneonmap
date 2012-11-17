package com.dotview.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private Context context;
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "PHONEMAP";

	// 电话地址
	private static final String DB_TABLE_PHONEADDRESS = "phone_address";
	// Columns present in DATABASE_TABLE
	public static final String PHONEADDRESS_ROWID = "p_id";
	public static final String PHONEADDRESS_PHONE = "phone";
	public static final String PHONEADDRESS_ADDRESS = "address";

	static final String PHONEADDRESS_CREATE = "create table "
			+ DB_TABLE_PHONEADDRESS + " (" + PHONEADDRESS_ROWID
			+ " integer primary key autoincrement, " + PHONEADDRESS_PHONE
			+ " text not null, " + PHONEADDRESS_ADDRESS + " text not null);";

	// 地址位置
	private static final String DB_TABLE_ADDRESSLOCATION = "address_location";

	public static final String ADDRESSLOCATION_ROWID = "a_id";
	public static final String ADDRESSLOCATION_ADDRESS = "address";
	public static final String ADDRESSLOCATION_LATITUDE = "latitude";
	public static final String ADDRESSLOCATION_LONGITUDE = "longitude";

	static final String ADDRESSLOCATION_CREATE = "create table "
			+ DB_TABLE_ADDRESSLOCATION + " (" + ADDRESSLOCATION_ROWID
			+ " integer primary key autoincrement, " + ADDRESSLOCATION_ADDRESS
			+ " text not null, " + ADDRESSLOCATION_LATITUDE
			+ " double not null, " + ADDRESSLOCATION_LONGITUDE
			+ " double not null);";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(PHONEADDRESS_CREATE);
		db.execSQL(ADDRESSLOCATION_CREATE);
	}

	public void InsertPhoneAddr(String phone, String addrss) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues initialValues = new ContentValues();

		initialValues.put(PHONEADDRESS_PHONE, phone);
		initialValues.put(PHONEADDRESS_ADDRESS, addrss);
		db.insert(DB_TABLE_PHONEADDRESS, null, initialValues);
		db.close();
	}

	public String getPhoneAddress(String Phone) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(DB_TABLE_PHONEADDRESS,
				new String[] { PHONEADDRESS_ADDRESS }, PHONEADDRESS_PHONE
						+ "=?", new String[] { Phone }, null, null, null);

		if (c.moveToFirst()) {
			return c.getString(c.getColumnIndex(PHONEADDRESS_ADDRESS));
		}
		c.close();
		return "";
	}

	public Map<String, String> getAllAddress() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(DB_TABLE_PHONEADDRESS, new String[] {
				PHONEADDRESS_PHONE, PHONEADDRESS_ADDRESS }, null, null, null,
				null, null);

		Map<String, String> res = new HashMap<String, String>();
		while (c.moveToNext()) {
			String key = c.getString(c.getColumnIndex(PHONEADDRESS_PHONE));
			String value = c.getString(c.getColumnIndex(PHONEADDRESS_ADDRESS));
			res.put(key, value);
		}
		c.close();
		return res;
	}

	public void InsertAddrLocation(String addrss, double latitude,
			double longitude) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues initialValues = new ContentValues();

		initialValues.put(PHONEADDRESS_ADDRESS, addrss);
		initialValues.put(ADDRESSLOCATION_LATITUDE, latitude);
		initialValues.put(ADDRESSLOCATION_LONGITUDE, longitude);
		db.insert(DB_TABLE_ADDRESSLOCATION, null, initialValues);
		db.close();
	}

	public Marker getLocationFromAddr(String address) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(DB_TABLE_ADDRESSLOCATION, new String[] {
				ADDRESSLOCATION_LATITUDE, ADDRESSLOCATION_LONGITUDE },
				PHONEADDRESS_ADDRESS + "=?", new String[] { address }, null,
				null, null);

		if (c.moveToFirst()) {
			double lat = c
					.getDouble(c.getColumnIndex(ADDRESSLOCATION_LATITUDE));
			double lng = c.getDouble(c
					.getColumnIndex(ADDRESSLOCATION_LONGITUDE));
			Marker m = new Marker();
			m.latitude = lat;
			m.longtitude = lng;
			m.address = address;
			return m;
		}
		c.close();
		
		return null;
	}

	public ArrayList<Marker> getAllLocations() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(DB_TABLE_ADDRESSLOCATION, new String[] {
				ADDRESSLOCATION_LATITUDE, ADDRESSLOCATION_LONGITUDE,
				PHONEADDRESS_ADDRESS }, null, null, null, null, null);

		ArrayList<Marker> res = new ArrayList<Marker>();
		while (c.moveToNext()) {
			double lat = c
					.getDouble(c.getColumnIndex(ADDRESSLOCATION_LATITUDE));
			double lng = c.getDouble(c
					.getColumnIndex(ADDRESSLOCATION_LONGITUDE));
			String address = c
					.getString(c.getColumnIndex(PHONEADDRESS_ADDRESS));
			Marker m = new Marker();
			m.latitude = lat;
			m.longtitude = lng;
			m.address = address;

			res.add(m);
		}
		c.close();
		return res;
	}

	public int deleteLocations(){
		SQLiteDatabase db = this.getReadableDatabase();
		int c = db.delete(DB_TABLE_ADDRESSLOCATION, null, null);
		return c;
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
}