package com.tk.ch4gas.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
public class DBHelper {
	public DBManager dbManager;
	public DBHelper(Context context) {
		this.dbManager = new DBManager(context);
	}
	
	public void updateGAS_CALIBRATION1(String gasZero,String gasZero_){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		String sql = "update "+DBManager.GAS_CALIBRATION+" set GAS_ZERO=?,GAS_ZERO_=? where ID ==1";
		db.execSQL(sql, new String[]{gasZero,gasZero_});
		db.close();
	}
	public void updateGAS_CALIBRATION2(String gasDate,String gasDate_){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		String sql = "update "+DBManager.GAS_CALIBRATION+" set GAS_DATE=?,GAS_DATE_=? where ID ==1";
		db.execSQL(sql, new String[]{gasDate,gasDate_});
		db.close();
	}
	public void updateGAS_CALIBRATION3(String gasAlarm){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		String sql = "update "+DBManager.GAS_CALIBRATION+" set GAS_ALARM=? where ID ==1";
		db.execSQL(sql, new String[]{gasAlarm});
		db.close();
	}
	public float[] findGAS_CALIBRATION(){
		float [] date = new float [5];
		SQLiteDatabase db = dbManager.getReadableDatabase();
		String sql = "select * from "+DBManager.GAS_CALIBRATION+" where id==1";
		Cursor cursor = db.rawQuery(sql, new String []{});
		while (cursor.moveToFirst()) {
			date[0] = Float.valueOf(cursor.getString(cursor.getColumnIndex("GAS_ZERO")));
			date[1] = Float.valueOf(cursor.getString(cursor.getColumnIndex("GAS_DATE")));
			date[2] = Float.valueOf(cursor.getString(cursor.getColumnIndex("GAS_ALARM")));
			date[3] = Float.valueOf(cursor.getString(cursor.getColumnIndex("GAS_ZERO_")));
			date[4] = Float.valueOf(cursor.getString(cursor.getColumnIndex("GAS_DATE_")));
			break;
		}
		cursor.close();
		db.close();
		return date;
	}

 
}
