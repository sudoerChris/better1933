package com.example.better.better1933.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.better.better1933.Infrastructure.KMB.KMBEtaReader;

/**
 * Created by Chris on 3/30/2018.
 */

public class AlarmService extends Service implements KMBEtaReader.IKMBEtaReaderUpdate {
	@Override
	public void onCreate(){
	
	}
	
	public void onKMBEtaReaderUpdate(int id, String[] time, String[] rawTime){
	
	}
	public void onKMBEtaReaderPostExecute(int id){
	
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
