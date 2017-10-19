package com.ramy.minervue.db;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.tst.bean.ChatInfo;
import com.ramy.minervue.app.MainActivity;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.bean.QuestionInfo;
import com.ramy.minervue.bean.UsersBean;
import com.ramy.minervue.sync.LocalFileUtil;
import com.ramy.minervue.util.FileUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;
public class DBHelper {
	public DBManager dbManager;
	public Context context;
	
	public DBHelper(Context context) {
		this.dbManager = new DBManager(context);
		this.context = context;
	}
//	db.execSQL("insert into user (userName,likeName,userIp) values('周乾龙','小周','192.168.1.1')", new Object[]{});
//	db.execSQL("insert into call (userName,callid,callTime) values('周乾龙',100,'2015年7月29日15:06:02')", new Object[]{});
//	db.execSQL("insert into call_history (callid,userName,likeName,userIp,callTime) values(100,'小周','小周','192.168.1.1','2015年7月29日15:05:57')", new Object[]{});
	/**
	 * 添加  CallTable
	 * @param person
	 */
//	public synchronized void saveCallTable(UserBean userBean) {
//		SQLiteDatabase db = dbManager.getWritableDatabase();
//		db.execSQL("insert into call (userName,likeName,callTime) values(?,?,?)",
//				new Object[] {userBean.getUsername(),userBean.getMacAddress(),userBean.getUserIp()});
//		Log.i("MYDB", "saveCallTable:"+userBean.getUsername()+":"+userBean.getMacAddress()+":"+userBean.getCallTime());
//		db.close();
//	}
	/**
	 * 删除CallTable
	 * @param id
	 */
//	public void deleteCallTable(Integer id) {
//		SQLiteDatabase db = dbManager.getWritableDatabase();
//		db.execSQL("delete from call_history where callid="+id);//先删除外表
//		db.execSQL("delete from call where callid="+id);//删除主表
//		db.close();
//	}
	
	/**
	 * 查询CallTable所有记录
	 */
//	public List<UserBean> findCallTable() {
//		List<UserBean> userList = new ArrayList<UserBean>();
//		SQLiteDatabase db = dbManager.getReadableDatabase();
//		Cursor cursor = db.rawQuery("select * from call", new String[]{});
//		while (cursor.moveToNext()) {
//			UserBean userBean = new UserBean();
//			userBean.setUsername(cursor.getString(cursor.getColumnIndex("userName")));
//			userBean.setCallid(cursor.getInt(cursor.getColumnIndex("callid")));
//			userBean.setCallTime(cursor.getString(cursor.getColumnIndex("callTime")));
//			userList.add(userBean);
//		}
//		db.close();
//		cursor.close();
//		return userList;
//	}
	
	
	/**call_history
	 * id	callid	userName	likeName	userIp	callTime
	 *	1	100	小周	小周	192.168.1.1	2015年7月29日15:05:57
	 * 添加  HistoryTable
	 * @param person
	 */
//	public synchronized void saveHistoryTable(UserBean userBean) {
//		SQLiteDatabase db = dbManager.getWritableDatabase();
//		db.execSQL("insert into call_history (callid,userName,likeName,userIp,callTime) values(?,?,?,?,?)",
//				new Object[] {userBean.getCallid(),userBean.getUsername(),userBean.getMacAddress(),userBean.getUserIp(),userBean.getCallTime()});
//		Log.i("MYDB", "saveHistoryTable:"+userBean.getUsername()+":"+userBean.getMacAddress()+":"+userBean.getCallTime());
//		db.close();
//	}
 
