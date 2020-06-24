package com.example.better.better1933.Infrastructure.KMB;

import android.os.AsyncTask;
import android.util.Log;

import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

public class KMBDBAutoUpdate extends AsyncTask<Void, Void, String[]> {
	public interface ResultUpdate {
		void KMBDBUpdate(String[] statements);
	}

	private ResultUpdate resultUpdate;
	private Date lastUpdate;

	public KMBDBAutoUpdate(ResultUpdate resultUpdate, Date lastUpdate) {
		this.resultUpdate = resultUpdate;
		this.lastUpdate = lastUpdate;
	}

	private static JsonObject readJsonFromUrl(String url, int timeout) throws IOException {
		URLConnection conn = new URL(url).openConnection();
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(timeout + 500);
		conn.setUseCaches(false);
		System.setProperty("http.keepAlive", "false");
		conn.connect();

		JsonParser jp = new JsonParser();
		JsonElement root = jp.parse(new InputStreamReader((InputStream) conn.getContent()));
		return root.getAsJsonObject();
	}

	private static String getUpdatePlistFromUrl(String url) throws IOException {
		Log.d("getUpdatePlistFromUrl",url);
		URLConnection urlConn = new URL(url).openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
						urlConn.getInputStream(), StandardCharsets.UTF_8));
		String inputLine;
		StringBuilder a = new StringBuilder();
		while ((inputLine = in.readLine()) != null)
			a.append(inputLine);
		in.close();
		return a.toString();
	}

	private static String getUpdateUrl(Date lastUpdate) {
		String urlLastUpdate = null;
		if (lastUpdate != null) {
			DateFormat df = new SimpleDateFormat("yyyymmddHHmmss", Locale.ENGLISH);
			urlLastUpdate = df.format(lastUpdate);
		}
		int retry = 1;
		boolean success = false;
		JsonObject jsonObj = null;
		while (!success && retry < 5) {
			try {
				jsonObj = readJsonFromUrl("https://m.kmb.hk/kmb-ws/checkupdateapp.php?version=1.6.3&lastupdate=" + urlLastUpdate, 1000);
				success = true;
			} catch (IOException ioe) {
				retry++;
			}
		}
		return jsonObj == null ? null : jsonObj.get("deltadataurl").getAsString();
	}

	private static String[] getDBUpdateStatement(Date lastUpdate) {
		int retry = 1;
		boolean success = false;
		String[] updateSatements = null;
		while (!success && retry < 5) {
			try {
				Object[] pListObj = (Object[]) PropertyListParser.parse(getUpdatePlistFromUrl(getUpdateUrl(lastUpdate)).getBytes(StandardCharsets.UTF_8)).toJavaObject();
				updateSatements = new String[pListObj.length];
				for (int i = 0; i < pListObj.length; i++) {
					updateSatements[i] = pListObj[i].toString();
				}
				success = true;
			} catch (IOException | ParseException | ParserConfigurationException | PropertyListFormatException | SAXException e) {
				retry++;
			}
		}
		return updateSatements;
	}

	@Override
	protected String[] doInBackground(Void... voids) {
		return getDBUpdateStatement(lastUpdate);
	}

	@Override
	protected void onPostExecute(String[] result) {
		super.onPostExecute(result);
		resultUpdate.KMBDBUpdate(result);
		this.cancel(true);
	}

}
