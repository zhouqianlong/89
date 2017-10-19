package com.ramy.minervue.db;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
public class AddressDBHelper extends SQLiteOpenHelper {
	private final static String TABLE_NAME = "address.db";
	private final static int TABLE_VERSION = 1;
	private static AddressDBHelper mInstance;
	private final static String sql="CREATE TABLE address(_id INTEGER PRIMARY KEY AUTOINCREMENT,address TEXT NOT NULL UNIQUE)";
	public AddressDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	public static synchronized AddressDBHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new AddressDBHelper(context, TABLE_NAME, null,
					TABLE_VERSION);
		}
		return mInstance;
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(sql);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