	/**
	 * 查询CallTable所有记录
	 */
//	public List<UserBean> findHistoryTable() {
//		List<UserBean> userList = new ArrayList<UserBean>();
//		SQLiteDatabase db = dbManager.getReadableDatabase();
//		Cursor cursor = db.rawQuery("select * from call_history", new String[]{});
//		while (cursor.moveToNext()) {
//			UserBean userBean = new UserBean();
//			userBean.setCallid(cursor.getInt(cursor.getColumnIndex("callid")));
//			userBean.setUsername(cursor.getString(cursor.getColumnIndex("userName")));
//			userBean.setLikename(cursor.getString(cursor.getColumnIndex("likeName")));
//			userBean.setUserIp(cursor.getString(cursor.getColumnIndex("userIp")));
//			userBean.setCallTime(cursor.getString(cursor.getColumnIndex("callTime")));
//			userList.add(userBean);
//		}
//		db.close();
//		cursor.close();
//		return userList;
//		
//	}
	/**
	 * 添加 服务端备份数据
	 * @param person
	 */
//	public void saveUserTable(UserBean userBean) {
//		if(findUserTableByIdOrName(userBean).size()==0){
//			SQLiteDatabase db = dbManager.getWritableDatabase();
//			db.execSQL("insert into user (userName,likeName,userIp) values(?,?,?)",
//					new Object[] {userBean.getUsername(),userBean.getMacAddress(),userBean.getUserIp()});
//			Log.i("MYDB", "saveUserTable:"+userBean.getUsername()+":"+userBean.getMacAddress()+":"+userBean.getUserIp());
//			db.close();
//		}else{
//			Log.i("MYDB", "saveUserTable:已存在"+userBean.getUsername()+":"+userBean.getMacAddress()+":"+userBean.getUserIp());
//		}
//	}
	
	

//db.execSQL("CREATE TABLE users(id integer primary key autoincrement not null, username text(20), driverId text(20),wangguan text(20),yuming text(20), xinhaobaohu text(20),servername text(20))"); 
	/**
	 * 添加一个用户
	 * @param chatInfo
	 */
	public void AddUsers(UsersBean usersBean){
		if(usersBean!=null){
			SQLiteDatabase db = dbManager.getWritableDatabase();
			db.execSQL("insert into users (username,driverId,wangguan,yuming,xinhaobaohu,servername) values(?,?,?,?,?,?)",new Object[]{usersBean.username,usersBean.driverId,
					usersBean.wangguan,usersBean.yuming,usersBean.xinhaobaohu,usersBean.serverIp});
			db.close();
		}
	}
	
	
	public void updateUser(UsersBean usersBean){
		if(usersBean!=null){
			SQLiteDatabase db = dbManager.getWritableDatabase();
			//update user"+zu+" set userIp=? where likeName = ?",
			db.execSQL("update users set username=?,driverId=?,wangguan=?,yuming=?,xinhaobaohu=?,servername=? where id = ?",new Object[]{usersBean.username,usersBean.driverId,
					usersBean.wangguan,usersBean.yuming,usersBean.xinhaobaohu,usersBean.serverIp,usersBean.id});
			db.close();
		}
		
	}
	
	/**
	 * 删除一个用户
	 * 
	 */
	public void deleteUsers(int id){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("delete from users where id = '"+id+"'");
		db.close();
		
	}
	

	/**
	 * 查询Users
	 * @param toMAC
	 * @return
	 */
	public List<UsersBean> selectUsers(){
		List<UsersBean> list = new ArrayList<UsersBean>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from users",null);
		while (cursor.moveToNext()) {
			UsersBean info = new UsersBean();
			info.id = cursor.getInt(cursor.getColumnIndex("id"));
			info.username = cursor.getString(cursor.getColumnIndex("username"));
			info.driverId = cursor.getString(cursor.getColumnIndex("driverId"));
			info.wangguan = cursor.getString(cursor.getColumnIndex("wangguan"));
			info.yuming = cursor.getString(cursor.getColumnIndex("yuming"));
			info.xinhaobaohu = cursor.getString(cursor.getColumnIndex("xinhaobaohu"));
			info.serverIp = cursor.getString(cursor.getColumnIndex("servername"));
			list.add(info);
		}
		db.close();
		cursor.close();
		return list;
	}
	
	
	
	
	/**
	 * 添加一条聊天记录
	 * @param chatInfo
	 */
	public void AddLiaoTian(ChatInfo chatInfo){
		if(chatInfo!=null){
			SQLiteDatabase db = dbManager.getWritableDatabase();
			db.execSQL("insert into liaotian (mac,tomac,content,time,type,fromOrTo) values(?,?,?,?,?,?)",new Object[]{chatInfo.mac,chatInfo.toMac,chatInfo.content,chatInfo.time,chatInfo.type,chatInfo.fromOrTo});
			db.close();
		}
	}
	
	
	
