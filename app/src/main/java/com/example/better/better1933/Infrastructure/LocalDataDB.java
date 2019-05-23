package com.example.better.better1933.Infrastructure;

import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.better.better1933.GlobalConst;
import com.example.better.better1933.Infrastructure.KMB.KMBDB;
import com.example.better.better1933.Model.AlarmNode;
import com.example.better.better1933.Model.BkmarkEtaNode;
import com.example.better.better1933.Model.DBStopInfoNode;
import com.example.better.better1933.Model.DBValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class LocalDataDB {
	private SQLiteDatabase dataBase;
	public LocalDataDB(boolean write) {
		if (dataBase == null || !dataBase.isOpen()) {
			String myPath = GlobalConst.AppDataPath+GlobalConst.DBFolder+GlobalConst.DBLocalDataName;
			if (!new File(myPath).exists()) {
				newDB();
			}
			try{
				dataBase = SQLiteDatabase.openDatabase(myPath, null, write?SQLiteDatabase.OPEN_READWRITE:SQLiteDatabase.OPEN_READONLY);
			}catch (SQLiteCantOpenDatabaseException SCDEx){
				//db error, renew db
				newDB();
				dataBase = SQLiteDatabase.openDatabase(myPath, null, write?SQLiteDatabase.OPEN_READWRITE:SQLiteDatabase.OPEN_READONLY);
			}
		}
	}
	public void close(){
		dataBase.close();
	}
	private static void newDB(){
		try {
			if(! new File(GlobalConst.AppDataPath+GlobalConst.DBFolder).exists()){
				new File(GlobalConst.AppDataPath+GlobalConst.DBFolder).mkdirs();
			}
			InputStream mInput = GlobalConst.AppAsset.open(GlobalConst.DBLocalDataName);
			String outFileName = GlobalConst.AppDataPath+GlobalConst.DBFolder + GlobalConst.DBLocalDataName;
			File dbFile = new File(outFileName);
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
	public static BkmarkEtaNode[] getBookmarks(){
		LocalDataDB localDataDB = new LocalDataDB(false);

		Cursor cursor = localDataDB.dataBase.rawQuery("select route_no, bound, service_type, stop_seq, alarm_min,notify,`index` from Bookmark order by `index` asc", null);
		ArrayList<BkmarkEtaNode> result = new ArrayList<>();
		cursor.moveToFirst();
		if(cursor.isAfterLast()){
			return new BkmarkEtaNode[0];
		}
		int routeNoIndex = cursor.getColumnIndex("route_no"),boundIndex= cursor.getColumnIndex("bound"), serviceTypeIndex= cursor.getColumnIndex("service_type"), stopSeqIndex= cursor.getColumnIndex("stop_seq"),alarmMinIndex = cursor.getColumnIndex("alarm_min"), notifyIndex = cursor.getColumnIndex("notify"),indexIndex = cursor.getColumnIndex("index");
		do{
			BkmarkEtaNode newNode = new BkmarkEtaNode(cursor.getString(routeNoIndex),cursor.getInt(boundIndex),cursor.getInt(stopSeqIndex),cursor.getString(serviceTypeIndex));
			newNode.alarmMin = cursor.getInt(alarmMinIndex);
			newNode.notify = cursor.getInt(notifyIndex)==1;
			String[] names = KMBDB.getName(newNode);
			newNode.stop = names[0];
			newNode.bound = names[1];
			newNode.index = cursor.getInt(indexIndex);
			result.add(newNode);
		}while (cursor.moveToNext());
		cursor.close();
		localDataDB.close();
		BkmarkEtaNode[] returnResult = new BkmarkEtaNode[result.size()];
		result.toArray(returnResult);
		return returnResult;
	}
	public static void deleteBookmarks(){

		LocalDataDB localDataDB = new LocalDataDB(true);
		localDataDB.dataBase.execSQL("delete from Bookmark");
		localDataDB.close();
	}
	public static void deleteBookmark(DBStopInfoNode node){

		LocalDataDB localDataDB = new LocalDataDB(true);
		localDataDB.dataBase.execSQL("delete from bookmark where route_no = ? and bound = ? and service_type = ? and stop_seq = ?",new Object[]{node.route,node.bound_seq, node.serviceType, node.stop_seq});
		localDataDB.close();
	}
	public static void addBookmark(DBStopInfoNode node){

		LocalDataDB localDataDB = new LocalDataDB(true);
		if(node.index<0) {
			Cursor cursor = localDataDB.dataBase.rawQuery("select max(`index`) from Bookmark", null);
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				node.index = cursor.getInt(0) + 1;
			}
			cursor.close();
		}
		try{
			localDataDB.dataBase.execSQL("insert or replace into Bookmark (route_no, bound, service_type, stop_seq, alarm_min,notify, `index`) values (?,?,?,?,?,?,?)", new Object[]{node.route, node.bound_seq,node.serviceType,node.stop_seq, node.alarmMin,node.notify?1:0,node.index});
		}catch (SQLiteConstraintException SQLCEx){
			Log.e("AddBookmark","Exception");
		}
		localDataDB.close();
	}
	public static void swapBookmark(int a, int b){

		LocalDataDB localDataDB = new LocalDataDB(true);
		localDataDB.dataBase.execSQL("update Bookmark set `index`=? where `index`=?",new String[]{"-999",Integer.toString(a)});
		localDataDB.dataBase.execSQL("update Bookmark set `index`=? where `index`=?",new String[]{Integer.toString(a),Integer.toString(b)});
		localDataDB.dataBase.execSQL("update Bookmark set `index`=? where `index`=?",new String[]{Integer.toString(b),"-999"});
		localDataDB.close();
	}
	public static void replaceBookmarks(DBStopInfoNode[] bookmarks){
		deleteBookmarks();
		LocalDataDB localDataDB = new LocalDataDB(true);
		int index = 0;
		for (DBStopInfoNode node : bookmarks) {
			if(node==null){
				continue;
			}
			localDataDB.dataBase.execSQL("insert into Bookmark (route_no, bound, service_type, stop_seq,alarm_min,notify, `index`) values(?,?,?,?,?,?,?)",new Object[]{node.route,node.bound_seq,node.serviceType,node.stop_seq,node.alarmMin,node.notify,index++});
		}
		localDataDB.close();
	}
	public static AlarmNode[] getAlarms(){
		LocalDataDB localDataDB = new LocalDataDB(false);
		Cursor cursor = localDataDB.dataBase.rawQuery("select route_no, bound, service_type, stop_seq, alarm_min from Bookmark where alarm_min>0 order by `index` asc", null);
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
		localDataDB.close();
		AlarmNode[] returnResult = new AlarmNode[result.size()];
		result.toArray(returnResult);
		return returnResult;
	}
	public static BkmarkEtaNode[] getNotifications(){

		LocalDataDB localDataDB = new LocalDataDB(false);
		Cursor cursor = localDataDB.dataBase.rawQuery("select route_no, bound, service_type, stop_seq from Bookmark where notify=1 order by `index` asc", null);
		ArrayList<BkmarkEtaNode> result = new ArrayList<>();
		cursor.moveToFirst();
		BkmarkEtaNode[] returnResult;
		if(cursor.isAfterLast()){
			returnResult = new BkmarkEtaNode[0];
		}
		else {
			int routeNoIndex = cursor.getColumnIndex("route_no"), boundIndex = cursor.getColumnIndex("bound"), serviceTypeIndex = cursor.getColumnIndex("service_type"), stopSeqIndex = cursor.getColumnIndex("stop_seq");
			do {
				BkmarkEtaNode newNode = new BkmarkEtaNode(cursor.getString(routeNoIndex), cursor.getInt(boundIndex), cursor.getInt(stopSeqIndex), cursor.getString(serviceTypeIndex));
				result.add(newNode);
			} while (cursor.moveToNext());
			cursor.close();
			returnResult = new BkmarkEtaNode[result.size()];
			result.toArray(returnResult);
			for (BkmarkEtaNode node : returnResult) {
				String[] name = KMBDB.getName(node);
				node.stop = name[0];
				node.bound = name[1];
			}
		}
		localDataDB.close();
		return returnResult;
	}
	public void createMasterTable(){
		dataBase.execSQL("drop table `Master`");
		dataBase.execSQL("CREATE TABLE `Master` ( `Name` TEXT NOT NULL UNIQUE, `Value` TEXT, `Type` TEXT NOT NULL, PRIMARY KEY(`Name`) )");
	}
	public static DBValue GetDBValue(String name){
		LocalDataDB localDataDB = new LocalDataDB(false);
		Cursor cursor = localDataDB.dataBase.rawQuery("select Value, Type from Master where Name = ?",new String[]{name});
		DBValue result = null;
		if(cursor.moveToFirst()){
			if(!cursor.isNull(0)){
			result = DBValue.parseFromCustomValue(cursor.getString(0),cursor.getString(1));
			}
		}

		cursor.close();
		localDataDB.close();
		return result;
	}
	public static void SetDBValue(String name, DBValue value){
		LocalDataDB localDataDB = new LocalDataDB(true);
		localDataDB.dataBase.execSQL("insert or replace into `Master` (Name, Value, Type) values (?,?,?)",new Object[]{name, value.strValue, value.type});
		localDataDB.close();
	}
}

