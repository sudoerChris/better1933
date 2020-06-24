package com.example.better.better1933.Infrastructure.KMB;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.better.better1933.Model.DBStopInfoNode;
import com.mklimek.sslutilsandroid.SslUtils;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
public class KMBEtaReader extends AsyncTask<Void, String[], Void> {
	public interface IKMBEtaReaderUpdate {
		void onKMBEtaReaderUpdate(int id, String[] time, String[] rawTime);
	}

	private final IKMBEtaReaderUpdate updateInterface;
	private final DBStopInfoNode node;
	private final int id;
	private Context context;

	private static String readFromUrl(String postContent, Context context) throws IOException {
		HttpsURLConnection conn = (HttpsURLConnection) new URL("https://etav3.kmb.hk/?action=geteta").openConnection();
		SSLContext serverCert = SslUtils.getSslContextForCertificateFile(context, "kmbhk.cer");
		conn.setSSLSocketFactory(serverCert.getSocketFactory());
		conn.setConnectTimeout(10000);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		try (BufferedWriter reqWriter = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8))) {
			reqWriter.write(postContent);
			reqWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try (BufferedReader respReader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = respReader.readLine()) != null) {
				response.append(line);
			}
			return response.toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			try (BufferedReader respReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
				String line;
				StringBuilder response = new StringBuilder();
				while ((line = respReader.readLine()) != null) {
					response.append(line);
				}
				Log.e("readFromUrl",response.toString());
			}
		}

		return null;
	}

	public KMBEtaReader(DBStopInfoNode node, int id, @NonNull IKMBEtaReaderUpdate updateInterface, Context context) {
		this.updateInterface = updateInterface;
		this.node = node;
		this.id = id;
		this.context = context;
	}
	private static class KMBEncrypt {
		static class KMBEncryptResult {
			String iv = null;
			String ctr = null;
			String d = null;
		}

		static KMBEncryptResult encrypt(String msg, String iv) {
			KMBEncryptResult aVar = new KMBEncryptResult();
			BigInteger ivBi;
			String bigInteger2;
			try {
				if (iv.equals("")) {
					ivBi = new BigInteger(50, new Random());
				} else {
					ivBi = new BigInteger(iv);
				}
				bigInteger2 = ivBi.toString(16);
				String prependIv = "00000000000000000000000000000000".substring(bigInteger2.length()) +
								bigInteger2;
				Cipher instance = Cipher.getInstance("AES/CTR/NoPadding");
				instance.init(1, getKey(), getIv(prependIv));
				aVar.d = new String(Hex.encodeHex(instance.doFinal(msg.getBytes())));
				aVar.ctr = ivBi.toString();
				aVar.iv = prependIv;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return aVar;
		}

		private static IvParameterSpec getIv(String str) {
			try {
				return new IvParameterSpec(Hex.decodeHex(str.toCharArray()));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		private static SecretKeySpec getKey() {
			try {
				return new SecretKeySpec(Hex.decodeHex("801C26C9AFB352FA4DF8C009BAB0FA72".toCharArray()), "AES");
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	private static long getSDelay() {
		try {
			URLConnection conn = new URL("http://etadatafeed.kmb.hk:1933/GetData.ashx?type=Server_T").openConnection();
			conn.setConnectTimeout(1000);
			conn.setReadTimeout(1500);
			conn.setUseCaches(false);
			conn.connect();
			Scanner s = new Scanner(conn.getInputStream());
			StringBuilder result = new StringBuilder();
			while (s.hasNext()) {
				result.append(s.next());
			}
			String stime = new JSONArray(result.toString()).getJSONObject(0).getString("stime");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss",Locale.getDefault());
			return sdf.parse(stime).getTime() - System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	@Override
	protected Void doInBackground(Void... param) {
		int trial = 0;
		long sdelay = -1;
		try {
			while (trial < 20) {
				if (isCancelled()) {
					return null;
				}
				try {
					if(sdelay==-1){
						sdelay =  getSDelay();
					}
					SimpleDateFormat apiKeySdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
					apiKeySdf.setTimeZone(TimeZone.getTimeZone("UTC"));
					KMBEncrypt.KMBEncryptResult apikeyEncResult = KMBEncrypt.encrypt(apiKeySdf.format(new Date(System.currentTimeMillis() +sdelay)), "");
					String reqParam = String.format(Locale.US,"?lang=tc&route=%s&bound=%d&stop_seq=%d&updated=-1&service_type=%s&vendor_id=%s&apiKey=%s&ctr=%s", node.route, node.bound_seq, node.stop_seq, node.serviceType.replaceFirst("0",""),new BigInteger(64, new Random()).toString(16), apikeyEncResult.d , apikeyEncResult.ctr);
					KMBEncrypt.KMBEncryptResult reqStringEncResult = KMBEncrypt.encrypt(reqParam, apikeyEncResult.ctr);
					JSONObject reqJson = new JSONObject();
					reqJson.put("d", reqStringEncResult.d);
					reqJson.put("ctr", apikeyEncResult.ctr);
					JSONArray json = new JSONArray(readFromUrl(reqJson.toString(),context)).getJSONObject(0).getJSONArray("eta");
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
				} else if (resultTime[0][i].equals("暫時沒有預定班次 九巴") || resultTime[0][i].equals("暫時沒有預定班次")) {
					time[i] = "--";
					rawTime[i] = "沒有預定班次";
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
					rawTime[i] = rawTime[i].substring(0, 8);
				} else if (rawTime[i].contains("預定班次")) {
					rawTime[i] = rawTime[i].substring(0, 8);
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