	/**
	 * 删除与MAC为XXX的聊天记录
	 * 
	 */
	public void deleteLiaoTian(String toMac){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("delete from liaotian where tomac = '"+toMac+"'");
		db.close();
		
	}
	
	
	/**
	 * 查询与XXX聊天了多长次聊天记录
	 * @param toMAC
	 * @return
	 */
	public long selectLiaoTian(String toMAC){
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select count(*) from liaotian where tomac = '"+toMAC+"'",null);
		cursor.moveToFirst();  
		long count = cursor.getLong(0);
		db.close();
		cursor.close();
		return count;
	}
	
	/**
	 * 查询MAC为XXX的聊天记录
	 * @param toMAC
	 * @return
	 */
	public List<ChatInfo> selectLiaoTian(String toMAC,long start,long end){
		List<ChatInfo> list = new ArrayList<ChatInfo>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from liaotian where tomac = '"+toMAC+"' order by id asc,id limit "+start+","+end+"; ",null);
		while (cursor.moveToNext()) {
			ChatInfo info = new ChatInfo();
			info.content = cursor.getString(cursor.getColumnIndex("content"));
			info.time = cursor.getString(cursor.getColumnIndex("time"));
			info.type = Integer.valueOf(cursor.getString(cursor.getColumnIndex("type")));
			info.fromOrTo = Integer.valueOf(cursor.getString(cursor.getColumnIndex("fromOrTo")));
			info.mac = cursor.getString(cursor.getColumnIndex("mac"));
			info.toMac = toMAC;
			list.add(info);
		}
		db.close();
		cursor.close();
		return list;
	}
	
