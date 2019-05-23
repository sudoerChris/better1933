package com.example.better.better1933.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction()==null){
            Log.d("NotificationActionReceiver","null intent");
            return;
        }
        Log.d("NotificationActionReceiver","Action: "+intent.getAction());
        if(intent.getAction().equals("close")) {
            //NotifyService.this.stopSelf();
            context.stopService(new Intent(context, NotifyService.class));
        }
    }
}