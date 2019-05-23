package com.example.better.better1933.Controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.example.better.better1933.Infrastructure.*;
import com.example.better.better1933.Infrastructure.KMB.KMBEtaReader;
import com.example.better.better1933.MainActivity;
import com.example.better.better1933.Model.*;
import com.example.better.better1933.R;
import com.example.better.better1933.Service.NotifyService;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class ViewBookmarkFragment extends Fragment implements KMBEtaReader.IKMBEtaReaderUpdate {
	
	final public static String fragmentId = "ViewBookmarkFragment";
	private ListView bookmarkListView;
	private BookmarkAdapter bookmarkListViewAdapter;
	private BkmarkEtaNode[] bookmarkList;
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	@Override
	public void onStart(){
		super.onStart();
		
		
		refreshTime();
	}
	@Override
	public void onStop(){
		super.onStop();
		for(BkmarkEtaNode node : bookmarkList){
			if(node.KMBEtaReader !=null){
				node.KMBEtaReader.cancel(true);
			}
		}
	}

	private boolean migration(){
		SharedPreferences oldBookmarks = this.getActivity().getSharedPreferences(getString(R.string.bookmark_file_key), MODE_PRIVATE);
			int index = 0;
		ArrayList<DBStopInfoNode> nodeList = new ArrayList<>();
			while(oldBookmarks.getString(getString(R.string.bookmark_stop_route_preset) + index,null)!=null) {
				nodeList.add( new DBStopInfoNode(
						oldBookmarks.getString(getString(R.string.bookmark_stop_route_preset) + index, null),
						oldBookmarks.getInt(getString(R.string.bookmark_stop_bound_seq_preset) + index, 0),
						oldBookmarks.getInt(getString(R.string.bookmark_stop_seq_preset) + index, 0),
						oldBookmarks.getString(getString(R.string.bookmark_stop_servicetype_preset) + index, "")));
				index++;
			}
		DBStopInfoNode[] temp = new DBStopInfoNode[nodeList.size()];
		nodeList.toArray(temp);
			LocalDataDB.replaceBookmarks(temp);
			SharedPreferences.Editor editor =  oldBookmarks.edit();
			editor.clear();
			editor.apply();
			return index==0;
	}

	private void swapBookmark(int mainIndex, int targetIndex) {
		if (targetIndex < 0 || targetIndex >= bookmarkList.length) {
			return;
		}
		BkmarkEtaNode mainNode = bookmarkList[mainIndex];
		BkmarkEtaNode targetNode = bookmarkList[targetIndex];
		bookmarkList[targetIndex] = mainNode;
		bookmarkList[mainIndex] = targetNode;
		LocalDataDB.swapBookmark(mainNode.index,targetNode.index);
		int swapTemp = mainNode.index;
		mainNode.index = targetNode.index;
		targetNode.index = swapTemp;
		bookmarkListViewAdapter.notifyDataSetChanged();
		Intent notifyServiceIntent = new Intent(getActivity(), NotifyService.class);
		getActivity().stopService(notifyServiceIntent);
		getActivity().startService(notifyServiceIntent);
	}
	@SuppressWarnings("ConstantConditions")
	private void refreshTime() {
		
		for (int i = 0; i < bookmarkListViewAdapter.getCount(); i++) {
			if (bookmarkListViewAdapter.getItem(i) == null) {
				return;
			}
			if (bookmarkListViewAdapter.getItem(i).KMBEtaReader != null) {
				bookmarkListViewAdapter.getItem(i).KMBEtaReader.cancel(true);
			}
			bookmarkListViewAdapter.getItem(i).KMBEtaReader = new KMBEtaReader(bookmarkListViewAdapter.getItem(i), i, this);

			View rowView = bookmarkListView.getChildAt(i);
			if (rowView != null) {
				((TextView) rowView.findViewById(R.id.bkmark_row_time)).setText("--");
				((TextView) rowView.findViewById(R.id.bkmark_row_time1)).setText("--");
				((TextView) rowView.findViewById(R.id.bkmark_row_time2)).setText("--");
				((TextView) rowView.findViewById(R.id.bkmark_row_rawTime)).setText("--:--");
				((TextView) rowView.findViewById(R.id.bkmark_row_rawTime1)).setText("--:--");
				((TextView) rowView.findViewById(R.id.bkmark_row_rawTime2)).setText("--:--");
			}
			MainActivity mainActivity = (MainActivity)getActivity();
			if(mainActivity!=null) {
				bookmarkListViewAdapter.getItem(i).KMBEtaReader.executeOnExecutor(mainActivity.EtaUpdateThreadPool);
			}
		}
	}
	public void onKMBEtaReaderUpdate(int id, String[] time, String[] rawtime) {
		BkmarkEtaNode node = bookmarkListViewAdapter.getItem(id);
		if(node==null){
			return;
		}
		node.rawTime = rawtime;
		node.time = time;
		
		if(id<bookmarkListView.getFirstVisiblePosition()||id>bookmarkListView.getFirstVisiblePosition()+bookmarkListView.getChildCount()){
			return;
		}
		View rowView = bookmarkListView.getChildAt(id-bookmarkListView.getFirstVisiblePosition());
		if (rowView != null) {
			if (time.length >= 1) {
				((TextView) rowView.findViewById(R.id.bkmark_row_time)).setText(time[0]);
			}
			if (time.length >= 2) {
				((TextView) rowView.findViewById(R.id.bkmark_row_time1)).setText(time[1]);
			}
			if (time.length >= 3) {
				((TextView) rowView.findViewById(R.id.bkmark_row_time2)).setText(time[2]);
			}
			if (rawtime.length >= 1) {
				((TextView) rowView.findViewById(R.id.bkmark_row_rawTime)).setText(rawtime[0]);
			}
			if (rawtime.length >= 2) {
				((TextView) rowView.findViewById(R.id.bkmark_row_rawTime1)).setText(rawtime[1]);
			}
			if (rawtime.length >= 3) {
				((TextView) rowView.findViewById(R.id.bkmark_row_rawTime2)).setText(rawtime[2]);
			}
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("fragment", "new ViewBookmarkFragment created");
	}
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.bkmark_main, container, false);
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		//super.onCreate(savedInstanceState);
		
		
		getActivity().setTitle(getResources().getString(R.string.title_home_activity));
		bookmarkList = LocalDataDB.getBookmarks();
		if(bookmarkList.length==0){
			migration();
		}
		bookmarkList =  LocalDataDB.getBookmarks();
		{
			View tempView = getView();
			if(tempView!=null) {
				bookmarkListView = tempView.findViewById(R.id.bkmark_ListView);
			}
		}
		bookmarkListViewAdapter = new BookmarkAdapter(this, bookmarkList);
		bookmarkListView.setAdapter(bookmarkListViewAdapter);
		
		bookmarkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				bookmarkListViewAdapter.switchShowMore((ViewHolder) view.getTag(), position);
			}
		});
		bookmarkListView.setOnCreateContextMenuListener(new ListView.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
			                                ContextMenu.ContextMenuInfo menuInfo) {
				getActivity().getMenuInflater().inflate(R.menu.bkmark_item_menu, menu);
				
			}
		});
		
		
		FloatingActionButton refresh_fab = getView().findViewById(R.id.bkmark_refresh_fab);
		refresh_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshTime();
			}
		});
		FloatingActionButton restart_fab = getView().findViewById(R.id.bkmark_restart_fab);
		restart_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewBookmarkFragment.this.onStop();
				Fragment fragment = new ViewBookmarkFragment();
				FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "ViewBookmarkFragment").commit();
				
			}
		});
		
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Log.d("menu", "menu id" + item.getItemId() + "pressed");
		Fragment fragment = null;
		FragmentManager fragmentManager;
		switch (item.getItemId()) {
			case R.id.bkmark_delete:
				if (bookmarkList[menuInfo.position].KMBEtaReader != null) {
					bookmarkList[menuInfo.position].KMBEtaReader.cancel(true);
				}
				LocalDataDB.deleteBookmark(bookmarkList[menuInfo.position]);
				bookmarkList = LocalDataDB.getBookmarks();
				bookmarkListViewAdapter = new BookmarkAdapter(this, bookmarkList);
				bookmarkListView.setAdapter(bookmarkListViewAdapter);
				Intent notifyServiceIntent = new Intent(getActivity(), NotifyService.class);
				getActivity().stopService(notifyServiceIntent);
				getActivity().startService(notifyServiceIntent);
				refreshTime();
				break;
			case R.id.bkmark_route_detail:
				fragment = RouteDetailFragment.newInstance(bookmarkList[menuInfo.position].route, bookmarkList[menuInfo.position].bound_seq, bookmarkList[menuInfo.position].serviceType);
				break;
			case R.id.bkmark_time_table:
				fragment = RouteInfoFragment.newInstance(bookmarkList[menuInfo.position].route, bookmarkList[menuInfo.position].bound_seq, bookmarkList[menuInfo.position].serviceType);
				break;
			case R.id.bkmark_moveup:
				swapBookmark(menuInfo.position, menuInfo.position - 1);
				
				break;
			case R.id.bkmark_movedown:
				swapBookmark(menuInfo.position, menuInfo.position + 1);
				break;
			case R.id.bkmark_edit:
				BookmarkEditFragment editDialog = BookmarkEditFragment.newInstance(bookmarkList[menuInfo.position]);
				editDialog.show(getActivity().getFragmentManager(),BookmarkEditFragment.fragmentId);
				break;
		}
		
		
		if (fragment != null) {
			fragmentManager = getActivity().getSupportFragmentManager();
			fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment).commit();
		}
		
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		//noinspection SimplifiableIfStatement
		
		switch (id) {
			case R.id.action_about:
				return true;
			case R.id.action_settings:
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class ViewHolder {
		private final View row;
		private TextView route = null, bound = null, stop = null;
		private TextView[] rawTime = null, time = null;
		private LinearLayout[] moreTimeLayout = null;
		
		ViewHolder(View row) {
			this.row = row;
		}
		
		TextView getRouteText() {
			if (this.route == null) {
				this.route = row.findViewById(R.id.bkmark_row_route);
			}
			return this.route;
		}
		
		TextView getBoundText() {
			if (this.bound == null) {
				this.bound = row.findViewById(R.id.bkmark_row_bound);
			}
			return this.bound;
		}
		
		TextView getStopText() {
			if (this.stop == null) {
				this.stop = row.findViewById(R.id.bkmark_row_stop);
			}
			return this.stop;
		}
		
		TextView[] getTimeText() {
			if (this.time == null) {
				this.time = new TextView[3];
				time[0] = row.findViewById(R.id.bkmark_row_time);
				time[1] = row.findViewById(R.id.bkmark_row_time1);
				time[2] = row.findViewById(R.id.bkmark_row_time2);
			}
			return this.time;
		}
		
		LinearLayout[] getMoreTimeLayout() {
			if (this.moreTimeLayout == null) {
				this.moreTimeLayout = new LinearLayout[2];
				moreTimeLayout[0] = row.findViewById(R.id.bkmark_row_time1_LinearLayout);
				moreTimeLayout[1] = row.findViewById(R.id.bkmark_row_time2_LinearLayout);
				
			}
			return this.moreTimeLayout;
		}
		
		TextView[] getRawTimeText() {
			if (this.rawTime == null) {
				this.rawTime = new TextView[3];
				rawTime[0] = row.findViewById(R.id.bkmark_row_rawTime);
				rawTime[1] = row.findViewById(R.id.bkmark_row_rawTime1);
				rawTime[2] = row.findViewById(R.id.bkmark_row_rawTime2);
			}
			
			return this.rawTime;
		}
		
	}
	
	private class BookmarkAdapter extends ArrayAdapter<BkmarkEtaNode> {
		BookmarkAdapter(ViewBookmarkFragment a, BkmarkEtaNode[] data) {
			super(a.getActivity(), R.layout.bkmark_row, data);
		}
		
		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			
			ViewHolder holder;
			LayoutInflater inflater = getActivity().getLayoutInflater();
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.bkmark_row, parent, false);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			setDetailText(holder, position);
			setTimeText(holder, position);
			setShowMore(holder,position);
			return convertView;
		}
		
		@SuppressWarnings("ConstantConditions")
		void switchShowMore(ViewHolder holder, int position) {
			this.getItem(position).toggleShowMore();
			setShowMore(holder,position);
		}
		void setShowMore(ViewHolder holder,int position){
			BkmarkEtaNode node = this.getItem(position);
			if(node!=null) {
				holder.getMoreTimeLayout()[0].setVisibility(node.isShowMore ? View.VISIBLE : View.GONE);
				holder.getMoreTimeLayout()[1].setVisibility(node.isShowMore ? View.VISIBLE : View.GONE);
			}
		}
		@SuppressWarnings("ConstantConditions")
		void setDetailText(ViewHolder holder, int position) {
			holder.getBoundText().setText(this.getItem(position).bound);
			holder.getRouteText().setText(this.getItem(position).route);
			holder.getStopText().setText(this.getItem(position).stop);
		}
		
		void setTimeText(ViewHolder holder, int position) {
			BkmarkEtaNode node =  this.getItem(position);
			if(node!=null) {
				TextView[] timeTextView = holder.getTimeText();
				TextView[] rawTimeTextView = holder.getRawTimeText();
				String[] time = node.time;
				String[] rawtime = node.rawTime;
				if (time.length >= 1) {
					timeTextView[0].setText(time[0]);
				}
				if (time.length >= 2) {
					timeTextView[1].setText(time[1]);
				}
				if (time.length >= 3) {
					timeTextView[2].setText(time[2]);
				}
				if (rawtime.length >= 1) {
					rawTimeTextView[0].setText(rawtime[0]);
				}
				if (rawtime.length >= 2) {
					rawTimeTextView[1].setText(rawtime[1]);
				}
				if (rawtime.length >= 3) {
					rawTimeTextView[2].setText(rawtime[2]);
				}
			}
		}
	}
}