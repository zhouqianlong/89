package com.ramy.minervue.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PubDBHelper extends SQLiteOpenHelper {
	//数据库名
	private final static String NAME="pub.db";
	//数据库的版本
	private final static int VERSION=1;
	
	private final String sql="CREATE TABLE pub(_id INTEGER PRIMARY KEY AUTOINCREMENT,path TEXT)";
	private final String sql2="CREATE TABLE workpub(_id INTEGER PRIMARY KEY AUTOINCREMENT,path TEXT)";
	
	private  static PubDBHelper mInstance; 
	public static synchronized PubDBHelper getInstance(Context context){
		if(mInstance==null){
			mInstance=new PubDBHelper(context, NAME, null, VERSION);
		}
		return mInstance;
	}
	private PubDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(sql);
		db.execSQL(sql2);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
