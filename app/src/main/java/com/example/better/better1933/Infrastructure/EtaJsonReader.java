package com.example.better.better1933.Infrastructure;

import android.os.AsyncTask;
import android.util.Log;

import com.example.better.better1933.Model.DBStopInfoNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Scanner;

public class EtaJsonReader extends AsyncTask<Void, String[],Void> {
	public interface ResultUpdate{
		void update(int id, String[] time, String[] rawTime);
		void onReaderStop(int id);
	}
	private final ResultUpdate updateTarget;
    private final DBStopInfoNode node;
    private final int id;
    private final int updateInterval;
    private static JSONObject readJsonFromUrl(String url,int timeout) throws IOException,JSONException {
	    URLConnection conn = new URL(url).openConnection();
	    conn.setConnectTimeout(timeout);
	    conn.setReadTimeout(timeout + 500);
	    conn.setUseCaches(false);
	    System.setProperty("http.keepAlive", "false");
	    conn.connect();
	    Scanner s = new Scanner(conn.getInputStream());
	    StringBuilder result = new StringBuilder();
	    while (s.hasNext()) {
		    result.append(s.next());
    }
	    return new JSONObject(result.toString());
    }
	public EtaJsonReader(DBStopInfoNode node, int id, ResultUpdate updateTarget,int updateInterval){
		this.updateTarget = updateTarget;
		this.node = node;
		this.id = id;
		this.updateInterval = updateInterval;
	}
    @Override
    protected Void doInBackground(Void... param) {
        JSONObject rawJSON;
        JSONArray json;
	    String[] rawTime;
		int trial = 0;
		try{
		while (updateInterval>0||trial<20) {
			if(isCancelled()){
				Log.d("EtaJsonReader","Cancelled");
				return null;
			}
			try {
				rawJSON = readJsonFromUrl("http://etav3.kmb.hk/?action=geteta&lang=tc&route=" + node.route + "&bound=" + Integer.toString(node.bound_seq) + "&stop_seq=" + Integer.toString(node.stop_seq) + "&serviceType=" + node.serviceType,trial * 50);
				json = rawJSON.getJSONArray("response");
				rawTime = new String[json.length()];
				for (int i = 0; i < json.length(); i++) {
					rawTime[i] = json.getJSONObject(i).get("t").toString();
				}
				if(rawTime[0].contains("--:--")){
					throw new SocketTimeoutException();
				}
				publishProgress(rawTime);
				trial = 0;
				if(updateInterval==0){
					return null;
				}
				try {
					Thread.sleep(updateInterval);
				} catch (InterruptedException ex) {
					return null;
				}
			} catch ( JSONException | IOException  NETEx) {
				Log.d("EtaJsonReader", "ConnectionTimeout(" + trial + ")");
				if(trial<20) {
					trial++;
				}
				try {
					for(int i = 0; i<trial;i++) {
						Thread.sleep( 50);
					}
				} catch (InterruptedException ex) {
					//ex.printStackTrace();
					return null;
				}
			}catch (NullPointerException NPEx){
				return null;
			}
		}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
        return null;
    }
    @Override
    protected void onProgressUpdate(String[]... resultTime){
    	if(updateTarget == null){
    		return;
	    }
	    if(isCancelled()||resultTime==null||resultTime[0]==null){
		    Log.d("EtaJsonReader","noResult");
		    updateTarget.onReaderStop(id);
		    return;
	    }
	    //Log.d("EtaJsonReader","onProgressUpdate");
	    String[] time = new String[3];
    	String[] rawTime = new String[3];
	    for (int i = 0; i<3;i++) {
	    	if(i>=resultTime[0].length){
	    		time[i] = "--";
	    		rawTime[i] = "--:--";
	    		continue;
		    }
		    String nowTime = Integer.toString(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + Integer.toString(Calendar.getInstance().get(Calendar.MINUTE));
		    if (!Character.isDigit(resultTime[0][i].charAt(0))) {
		    	if(resultTime[0][i].contains("受阻@")){
				    time[i] = "受阻";
				    String blockingMsg = resultTime[0][i].substring(resultTime[0][i].indexOf("@"));
				    rawTime[i] = blockingMsg.length()>5?blockingMsg.substring(0,5):blockingMsg;
			    }else if(resultTime[0][i].contains("尾班車已過")){
		    		time[i] = "--";
				    rawTime[i] = "尾班已過";
			    }else if(resultTime[0][i].contains("非九巴時段")){
				    time[i] = "--";
				    rawTime[i] = "非九巴時段";
			    }else if(resultTime[0][i].contains("此路線只於深宵")){
				    time[i] = "--";
				    rawTime[i] = "深宵路線";
			    }else{
				    time[i] = "--";
				    rawTime[i] = resultTime[0][i];
			    }
		    } else {
			    SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
			    long remainingTime;
			    try {
				    remainingTime = (format.parse(resultTime[0][i]).getTime() - format.parse(nowTime).getTime()) / 1000 / 60;
				    if (remainingTime < -1000) {
					    remainingTime += 1440;
				    }
				    time[i]=String.valueOf(remainingTime);
			    } catch (ParseException e) {
				    Log.d("debug", e.toString());
			    }
			    rawTime[i] = resultTime[0][i];
			    if(rawTime[i].contains("尾班車")){
				    rawTime[i] = rawTime[i].substring(0,7);
			    }else if( rawTime[i].contains("預定班次")){
				    rawTime[i] = rawTime[i].substring(0,7);
			    }
		    }
		    
	    }
	    updateTarget.update(id,time,rawTime);
	    
    }
    @Override
	protected void onPostExecute(Void result){
	    if(updateTarget == null){
		    return;
	    }
    	updateTarget.onReaderStop(id);
    }
}


