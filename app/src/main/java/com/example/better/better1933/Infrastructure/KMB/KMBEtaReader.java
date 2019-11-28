package com.example.better.better1933.Infrastructure.KMB;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.better.better1933.Model.DBStopInfoNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Scanner;

public class KMBEtaReader extends AsyncTask<Void, String[], Void> {
	private final static String URLTemplate = "http://etav3.kmb.hk/?action=geteta&lang=tc&route=%s&bound=%s&stop_seq=%s&serviceType=%s";
	public interface IKMBEtaReaderUpdate {
		void onKMBEtaReaderUpdate(int id, String[] time, String[] rawTime);
	}

	private final IKMBEtaReaderUpdate updateInterface;
	private final DBStopInfoNode node;
	private final int id;

	private static JSONObject readJsonFromUrl(String url, int timeout) throws IOException, JSONException {
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

	public KMBEtaReader(DBStopInfoNode node, int id, @NonNull IKMBEtaReaderUpdate updateInterface) {
		this.updateInterface = updateInterface;
		this.node = node;
		this.id = id;
	}

	@Override
	protected Void doInBackground(Void... param) {
		int trial = 0;
		try {
			while (trial < 20) {
				if (isCancelled()) {
					return null;
				}
				try {
					JSONObject rawJSON = readJsonFromUrl(String.format(URLTemplate, node.route, Integer.toString(node.bound_seq), Integer.toString(node.stop_seq), node.serviceType), trial * 50);
					JSONArray json = rawJSON.getJSONArray("response");
					String[] rawTime = new String[json.length()];
					for (int i = 0; i < json.length(); i++) {
						rawTime[i] = json.getJSONObject(i).get("t").toString();
					}
					publishProgress(rawTime);
					return null;
				} catch (JSONException | IOException NETEx) {
					Log.d("KMBEtaReader", "ConnectionTimeout(" + trial + ")");
					trial++;
					Thread.sleep(50 * trial);
				}
			}
		} catch (InterruptedException IEx) {
			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String[]... resultTime) {
		super.onProgressUpdate(resultTime);
				if (isCancelled() || resultTime == null || resultTime[0] == null) {
			Log.d("KMBEtaReader", "noResult");
			return;
		}
		String[] time = new String[3];
		String[] rawTime = new String[3];
		for (int i = 0; i < 3; i++) {
			if (i >= resultTime[0].length || resultTime[0][i].length()==0) {
				time[i] = "--";
				rawTime[i] = "--:--";
				continue;
			}
			String nowTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE);
			if (!Character.isDigit(resultTime[0][i].charAt(0))) {
				if (resultTime[0][i].contains("受阻@")) {
					time[i] = "受阻";
					String blockingMsg = resultTime[0][i].substring(resultTime[0][i].indexOf("@"));
					rawTime[i] = blockingMsg.length() > 5 ? blockingMsg.substring(0, 5) : blockingMsg;
				} else if (resultTime[0][i].contains("尾班車已過")) {
					time[i] = "--";
					rawTime[i] = "尾班已過";
				} else if (resultTime[0][i].contains("非九巴時段")) {
					time[i] = "--";
					rawTime[i] = "非九巴時段";
				} else if (resultTime[0][i].contains("此路線只於深宵")) {
					time[i] = "--";
					rawTime[i] = "深宵路線";
				} else {
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
					time[i] = String.valueOf(remainingTime);
				} catch (ParseException e) {
					Log.d("debug", e.toString());
				}
				rawTime[i] = resultTime[0][i];
				if (rawTime[i].contains("尾班車")) {
					rawTime[i] = rawTime[i].substring(0, 7);
				} else if (rawTime[i].contains("預定班次")) {
					rawTime[i] = rawTime[i].substring(0, 7);
				}
			}
		}
		updateInterface.onKMBEtaReaderUpdate(id, time, rawTime);
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
	}
}


