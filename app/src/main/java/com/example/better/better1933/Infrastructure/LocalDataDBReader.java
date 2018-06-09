package com.example.better.better1933.Infrastructure;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.better.better1933.Model.AlarmNode;
import com.example.better.better1933.Model.BkmarkEtaNode;
import com.example.better.better1933.Model.DBStopInfoNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class LocalDataDBReader {
	private static String DB_PATH;
	private static final String DB_NAME = "localdata.db";
	private SQLiteDatabase mDataBase;
	private final Context context;
	public LocalDataDBReader(Context context) {
		this.context = context;
		DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
		if (mDataBase == null || !mDataBase.isOpen()) {
			String myPath = DB_PATH + DB_NAME;
			if (!new File(myPath).exists()) {
				newDB(context);
			}
			try{
				mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
			}catch (SQLiteCantOpenDatabaseException SCDEx){
				//db error, renew db
				newDB(context);
				mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
			}
		}
	}
	private static void newDB(Context context){
		try {
			if(! new File(DB_PATH).exists()){
				new File(DB_PATH).mkdirs();
			}
			InputStream mInput = context.getAssets().open(DB_NAME);
			String outFileName = DB_PATH + DB_NAME;
			File dbFile = new File(DB_PATH + DB_NAME);
			dbFile.createNewFile();
			OutputStream mOutput = new FileOutputStream(outFileName);
			byte[] mBuffer = new byte[1024];
			int mLength;
			while ((mLength = mInput.read(mBuffer)) > 0) {
				mOutput.write(mBuffer, 0, mLength);
			}
			mOutput.flush();
			mOutput.close();
			mInput.close();
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	public void close(){
		mDataBase.close();
	}
	public BkmarkEtaNode[] getBookmarks(){
		KmbDBReader kmbDBReader = new KmbDBReader(context);
		Cursor cursor = mDataBase.rawQuery("select route_no, bound, service_type, stop_seq, alarm_min,notify,`index` from Bookmark order by `index` asc", null);
		ArrayList<BkmarkEtaNode> result = new ArrayList<>();
		cursor.moveToFirst();
		if(cursor.isAfterLast()){
			kmbDBReader.close();
			return new BkmarkEtaNode[0];
		}
		int routeNoIndex = cursor.getColumnIndex("route_no"),boundIndex= cursor.getColumnIndex("bound"), serviceTypeIndex= cursor.getColumnIndex("service_type"), stopSeqIndex= cursor.getColumnIndex("stop_seq"),alarmMinIndex = cursor.getColumnIndex("alarm_min"), notifyIndex = cursor.getColumnIndex("notify"),indexIndex = cursor.getColumnIndex("index");
		do{
			BkmarkEtaNode newNode = new BkmarkEtaNode(cursor.getString(routeNoIndex),cursor.getInt(boundIndex),cursor.getInt(stopSeqIndex),cursor.getString(serviceTypeIndex));
			newNode.alarmMin = cursor.getInt(alarmMinIndex);
			newNode.notify = cursor.getInt(notifyIndex)==1;
			String[] names = kmbDBReader.getName(newNode);
			newNode.stop = names[0];
			newNode.bound = names[1];
			newNode.index = cursor.getInt(indexIndex);
			result.add(newNode);
		}while (cursor.moveToNext());
		cursor.close();
		BkmarkEtaNode[] returnResult = new BkmarkEtaNode[result.size()];
		result.toArray(returnResult);
		kmbDBReader.close();
		return returnResult;
	}
	public void deleteBookmarks(){
		mDataBase.execSQL("delete from Bookmark");
	}
	public void deleteBookmark(DBStopInfoNode node){
		mDataBase.execSQL("delete from bookmark where route_no = ? and bound = ? and service_type = ? and stop_seq = ?",new Object[]{node.route,node.bound_seq, node.serviceType, node.stop_seq});
	}
	public void addBookmark(DBStopInfoNode node){
		if(node.index<0) {
			Cursor cursor = mDataBase.rawQuery("select max(`index`) from Bookmark", null);
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				node.index = cursor.getInt(0) + 1;
			}
			cursor.close();
		}
		try{
			mDataBase.execSQL("insert or replace into Bookmark (route_no, bound, service_type, stop_seq, alarm_min,notify, `index`) values (?,?,?,?,?,?,?)", new Object[]{node.route, node.bound_seq,node.serviceType,node.stop_seq, node.alarmMin,node.notify?1:0,node.index});
		}catch (SQLiteConstraintException SQLCEx){
			Log.d("AddBookmark","Exception");
		}
	}
	public void swapBookmark(int a, int b){
		mDataBase.execSQL("update Bookmark set `index`=? where `index`=?",new String[]{"-999",Integer.toString(a)});
		mDataBase.execSQL("update Bookmark set `index`=? where `index`=?",new String[]{Integer.toString(a),Integer.toString(b)});
		mDataBase.execSQL("update Bookmark set `index`=? where `index`=?",new String[]{Integer.toString(b),"-999"});
	}
	public void replaceBookmarks(DBStopInfoNode[] bookmarks){
		deleteBookmarks();
		int index = 0;
		for (DBStopInfoNode node : bookmarks) {
			if(node==null){
				continue;
			}
			mDataBase.execSQL("insert into Bookmark (route_no, bound, service_type, stop_seq,alarm_min,notify, `index`) values(?,?,?,?,?,?,?)",new Object[]{node.route,node.bound_seq,node.serviceType,node.stop_seq,node.alarmMin,node.notify,index++});
		}
	}
	public AlarmNode[] getAlarms(){
		Cursor cursor = mDataBase.rawQuery("select route_no, bound, service_type, stop_seq, alarm_min from Bookmark where alarm_min>0 order by `index` asc", null);
		ArrayList<AlarmNode> result = new ArrayList<>();
		cursor.moveToFirst();
		if(cursor.isAfterLast()){
			return new AlarmNode[0];
		}
		int routeNoIndex = cursor.getColumnIndex("route_no"),boundIndex= cursor.getColumnIndex("bound"), serviceTypeIndex= cursor.getColumnIndex("service_type"), stopSeqIndex= cursor.getColumnIndex("stop_seq"), minBeforeIndex = cursor.getColumnIndex("alarm_min");
		do{
			AlarmNode newNode = new AlarmNode(cursor.getString(routeNoIndex),cursor.getInt(boundIndex),cursor.getInt(stopSeqIndex),cursor.getString(serviceTypeIndex),cursor.getInt(minBeforeIndex));
			result.add(newNode);
		}while (cursor.moveToNext());
		cursor.close();
		AlarmNode[] returnResult = new AlarmNode[result.size()];
		result.toArray(returnResult);
		return returnResult;
	}
	public BkmarkEtaNode[] getNotifications(){
		Cursor cursor = mDataBase.rawQuery("select route_no, bound, service_type, stop_seq from Bookmark where notify=1 order by `index` asc", null);
		ArrayList<BkmarkEtaNode> result = new ArrayList<>();
		cursor.moveToFirst();
		if(cursor.isAfterLast()){
			return new BkmarkEtaNode[0];
		}
		int routeNoIndex = cursor.getColumnIndex("route_no"),boundIndex= cursor.getColumnIndex("bound"), serviceTypeIndex= cursor.getColumnIndex("service_type"), stopSeqIndex= cursor.getColumnIndex("stop_seq");
		do{
			BkmarkEtaNode newNode = new BkmarkEtaNode(cursor.getString(routeNoIndex),cursor.getInt(boundIndex),cursor.getInt(stopSeqIndex),cursor.getString(serviceTypeIndex));
			result.add(newNode);
		}while (cursor.moveToNext());
		cursor.close();
		BkmarkEtaNode[] returnResult = new BkmarkEtaNode[result.size()];
		result.toArray(returnResult);
		KmbDBReader kmbDBReader = new KmbDBReader(context);
		for(BkmarkEtaNode node: returnResult){
			String[] name = kmbDBReader.getName(node);
				node.stop = name[0];
				node.bound = name[1];
		}
		kmbDBReader.close();
		return returnResult;
	}
}

