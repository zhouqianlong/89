package com.ramy.minervue.dao;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ramy.minervue.db.PubDBHelper;

public class PubDao {
	public static String TAG="PubDao";
	private final static String table="pub";
	private final static String table2="workpub";
	private PubDBHelper pubHelper;
	private Context context;
	public PubDao(Context context){
		this.context=context;
		pubHelper=PubDBHelper.getInstance(context);
	}
	/**
	 * �����ݿ��еõ����е�����
	 * @return �������ݿ��е���������
	 */
	public HashSet<File> getAllFiles(){
		HashSet<File> files=null;
		SQLiteDatabase db=pubHelper.getWritableDatabase();
		if(db.isOpen()){
			files=new HashSet<File>();
			Cursor cursor=db.query(table,new String[]{"path"}, null, null, null,null, null);
			while (cursor.moveToNext()) {
				String path=cursor.getString(cursor.getColumnIndex("path"));
				File file=new File(path);
				files.add(file);
			}
			cursor.close();
			db.close();
		}
		return files;
	}
	public void delete(String path){
		SQLiteDatabase db=pubHelper.getWritableDatabase();
		if(db.isOpen()){
			db.delete(table, "path=?",new String[]{path});
			db.close();
		}
		
	}
	public boolean isExist(String path){
		File file=new File(path);
		HashSet<File> files=getAllFiles();
		return files.contains(file);
	}
	public void add(String path){
		SQLiteDatabase db=pubHelper.getWritableDatabase();
		if(db.isOpen()){
			ContentValues values=new ContentValues();
			values.put("path", path);
			db.insert(table,null, values);
			db.close();
		}
	}
	
	/**
	 * �����ݿ��еõ����е�����
	 * @return �������ݿ��е���������
	 */
	public HashSet<File> getWorkAllFiles(){
		HashSet<File> files=null;
		SQLiteDatabase db=pubHelper.getWritableDatabase();
		if(db.isOpen()){
			files=new HashSet<File>();
			Cursor cursor=db.query(table2,new String[]{"path"}, null, null, null,null, null);
			while (cursor.moveToNext()) {
				String path=cursor.getString(cursor.getColumnIndex("path"));
				File file=new File(path);
				files.add(file);
			}
			cursor.close();
			db.close();
		}
		return files;
	}
	public void deleteWork(String path){
		SQLiteDatabase db=pubHelper.getWritableDatabase();
		if(db.isOpen()){
			db.delete(table2, "path=?",new String[]{path});
			db.close();
		}
		
	}
	public boolean isExistWork(String path){
		File file=new File(path);
		HashSet<File> files=getWorkAllFiles();
		return files.contains(file);
	}
	public void addWork(String path){
		SQLiteDatabase db=pubHelper.getWritableDatabase();
		if(db.isOpen()){
			ContentValues values=new ContentValues();
			values.put("path", path);
			db.insert(table2,null, values);
			db.close();
		}
	}
	
}
