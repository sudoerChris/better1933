package com.example.better.better1933.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.better.better1933.GlobalConst;
import com.example.better.better1933.Infrastructure.AutoUpdateTimer;
import com.example.better.better1933.Infrastructure.KMB.KMBEtaReader;
import com.example.better.better1933.Infrastructure.LocalDataDB;
import com.example.better.better1933.MainActivity;
import com.example.better.better1933.Model.BkmarkEtaNode;
import com.example.better.better1933.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.app.Notification.VISIBILITY_PUBLIC;

/**
 * Created by Chris on 3/30/2018.
 */

public class NotifyService extends Service implements KMBEtaReader.IKMBEtaReaderUpdate,AutoUpdateTimer.IAutoUpdateTimerUpdate {
	private BkmarkEtaNode[] notifyList;
	private RemoteViews[] notificationViews;
	private AutoUpdateTimer autoUpdateTimer;
	public Executor EtaUpdateThreadPool;
	private NotificationManagerCompat notificationManager;
	private Notification[] notifications;
	private Notification createNotification(RemoteViews view, boolean closeButton){
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		Intent closeIntent = new Intent(this, NotificationActionReceiver.class);
		closeIntent.setAction("close");
		PendingIntent closePendingIntent = PendingIntent.getBroadcast(this, 0, closeIntent, 0);
		NotificationCompat.Action closeAction = (new NotificationCompat.Action.Builder(0,"Close",closePendingIntent)).build();
		NotificationCompat.Builder notificationBuilder;
		if(closeButton) {
			notificationBuilder = new NotificationCompat.Builder(this,GlobalConst.ChannelId.EtaNotify)
				.setSmallIcon(R.drawable.ic_directions_bus_black_24dp)
				.setCustomContentView(view)
				.setContentIntent(pendingIntent)
				.addAction(closeAction)
				.setVisibility(VISIBILITY_PUBLIC)
				.setOngoing(true)
				.setOnlyAlertOnce(true);
		}else{
			notificationBuilder = new NotificationCompat.Builder(this,GlobalConst.ChannelId.EtaNotify)
					.setSmallIcon(R.drawable.ic_directions_bus_black_24dp)
					.setCustomContentView(view)
					.setContentIntent(pendingIntent)
					.setVisibility(VISIBILITY_PUBLIC)
					.setOngoing(true)
					.setOnlyAlertOnce(true);
		}
		return notificationBuilder.build();
	}
	@Override
	public void onCreate(){
		Log.d("NotifyService","onCreate");
		GlobalConst.Init(getApplicationContext());
		notificationManager = NotificationManagerCompat.from(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(GlobalConst.ChannelId.EtaNotify, GlobalConst.ChannelId.EtaNotify, NotificationManager.IMPORTANCE_HIGH);
			channel.setVibrationPattern(new long[0]);
			channel.setSound(null, null);
			getSystemService(NotificationManager.class).createNotificationChannel(channel);
		}
		notifyList = LocalDataDB.getNotifications();

		if(notifyList.length==0){
			Log.d("NotifyService","notifyListEmpty");
			stopSelf();
			return;
		}

		int iLimit = 0;
		if (notifyList.length > 2) {
			iLimit = (notifyList.length - 1) / 2;
		}
		notificationViews = new RemoteViews[iLimit + 1];
		notifications = new Notification[iLimit + 1];
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
			notifications[i] = createNotification(notificationViews[i],i==iLimit);
				if(notificationManager!=null) {
					notificationManager.notify(i+1, notifications[i]);
				}
//			}
		}

		EtaUpdateThreadPool = Executors.newFixedThreadPool(4);
		onAutoUpdateTimerTick();
		autoUpdateTimer = new AutoUpdateTimer(this,10000,true);
		autoUpdateTimer.executeOnExecutor(Executors.newSingleThreadExecutor());
	}
	public void onKMBEtaReaderUpdate(int id, String[] time, String[] rawTime){
		Log.d("NotifyService","onKMBEtaReaderUpdate");
		boolean alarm = id>=notifyList.length;
		int targetNotify;
		if (alarm) {
			//TODO implement alarm function
		return;
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
		if(notificationManager!=null) {
			notificationManager.notify(targetNotify + 1, notifications[targetNotify]);
		}
	}
	@Override
	public void onAutoUpdateTimerTick() {
		Log.d("NotifyService","onAutoUpdateTimerTick");

		for(int i = 0; i<notifyList.length;i++){
			if(notifyList[i].KMBEtaReader !=null && !notifyList[i].KMBEtaReader.isCancelled()){
				notifyList[i].KMBEtaReader.cancel(true);
			}
				notifyList[i].KMBEtaReader = new KMBEtaReader(notifyList[i], i, NotifyService.this);
				notifyList[i].KMBEtaReader.executeOnExecutor(EtaUpdateThreadPool);

		}
	}

	@Override
	public void onAutoUpdateTimerPostExecute() {
		//restart timer
		Log.d("NotifyService","onAutoUpdateTimerPostExecute");
		if(autoUpdateTimer!=null) {
			autoUpdateTimer.cancel(true);
		}
			autoUpdateTimer = new AutoUpdateTimer(this, 10000, true);
			autoUpdateTimer.executeOnExecutor(Executors.newSingleThreadExecutor());

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onDestroy (){
		stopForeground(true);
		stopSelf();
		super.onDestroy();
		autoUpdateTimer.cancel(true);
		for(BkmarkEtaNode node: notifyList){
			if(node.KMBEtaReader !=null){
				node.KMBEtaReader.cancel(true);
			}
		}
		notificationManager.cancelAll();
		Log.d("NotifyService","Destroy");
	}
}
