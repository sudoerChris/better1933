package com.example.better.better1933;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.ThreadPoolExecutor;

//update Url:
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Context context;
	private TextView statusBarTextView;
	public Intent notifyServiceIntent;
	void writeStatus(String status, boolean stay){
        statusBarTextView.setText(status);
        statusBarTextView.setVisibility(TextView.VISIBLE);
        if (!stay) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    statusBarTextView.setVisibility(TextView.GONE);
                }
            }, 1000);
        }
    }
    void statusGone(){
        statusBarTextView.setVisibility(TextView.GONE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR).setCorePoolSize(15);
        ((ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR).setMaximumPoolSize(100);
        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.main_nev);
	    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("setNavList",v.toString());
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        statusBarTextView = (TextView) findViewById(R.id.app_status);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Fragment fragment = new ViewBookmarkFragment();
        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment, ViewBookmarkFragment.fragmentId).commit();
        
        //start background service
	    notifyServiceIntent = new Intent(this, NotifyService.class);
	    startService(notifyServiceIntent);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(getSupportFragmentManager().getBackStackEntryCount()<=1){
                this.finish();
            }else {
                getSupportFragmentManager().popBackStackImmediate();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onSupportNavigateUp(){
        Log.d("toolbar","onSupportNavigateUp");
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        Fragment fragment = null;
        String fragmentTag = "";
        if (id == R.id.action_settings) {
            fragment = new SettingFragment();
            fragmentTag = SettingFragment.fragmentId;
        }
        if (id == R.id.action_about) {
            fragment = new AboutFragment();
            fragmentTag = AboutFragment.fragmentId;
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment, fragmentTag).commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        Fragment fragment;
        String fragmentTag;
	    fragment = new ViewBookmarkFragment();
	    fragmentTag = ViewBookmarkFragment.fragmentId;
	    while(getSupportFragmentManager().getBackStackEntryCount()>1){
		    getSupportFragmentManager().popBackStackImmediate();
	    }
	    //getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment, fragmentTag).commit();
	
	    switch (id) {
		    case R.id.nav_bookmark:
			    fragment = null;
			    break;
		    case R.id.nav_route_search:
			    fragment = new RouteSearchFragment();
			    fragmentTag = RouteSearchFragment.fragmentId;
			    break;
		    case R.id.nav_setting:
			    fragment = new SettingFragment();
			    fragmentTag = SettingFragment.fragmentId;
			    break;
		    case R.id.nav_about:
			    fragment = new AboutFragment();
			    fragmentTag = AboutFragment.fragmentId;
			    break;
	    }
        if (fragment != null) {
	        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment, fragmentTag).commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void onDestroy(){
        super.onDestroy();
        //stopService(notifyServiceIntent);
    }
}
