package com.ramy.minervue.dao;

import com.ramy.minervue.db.AddressDBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AddressDao {
	private Context context;
	private AddressDBHelper addressDBHelper;

	public AddressDao(Context context) {
		this.context = context;
		addressDBHelper = AddressDBHelper.getInstance(context);
	}

	public Cursor getAllDatas(String address) {
		
		SQLiteDatabase db = addressDBHelper.getWritableDatabase();
		return db.query("address", null, "address like '%"+address+"%'",null, null, null, null);
	}

	public void addData(String address) {
		SQLiteDatabase db = addressDBHelper.getWritableDatabase();
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put("address", address);
			db.insert("address", null, values);
			db.close();
		}

	}

}
