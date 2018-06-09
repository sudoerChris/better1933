package com.example.better.better1933.Infrastructure;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.better.better1933.Model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class KmbDBReader extends SQLiteOpenHelper {

    private static String DB_PATH;
    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private static final String KMB_DB = "kmb.db";// Database name
    private static final int DB_VERSION = 1;

    public KmbDBReader(Context context) {

        super(context, KMB_DB, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 4.2) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = context.getFilesDir().getPath() + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }

    private SQLiteDatabase getDatabase() {
        if (mDataBase == null || !mDataBase.isOpen()) {
            String myPath = DB_PATH + KMB_DB;
            if (!new File(myPath).exists()) {
                    createDataBase();
            }
            mDataBase = SQLiteDatabase.openDatabase(myPath, null,
                    SQLiteDatabase.OPEN_READONLY);
        }
        return mDataBase;
    }

    public void resetDB() {
        this.getReadableDatabase();
        this.close();
        try {
            // Copy the database from assests
            copyDataBase();
            Log.e("DataBaseHelper", "createDatabase database created");
        } catch (IOException mIOException) {
            throw new Error("Error Copying DataBase");
        }
    }

    private void createDataBase() {

        // If database not exists copy it from the assets

        boolean mDataBaseExist = checkDataBase();

        if (!mDataBaseExist) {
            resetDB();
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + KMB_DB);
        return dbFile.exists();
    }


    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(KMB_DB);
        String outFileName = DB_PATH + KMB_DB;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }


    @Override
    public synchronized void close() {

        if (mDataBase != null)
            mDataBase.close();

        super.close();

    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public RouteInfo getRoute(String route, int bound, String serviceType) {
        RouteInfo newRoute = new RouteInfo(route, bound, serviceType);
        Cursor c = getDatabase().rawQuery("SELECT destination_chi, service_type_desc_chi FROM kmb_routeboundmaster_ST WHERE route_no = ? AND bound_no = ? AND service_type = ?", new String[]{route, Integer.toString(bound), serviceType});
        c.moveToFirst();
        newRoute.bound= c.getString(c.getColumnIndexOrThrow("destination_chi"));
        newRoute.serviceTypeDesc = c.getString(c.getColumnIndexOrThrow("service_type_desc_chi"));
        c.close();
        return newRoute;
    }
    public String[] getName(DBStopInfoNode node){
        String[] result = new String[2];
        Cursor c = getDatabase().rawQuery("SELECT stop_name_chi, destination_chi from kmb_routeboundmaster_ST,kmb_routestopfile_ST where kmb_routeboundmaster_ST.route_no=? and kmb_routestopfile_ST.route_no=? and bound_no = ? and bound=? and stop_seq=? and kmb_routestopfile_ST.service_type=? and kmb_routeboundmaster_ST.service_type=?",new String[]{node.route,node.route,String.valueOf(node.bound_seq),String.valueOf(node.bound_seq),String.valueOf( node.stop_seq),node.serviceType,node.serviceType});
        c.moveToFirst();
        if(c.isAfterLast()){
            return new String[]{"車站已取消","車站已取消"};
        }
        result[0] = c.getString(0);
        result[1] = c.getString(1);
        c.close();
        return result;
    }
    public RouteInfo[] getSearchRoute(String partialRoute) {
        Cursor c = getDatabase().rawQuery("SELECT route_no, bound_no, destination_chi, service_type,service_type_desc_chi FROM kmb_routeboundmaster_ST WHERE route_no like ?", new String[]{partialRoute + "%"});
        c.moveToFirst();
        if (c.getCount() == 0) {
            Log.d("getSearchRoute", partialRoute + ": nothing return");
            RouteInfo[] routeList = new RouteInfo[1];
            routeList[0] = new RouteInfo("", 0, "");
            routeList[0].bound = "No Result";
            return routeList;
        }
        RouteInfo[] routeList = new RouteInfo[c.getCount()];
        Log.d("getSearchRoute", partialRoute + ": " + Integer.toString(routeList.length) + " return");
        for (int i = 0; i < routeList.length; i++) {
            routeList[i] = new RouteInfo(c.getString(c.getColumnIndexOrThrow("route_no")), c.getInt(c.getColumnIndexOrThrow("bound_no")), c.getString(c.getColumnIndexOrThrow("service_type")));
            routeList[i].serviceTypeDesc = c.getString(c.getColumnIndexOrThrow("service_type_desc_chi"));
            routeList[i].bound = c.getString(c.getColumnIndexOrThrow("destination_chi"));
            c.moveToNext();
        }
        c.close();
        return routeList;
    }

    public DBStopInfoNode[] getStopList(RouteInfo route) {
        Cursor c = getDatabase().rawQuery("SELECT stop_seq, stop_name_chi, air_cond_fare FROM kmb_routestopfile_ST WHERE route_no = ? AND bound = ? AND service_type = ? order by stop_seq asc", new String[]{route.route, Integer.toString(route.boundSeq), route.serviceType});
        c.moveToFirst();
        if (c.getCount() == 0) {
            return null;
        }
        DBStopInfoNode[] stopList = new DBStopInfoNode[c.getCount()];
        for (int i = 0; i < stopList.length; i++) {
            stopList[i] = new DBStopInfoNode(route.route, route.boundSeq, c.getInt(c.getColumnIndexOrThrow("stop_seq")), route.serviceType);
            stopList[i].stop = c.getString(c.getColumnIndexOrThrow("stop_name_chi"));
            stopList[i].bound = route.bound;
            stopList[i].fare = c.getString(c.getColumnIndexOrThrow("air_cond_fare"));
            c.moveToNext();
        }
        c.close();
        return stopList;
    }

    public DBStopInfoNode getStop(RouteInfo route, int stop_seq) {
        Cursor c = getDatabase().rawQuery("SELECT stop_name_chi, air_cond_fare, destination_chi FROM kmb_routestopfile_ST, kmb_routeboundmaster_ST WHERE kmb_routeboundmaster_ST.route_no = ? AND kmb_routeboundmaster_ST.bound_no = ? AND kmb_routeboundmaster_ST.service_type = ? AND kmb_routeboundmaster_ST.route_no = kmb_routestopfile_ST.route_no AND kmb_routeboundmaster_ST.bound_no =kmb_routestopfile_ST.bound AND kmb_routeboundmaster_ST.service_type = kmb_routestopfile_ST.service_type AND stop_seq = ?", new String[]{route.bound, Integer.toString(route.boundSeq), route.serviceType, Integer.toString(stop_seq)});
        c.moveToFirst();
        if (c.getCount() == 0) {
            return null;
        }
        DBStopInfoNode stop = new DBStopInfoNode(route.route, route.boundSeq, stop_seq, route.serviceType);
        stop.stop = c.getString(c.getColumnIndexOrThrow("stop_name_chi"));
        stop.bound = c.getString(c.getColumnIndexOrThrow("destination_chi"));
        stop.fare = c.getString(c.getColumnIndexOrThrow("air_cond_fare"));
        c.close();
        return stop;
    }

    public String[] getFirstLastInfo(RouteInfo route) {
        Cursor c = getDatabase().rawQuery("select wf_first_trip, wf_last_trip, hd_first_trip, hd_last_trip, sat_first_trip, sat_last_trip from kmb_routeboundmaster_ST where route_no = ? and bound_no = ? and service_type = ?", new String[]{route.route, Integer.toString(route.boundSeq), route.serviceType});
        c.moveToFirst();
        if (c.getCount() == 0) {
            return null;
        }
        String[] result = new String[6];
        result[0] = c.getString(c.getColumnIndexOrThrow("wf_first_trip"));
        if (result[0].equals("0")) {
            result[0] = "不提供服務";
        }
        result[1] = c.getString(c.getColumnIndexOrThrow("wf_last_trip"));
        if (result[1].equals("0")) {
            result[0] = "不提供服務";
        }
        result[2] = c.getString(c.getColumnIndexOrThrow("sat_first_trip"));
        if (result[2].equals("0")) {
            result[0] = "不提供服務";
        }
        result[3] = c.getString(c.getColumnIndexOrThrow("sat_last_trip"));
        if (result[3].equals("0")) {
            result[0] = "不提供服務";
        }
        result[4] = c.getString(c.getColumnIndexOrThrow("hd_first_trip"));
        if (result[4].equals("0")) {
            result[0] = "不提供服務";
        }
        result[5] = c.getString(c.getColumnIndexOrThrow("hd_last_trip"));
        if (result[5].equals("0")) {
            result[0] = "不提供服務";
        }
        c.close();
        return result;
    }

    public String getSpecialNoteInfo(String route) {
        Cursor c = getDatabase().rawQuery("select desc_chi from kmb_specialnote where route = ?", new String[]{route});
        c.moveToFirst();
        if (c.getCount() == 0) {
            return null;
        }
        String result = c.getString(c.getColumnIndexOrThrow("desc_chi"));
        c.close();
        return result;
    }

    public FreqNode[] getFreqNode(RouteInfo route, String dayCode) {
        Cursor c = getDatabase().rawQuery("select time_" + route.boundSeq + " as time, freq_" + route.boundSeq + " as freq from kmb_routefreqfile_ST where route_no = ? and service_type = ? and day_code = ? order by seq_no", new String[]{route.route, route.serviceType, dayCode});
        c.moveToFirst();
        if (c.getCount() == 0) {
            return null;
        }
        ArrayList<FreqNode> resultList = new ArrayList<>();
        for (int i = 0; i < c.getCount(); i++) {
            if (!c.getString(c.getColumnIndexOrThrow("time")).equals("")) {
                resultList.add(new FreqNode(c.getString(c.getColumnIndexOrThrow("time")), c.getString(c.getColumnIndexOrThrow("freq"))));
            }
            c.moveToNext();
        }
        c.close();
        if (resultList.size() == 0) {
            return null;
        }
        FreqNode[] result = new FreqNode[resultList.size()];
        resultList.toArray(result);
        return result;
    }
}
