package com.example.better.better1933;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.better.better1933.Infrastructure.EtaJsonReader;
import com.example.better.better1933.Infrastructure.LocalDataDBReader;
import com.example.better.better1933.Model.BkmarkEtaNode;

/**
 * Created by Chris on 3/30/2018.
 */

public class NotifyService extends Service implements EtaJsonReader.ResultUpdate{
	private BkmarkEtaNode[] notifyList;
	private RemoteViews[] notificationViews;
	private Notification[] notifications;
	@Override
	public void onCreate(){
		LocalDataDBReader localDataDBReader = new LocalDataDBReader(getApplicationContext());
		notifyList = localDataDBReader.getNotifications();
		localDataDBReader.close();
		if(notifyList.length==0){
			Log.d("NotifyService","notifyListEmpty");
			stopSelf();
			return;
		}
		int iLimit = 0;
		if (notifyList.length > 2) {
			iLimit = (notifyList.length - 1) / 2;
		}
		notifications = new Notification[iLimit + 1];
		notificationViews = new RemoteViews[iLimit + 1];
		
		for (int i = iLimit; i >= 0; i--) {
			notificationViews[i] = new RemoteViews(
					getApplicationContext().getPackageName(),
					R.layout.notify_tile
			);
			int nodeCount = ((i == iLimit)&&(notifyList.length%2>0)) ? 1 : 2;
			notificationViews[i].setTextViewText(R.id.left_row_route, notifyList[i * 2].route);
			notificationViews[i].setTextViewText(R.id.left_row_bound, notifyList[i * 2].bound);
			notificationViews[i].setTextViewText(R.id.left_row_stop, notifyList[i * 2].stop);
			if (nodeCount > 1) {
				notificationViews[i].setViewVisibility(R.id.right_layout,View.VISIBLE);
				notificationViews[i].setTextViewText(R.id.right_row_route, notifyList[i * 2+1].route);
				notificationViews[i].setTextViewText(R.id.right_row_bound, notifyList[i * 2+1].bound);
				notificationViews[i].setTextViewText(R.id.right_row_stop, notifyList[i * 2+1].stop);
			}
			
			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_directions_bus_black_24dp)
					.setContent(notificationViews[i])
					.setPriority(NotificationCompat.PRIORITY_DEFAULT)
					.setOngoing(true)
					.setOnlyAlertOnce(true);
			notifications[i] = notificationBuilder.build();
			if (i == 0) {
				startForeground(1, notifications[i]);
			} else {
				NotificationManagerCompat.from(this).notify(i + 1, notifications[i]);
			}
			
		}
		for(int i = 0; i<notifyList.length;i++){
			if(notifyList[i].etaJsonReader==null){
				notifyList[i].etaJsonReader = new EtaJsonReader(notifyList[i],i,NotifyService.this,10000);
			}
			AsyncTaskCompat.executeParallel(notifyList[i].etaJsonReader);
		}
	}
	public void update(int id, String[] time, String[] rawTime){
		boolean alarm = id>=notifyList.length;
		int targetNotify;
		if (alarm) {
		targetNotify= id-notifyList.length+1;
		}else{
			//notification
			targetNotify =  id/2;
			boolean left = id%2==0;
			RemoteViews targetView = notificationViews[targetNotify];
			if(left){
				targetView.setTextViewText(R.id.left_time,time[0]);
				targetView.setTextViewText(R.id.left_rawTime,rawTime[0]);
			}else{//right
				targetView.setTextViewText(R.id.right_time,time[0]);
				targetView.setTextViewText(R.id.right_rawTime,rawTime[0]);
			}
		}
		NotificationManagerCompat.from(this).notify(targetNotify + 1, notifications[targetNotify]);
	}
	
	public void onReaderStop(int id){
		Log.e("NotifyService","ReaderStop: "+id);
		if(id<notifyList.length) {
			if (notifyList[id].etaJsonReader != null) {
				notifyList[id].etaJsonReader.cancel(true);
			}
			notifyList[id].etaJsonReader = new EtaJsonReader(notifyList[id],id,NotifyService.this,10000);
			AsyncTaskCompat.executeParallel(notifyList[id].etaJsonReader);
			
		}else{//alarm
		
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onDestroy (){
		super.onDestroy();
		for(BkmarkEtaNode node: notifyList){
			if(node.etaJsonReader!=null){
				node.etaJsonReader.cancel(true);
			}
		}
		NotificationManagerCompat.from(this).cancelAll();
		stopForeground(true);
		Log.d("NotifyService","Destroy");
	}
}
