package com.example.better.better1933;


import android.content.Context;
import android.content.res.AssetManager;

public class GlobalConst {
	public static void Init(Context appContext){
		if(!AppStartInit){ //Set Init Constant
			if (android.os.Build.VERSION.SDK_INT >= 4.2) {
				AppDataPath = appContext.getApplicationInfo().dataDir;
			}else{
				AppDataPath = appContext.getFilesDir().getPath() + appContext.getPackageName();
			}
			AppAsset = appContext.getAssets();
			AppStartInit = true;
		}
	}
	public static boolean AppStartInit = false;
	public static String AppDataPath;
	public static final String DBFolder = "/databases/";
	public static final String DBKmbName = "kmb.db";
	public static final String DBLocalDataName = "localdata.db";
	public static final int DB_VERSION = 1;
	public static AssetManager AppAsset;

	public class DBValueName{
		public static final String LastUpdate = "LastUpdate";
	}
	public class ChannelId{
		public static final String EtaNotify = "ETA Notification";
	}
}
