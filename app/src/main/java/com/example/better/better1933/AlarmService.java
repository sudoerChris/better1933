package com.example.better.better1933;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.better.better1933.Infrastructure.EtaJsonReader;

/**
 * Created by Chris on 3/30/2018.
 */

public class AlarmService extends Service implements EtaJsonReader.ResultUpdate{
	@Override
	public void onCreate(){
	
	}
	
	public void update(int id, String[] time, String[] rawTime){
	
	}
	public void onReaderStop(int id){
	
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onDestroy (){
		super.onDestroy();
		stopForeground(true);
		
	}
}