	/**
	 * 添加 saveUserTable
	 * @param person
	 */
	public boolean saveUserTable(UserBean userBean,int zu) {
		try {
			if(findUserTableByName(userBean,zu).size()==0&&findUserTableByMAC(userBean, zu).size()==0){
				SQLiteDatabase db = dbManager.getWritableDatabase();
				db.execSQL("insert into user"+zu+" (userName,likeName,userIp) values(?,?,?)",
						new Object[] {userBean.getUsername(),userBean.getMacAddress(),userBean.getUserIp()});
				Log.i("MYDB", "saveUserTable:"+userBean.getUsername()+":"+userBean.getMacAddress()+":"+userBean.getUserIp());
				db.close();
				MainService.getInstance().initUserDB();
				return true;
			}else{
				Log.i("MYDB", "saveUserTable:已存在"+userBean.getUsername()+":"+userBean.getMacAddress()+":"+userBean.getUserIp());
				SQLiteDatabase db = dbManager.getWritableDatabase();
				db.execSQL("update user"+zu+" set userIp=? where likeName = ?",
						new Object[] {userBean.getUserIp(),userBean.getMacAddress()});
				db.close();
				MainService.getInstance().initUserDB();
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * 删除服务端备份数据
	 * @param id
	 */
	public void deleteServerTB() {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("delete from user where id>0");
		db.close();
		MainService.getInstance().initUserDB();
	}
	
	
//		db.execSQL("CREATE TABLE blue(id integer primary key autoincrement not null,userName text(20) not null,address text(20),time text(20),db text(20))");//蓝牙定位
	
	
	public void addBlueAddress(String username, String address,String time,String ressidDb){
		
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("insert into blue (userName,address,time,db) values (?,?,?,?)",new Object[]{username,address,time,ressidDb});
		db.close();
		MainService.getInstance().initUserDB();
	}
	
	
	public void selectBlueAddress(){
//		List<ChatInfo> list = new ArrayList<ChatInfo>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from blue",null);
		String userName;
		String address;
		String time;
		String dbd;
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		while (cursor.moveToNext()) {
			userName = cursor.getString(cursor.getColumnIndex("userName"));
			address = cursor.getString(cursor.getColumnIndex("address"));
			time = cursor.getString(cursor.getColumnIndex("time"));
			dbd = cursor.getString(cursor.getColumnIndex("db"));
			try {
				obj.put("userName", userName);
				obj.put("address", address);
				obj.put("time", time);
				obj.put("db", dbd);
				array.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
//		Toast.makeText(context,array.toString(), 0).show();
		File paht = LocalFileUtil.getDir(LocalFileUtil.DIR_LOACL);
		if(cursor.getCount()>0){
			FileUtils.writeFile(paht.getPath()+"/1-"+MainService.getInstance().getUUID()+System.currentTimeMillis()+".txt", array.toString());
			deleteBlueAddress();
		}
		db.close();
		cursor.close();
	}
	
	
	
	
	public void deleteBlueAddress(){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("delete from blue where id>0");
		db.close();
		MainService.getInstance().initUserDB();
	
	}
	
	
	
	
	/**
	 * 删除saveUserTable
	 * @param id
	 */
//	public void deleteUserID(Integer id) {
//		SQLiteDatabase db = dbManager.getWritableDatabase();
//		db.execSQL("delete from user where id="+id);
//		db.close();
//		MainService.getInstance().initUserDB();
//	}
	/**
	 * 删除saveUserTable
	 * @param id
	 */
	public void deleteUserID(Integer id,int zu) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("delete from user"+zu+" where id="+id);
		db.close();
		MainService.getInstance().initUserDB();
	}
	/**
	 * 删除全部
	 * @param id
	 */
	public void deleteALL(int zu) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("delete from user"+zu+" where id>=0");
		db.close();
		MainService.getInstance().initUserDB();
	}
	/**
	 * 修改saveUserTable记录
	 */
	public void updateUserTable(UserBean userBean) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("update user set userName=?,likeName=?,userIp=?where id=?",new Object[] {userBean.getUsername(),userBean.getMacAddress(),userBean.getUserIp(),Integer.valueOf(userBean.getId())});
		db.close();
		MainService.getInstance().initUserDB();
	}
	/**
	 * 修改saveUserTable记录
	 */
	public void updateUserTableName(UserBean userBean) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("update user set userName=?where id=?",new Object[] {userBean.getUsername(),Integer.valueOf(userBean.getId())});
		db.close();
		MainService.getInstance().initUserDB();
	}
	public void insertQuestion(String questionName,String questionContent){
		if(findQuestionByName(questionName)==false){
			SQLiteDatabase db = dbManager.getWritableDatabase();
			db.execSQL("insert into  question (questionName,questionContent) values(?,?)",new Object[] {questionName,questionContent});
			db.close();
		}
	}
	public void clearQuestion(){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("delete from question where id>0",new Object[]{});
		db.close();
	}
	public void clearQuestion(int id){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("delete from question where id=?",new Object[]{id});
		db.close();
	}
	public boolean findQuestionByName(String questionName){
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from question where questionName = '"+questionName+"'",null);
		boolean isExit = false;
		if(cursor.getCount()>0){
			isExit = true;//存在
		}
		cursor.close();
		db.close();
		return isExit;
	}
	public List<QuestionInfo> findQuestion(){
		List<QuestionInfo> list = new ArrayList<QuestionInfo>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from question",null);
		while (cursor.moveToNext()) {
			QuestionInfo questionInfo = new QuestionInfo();
			questionInfo.setId(cursor.getInt(cursor.getColumnIndex("id")));
			questionInfo.setQuestionContent(cursor.getString(cursor
					.getColumnIndex("questionContent")));
			questionInfo.setQuestionName(cursor.getString(cursor
					.getColumnIndex("questionName")));
			list.add(questionInfo);
		}
		db.close();
		cursor.close();
		return list;
	}
	/**
	 * 	修改某组用户的名字
	 */
	public boolean updateUserTableName(UserBean userBean,int zu) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		try {
			db.execSQL("update user"+zu+" set userName=?where id=?",new Object[] {userBean.getUsername(),Integer.valueOf(userBean.getId())});
			db.close();
		}catch (Exception e) {
			return false;
		}finally{
			db.close();
		}
		MainService.getInstance().initUserDB();
		return true;
		
	}
	/**
	 * 修改某组ip和名字
	 */
	public void updateUserTable(UserBean userBean,int zu) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("update user"+zu+" set userName=?,likeName=?,userIp=?where id=?",new Object[] {userBean.getUsername(),userBean.getMacAddress(),userBean.getUserIp(),Integer.valueOf(userBean.getId())});
		db.close();
		MainService.getInstance().initUserDB();
	}
	/**
	 * 根据MAC地址修改IP地址
	 */
	public void updateUserIpByMac(String mac,String ip,int zu) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("update user"+zu+" set userIp=? where likeName='"+mac+"'",new Object[] {ip});
		db.close();
		MainService.getInstance().initUserDB();
	}
	/**
	 *  根据名称  + ip查询是否存在
	 */
	public List<UserBean> findUserTableByIdOrNameAndZU(UserBean user,int zu) {
		List<UserBean> userList = new ArrayList<UserBean>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user where userName = ? and userIp = ?", new String[]{user.getUsername(),user.getUserIp()});
		while (cursor.moveToNext()) {
			UserBean userBean = new UserBean();
			userBean.setId(cursor.getInt(cursor.getColumnIndex("id"))+"");
			userBean.setUsername(cursor.getString(cursor
					.getColumnIndex("userName")));
			userBean.setMacAddress(cursor.getString(cursor
					.getColumnIndex("likeName")));
			userBean.setUserIp(cursor.getString(cursor.getColumnIndex("userIp")));
			userList.add(userBean);
		}
		db.close();
		cursor.close();
		return userList;
	}
	/**
	 *  根据名称  + ip查询是否存在
	 */
	public List<UserBean> findUserTableByIdOrName(UserBean user) {
		List<UserBean> userList = new ArrayList<UserBean>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user where userName = ? and userIp = ?", new String[]{user.getUsername(),user.getUserIp()});
		while (cursor.moveToNext()) {
			UserBean userBean = new UserBean();
			userBean.setId(cursor.getInt(cursor.getColumnIndex("id"))+"");
			userBean.setUsername(cursor.getString(cursor
					.getColumnIndex("userName")));
			userBean.setMacAddress(cursor.getString(cursor
					.getColumnIndex("likeName")));
			userBean.setUserIp(cursor.getString(cursor.getColumnIndex("userIp")));
			userList.add(userBean);
		}

		db.close();
		cursor.close();
		return userList;
	}
	/**
	 *  根据名称  + ip查询是否存在
	 */
	public List<UserBean> findUserTableByIdOrName(UserBean user,int zu) {
		List<UserBean> userList = new ArrayList<UserBean>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user"+zu+" where userName = ? and userIp = ?", new String[]{user.getUsername(),user.getUserIp()});
		while (cursor.moveToNext()) {
			UserBean userBean = new UserBean();
			userBean.setId(cursor.getInt(cursor.getColumnIndex("id"))+"");
			userBean.setUsername(cursor.getString(cursor
					.getColumnIndex("userName")));
			userBean.setMacAddress(cursor.getString(cursor
					.getColumnIndex("likeName")));
			userBean.setUserIp(cursor.getString(cursor.getColumnIndex("userIp")));
			userList.add(userBean);
		}
		
		db.close();
		cursor.close();
		return userList;
	}
	/**
	 *  根据ip查询是否存在
	 */
	public List<UserBean> findUserTableByIdOrName(String ip) {
		List<UserBean> userList = new ArrayList<UserBean>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user where userIp = ?", new String[]{ip});
		while (cursor.moveToNext()) {
			UserBean userBean = new UserBean();
			userBean.setId(cursor.getInt(cursor.getColumnIndex("id"))+"");
			userBean.setUsername(cursor.getString(cursor
					.getColumnIndex("userName")));
			userBean.setMacAddress(cursor.getString(cursor
					.getColumnIndex("likeName")));
			userBean.setUserIp(cursor.getString(cursor.getColumnIndex("userIp")));
			userList.add(userBean);
		}
		
		db.close();
		cursor.close();
		return userList;
		
	}
	/**
	 *  根据ip查询是否存在
	 */
	public List<UserBean> findUserTableByIdOrName(String ip,int zu) {
		List<UserBean> userList = new ArrayList<UserBean>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user"+zu+" where userIp = ?", new String[]{ip});
		while (cursor.moveToNext()) {
			UserBean userBean = new UserBean();
			userBean.setId(cursor.getInt(cursor.getColumnIndex("id"))+"");
			userBean.setUsername(cursor.getString(cursor
					.getColumnIndex("userName")));
			userBean.setMacAddress(cursor.getString(cursor
					.getColumnIndex("likeName")));
			userBean.setUserIp(cursor.getString(cursor.getColumnIndex("userIp")));
			userList.add(userBean);
		}
		
		db.close();
		cursor.close();
		return userList;
		
	}
	/**
	 *  查询saveUserTable单个记录是否存在
	 */
	public List<UserBean> findUserTableByName(UserBean user ,int zu) {
		List<UserBean> userList = new ArrayList<UserBean>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user"+zu+" where userName = ? and userIp = ?", new String[]{user.getUsername(),user.getUserIp()});
		while (cursor.moveToNext()) {
			UserBean userBean = new UserBean();
			userBean.setId(cursor.getInt(cursor.getColumnIndex("id"))+"");
			userBean.setUsername(cursor.getString(cursor
					.getColumnIndex("userName")));
			userBean.setMacAddress(cursor.getString(cursor
					.getColumnIndex("likeName")));
			userBean.setUserIp(cursor.getString(cursor.getColumnIndex("userIp")));
			userList.add(userBean);
		}
		
		db.close();
		cursor.close();
		Log.i("TEST", "select * from user"+zu);
	//	MainService.getInstance().initUserDB();
		return userList;
		
	}
	/**
	 *  查询MAC地址是否存在
	 */
	public List<UserBean> findUserTableByMAC(UserBean user ,int zu) {
		List<UserBean> userList = new ArrayList<UserBean>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user"+zu+" where likeName = ?", new String[]{user.getMacAddress()});
		while (cursor.moveToNext()) {
			UserBean userBean = new UserBean();
			userBean.setId(cursor.getInt(cursor.getColumnIndex("id"))+"");
			userBean.setUsername(cursor.getString(cursor
					.getColumnIndex("userName")));
			userBean.setMacAddress(cursor.getString(cursor
					.getColumnIndex("likeName")));
			userBean.setUserIp(cursor.getString(cursor.getColumnIndex("userIp")));
			userList.add(userBean);
		}
		
		db.close();
		cursor.close();
		Log.i("TEST", "select * from user"+zu);
		//	MainService.getInstance().initUserDB();
		return userList;
		
	}
	
	public String findNameByMac(String mac){

		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user1 where likeName = ?", new String[]{mac});
		String username ="";
		while (cursor.moveToNext()) { 
			username = cursor.getString(cursor.getColumnIndex("userName"));
			break;
		}
		
		db.close();
		cursor.close();
		return username;
		
	
		
	}
	/**
	 *  查询用户表所有记录
	 */
	public List<UserBean> findUserTable() {
		List<UserBean> userList = new ArrayList<UserBean>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user", new String[]{});
		while (cursor.moveToNext()) {
			UserBean userBean = new UserBean();
			userBean.setId(cursor.getInt(cursor.getColumnIndex("id"))+"");
			userBean.setUsername(cursor.getString(cursor
					.getColumnIndex("userName")));
			userBean.setMacAddress(cursor.getString(cursor
					.getColumnIndex("likeName")));
			userBean.setUserIp(cursor.getString(cursor.getColumnIndex("userIp")));
			userList.add(userBean);
			//		mHolder.tv_userName.setText(list.get(position).getUsername());
//			mHolder.tv_ip.setText(list.get(position).getUserIp());
		}

		db.close();
		cursor.close();
		return userList;

	}
	/**
	 *  查询用户组表所有记录
	 */
	public List<UserBean> findUserTable(int zu) {
		List<UserBean> userList = new ArrayList<UserBean>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user"+zu, new String[]{});
		while (cursor.moveToNext()) {
			UserBean userBean = new UserBean();
			userBean.setId(cursor.getInt(cursor.getColumnIndex("id"))+"");
			userBean.setUsername(cursor.getString(cursor
					.getColumnIndex("userName")));
			userBean.setMacAddress(cursor.getString(cursor
					.getColumnIndex("likeName")));
			userBean.setUserIp(cursor.getString(cursor.getColumnIndex("userIp")));
			if(userBean.getUserIp().equals(MainService.getInstance().getIPadd())){
				continue;
			}
			userList.add(userBean);
			//		mHolder.tv_userName.setText(list.get(position).getUsername());
//			mHolder.tv_ip.setText(list.get(position).getUserIp());
		}
		
		db.close();
		cursor.close();
		Log.i("TEST", "select * from user"+zu);
//			MainService.getInstance().initUserDB();
		return userList;
		
	}

	public void execSQL(String string, Object[] objects) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL(string,objects);
		db.close();
	}
	
	
	/**
	 * 周乾龙
	 * 2016年1月18日15:01:27
	 */
	public void addRtspCamera(RtspCamera camera) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("insert into camera (cameraName,cameraAddress,cameraPort) values(?,?,?)",
				new Object[] {camera.getCameraName(),camera.getCameraAddress(),camera.getCameraPort()});
		Log.i("MYDB", "insert into camera ("+camera.getCameraName()+","+camera.getCameraAddress()+","+camera.getCameraPort()+") values(?,?,?)");
		db.close();
	}
	
	/**
	 * 周乾龙
	 * 2016年1月18日15:05:05
	 */
	public void updateRtspCamera(RtspCamera camera) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("update camera set cameraName = ?,cameraAddress = ?,cameraPort= ? where id=?",new Object[]{camera.getCameraName(),camera.getCameraAddress(),camera.getCameraPort(),camera.getId()});
		db.close();
	}
	/**
	 * 周乾龙
	 * 2016年1月18日15:10:42
	 * @return List<RtspCamera>
	 */
	public List<RtspCamera> findRtspCameraAll(){
		 List<RtspCamera> list = new ArrayList<RtspCamera>();
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from camera", new String[]{});
		while (cursor.moveToNext()) {
			RtspCamera camera = new RtspCamera();
			camera.setId(cursor.getInt(cursor.getColumnIndex("id")));
			camera.setCameraName(cursor.getString(cursor
					.getColumnIndex("cameraName")));
			camera.setCameraAddress(cursor.getString(cursor
					.getColumnIndex("cameraAddress")));
			camera.setCameraPort(cursor.getString(cursor.getColumnIndex("cameraPort")));
			list.add(camera);
		}
		db.close();
		cursor.close();
		return list;
	}
	
	/**
	 * 周乾龙
	 *  
	 */
	public void deleteRtspCamera(RtspCamera camera) {
		SQLiteDatabase db = dbManager.getWritableDatabase();
		//delete from call_history where callid=
		db.execSQL("delete from camera where id = "+camera.getId());
		Log.i("MYDB", "delete from camera where id = "+camera.getId());
		db.close();
	}
	
	public void updatePingdaoid(String id){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		db.execSQL("update pingdaoid set pindao = ? where id=1",new Object[]{id});
		db.close();
	}
	public String getPingdaoId(){
		SQLiteDatabase db = dbManager.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from pingdaoid where id =1", new String[]{});
		String pindao = "";
		while (cursor.moveToNext()) {
			pindao =  cursor.getString(cursor.getColumnIndex("pindao"));
		}
		db.close();
		cursor.close();
		return pindao;
	}
	
}
