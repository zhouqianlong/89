package com.tk.ch4gas.db;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager extends SDSQLiteOpenHelper{

	public static final String DATABASE_NAME="GAS_CH4.db";
	public static final int VERSION=1;
	private static final String TAG = "GAS_CH4";
	public static final String GAS_CALIBRATION = "GAS_CALIBRATION";//����У׼��
	public DBManager(Context context) {
		super(context,DATABASE_NAME, null, VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "�������ݿ⣺"+DATABASE_NAME);
		
		Log.i(TAG, "��������У׼��"+GAS_CALIBRATION);
		createGAS_CALIBRATION(db);

	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	private void createGAS_CALIBRATION(SQLiteDatabase db) {
		String sql;
		sql="CREATE TABLE "+GAS_CALIBRATION+"(" +
				"ID integer primary key autoincrement not null," +//ID
				"GAS_ZERO text(40)," + //��ǰ���
				"GAS_ZERO_ text(40)," + //���ú����
				"GAS_DATE text(40)," +//��ǰ����
				"GAS_DATE_ text(40)," +//���ú����
				"GAS_ALARM text(40)" + //����
				")";
		db.execSQL(sql);
		
		db.execSQL("insert into "+GAS_CALIBRATION+"(GAS_ZERO,GAS_ZERO_,GAS_DATE,GAS_DATE_,GAS_ALARM) values('0','0','0','0','100')");
	}

	/**
	 * �ж�ĳ���Ƿ����
	 * @param tabName ����
	 * @return
	 */
	public boolean tabIsExist(String tabName,SQLiteDatabase db){
		boolean result = false;
		if(tabName == null){
			return false;
		}
		Cursor cursor = null;
		try {
			String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"+tabName.trim()+"'" ;
			cursor = db.rawQuery(sql, null);
			if(cursor.moveToNext()){
				int count = cursor.getInt(0);
				if(count>0){
					result = true;
				}
			}
		} catch (Exception e) {
		}                
		return result;
	}
}
