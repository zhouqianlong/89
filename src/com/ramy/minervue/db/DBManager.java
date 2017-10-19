package com.ramy.minervue.db;



import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager extends SDSQLiteOpenHelper{

	public static final String DATABASE_NAME="TK_IP_LIST.db";
	public static final int VERSION=14;
	/**				
	 *  
	 * 用户表保存：用户名,昵称，ip地址
	 */
	public static final String USER_TABLE = "user";

	/**				
	 * 通话记录主表
	 * 			主表字段：callid
	 */
	public static final String CALL_TABLE= "call";
	/**				
	 * 通话记录子表
	 * 		callid 关联主表的  callid
	 */
	public static final String CALL_RECODING_TABLE= "call_history";


	public DBManager(Context context) {
		super(context,DATABASE_NAME, null, VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("TEST", "db---------------------------------------------------onCreate");
		db.execSQL("CREATE TABLE pingdaoid(id integer primary key autoincrement not null,pindao text(20))");
		db.execSQL("CREATE TABLE user(id integer primary key autoincrement not null,userName text(20) not null,likeName text(20),userIp text(20))");
		db.execSQL("CREATE TABLE user1(id integer primary key autoincrement not null,userName text(20) not null,likeName text(20),userIp text(20))");//组1
		db.execSQL("CREATE TABLE user2(id integer primary key autoincrement not null,userName text(20) not null,likeName text(20),userIp text(20))");//组2
		db.execSQL("CREATE TABLE user3(id integer primary key autoincrement not null,userName text(20) not null,likeName text(20),userIp text(20))");//组3
		db.execSQL("CREATE TABLE blue(id integer primary key autoincrement not null,userName text(20) not null,address text(20),time text(20),db text(20))");//蓝牙定位
		db.execSQL("CREATE TABLE call(userName text(20) not null,callid integer primary key autoincrement not null ,callTime text(20))");
		db.execSQL("CREATE TABLE call_history(id integer primary key autoincrement not null,callid integer not null ,userName text(20),likeName text(20),userIp text(20),callTime text(20),foreign key(callid) references call(callid))");
		db.execSQL("CREATE TABLE camera(id integer primary key autoincrement not null,cameraName text(20) not null,cameraAddress text(20),cameraPort text(20))");
		db.execSQL("CREATE TABLE question(id integer primary key autoincrement not null,questionName text(20) not null,questionContent text(20))");
		db.execSQL("CREATE TABLE liaotian(id integer primary key autoincrement not null, mac text(20),tomac text(20),content text(1000),time text(20),type text(20),fromOrTo text(20))");
		db.execSQL("CREATE TABLE users(id integer primary key autoincrement not null, username text(20), driverId text(20),wangguan text(20),yuming text(20), xinhaobaohu text(20),servername text(20))");
		db.execSQL("insert into pingdaoid (pindao) values(?)",new Object[] {"001"});
		
	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			if(!tabIsExist("camera",db)){
				db.execSQL("CREATE TABLE camera(id integer primary key autoincrement not null,cameraName text(20) not null,cameraAddress text(20),cameraPort text(20))");
				db.execSQL("insert into camera (cameraName,cameraAddress,cameraPort) values(?,?,?)", new Object[] {"默认摄像头","192.168.1.32","554"});
			}
			if(!tabIsExist("liaotian",db)){
				db.execSQL("CREATE TABLE liaotian(id integer primary key autoincrement not null, mac text(20),tomac text(20),content text(1000),time text(20),type text(20),fromOrTo text(20))");
			}
			if(!tabIsExist("pingdaoid",db)){
				db.execSQL("CREATE TABLE pingdaoid(id integer primary key autoincrement not null,pindao text(20))");
				db.execSQL("insert into pingdaoid (pindao) values(?)",new Object[] {"001"});
			}
			if(!tabIsExist("users",db)){
				db.execSQL("CREATE TABLE users(id integer primary key autoincrement not null, username text(20), driverId text(20),wangguan text(20),yuming text(20), xinhaobaohu text(20),servername text(20))");
			}
			if(!tabIsExist("question",db)){
				db.execSQL("CREATE TABLE question(id integer primary key autoincrement not null,questionName text(20) not null,fromOrTo text(20))");
			}
			if(!tabIsExist("blue",db)){
				db.execSQL("CREATE TABLE blue(id integer primary key autoincrement not null,userName text(20) not null,address text(20),time text(20),db text(20))");//蓝牙定位
			}
			
		} catch (Exception e) {}
		 
	}
	
	

	/**
	 * 判断某表是否存在
	 * @param tabName 表名
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
