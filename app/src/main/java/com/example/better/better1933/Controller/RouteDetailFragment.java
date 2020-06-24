package com.example.better.better1933.Controller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.better.better1933.Infrastructure.KMB.KMBEtaReader;
import com.example.better.better1933.Infrastructure.KMB.KMBDB;
import com.example.better.better1933.MainActivity;
import com.example.better.better1933.Model.*;
import com.example.better.better1933.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RouteDetailFragment extends Fragment implements KMBEtaReader.IKMBEtaReaderUpdate {
	final static String fragmentId = "RouteDetailFragment";
	private static final String ARG_PARAM1 = "route";
	private static final String ARG_PARAM2 = "bound";
	private static final String ARG_PARAM3 = "serviceType";

	private RouteInfo route;
	private ListView stopListView;
	private RouteDetailFragment.StopAdapter stopListViewAdapter;
	private BkmarkEtaNode[] stopList = null;

	private void refreshTime() {
		for (int i = 0; i < stopListViewAdapter.getCount(); i++) {
			BkmarkEtaNode node = stopListViewAdapter.getItem(i);
			if (node == null) {
				return;
			}
			if (node.KMBEtaReader != null) {

				node.KMBEtaReader.cancel(true);
			}
			node.KMBEtaReader = new KMBEtaReader(stopListViewAdapter.getItem(i), i, this,getContext());
			View rowView = stopListView.getChildAt(i);
			if (rowView != null) {
				((TextView) rowView.findViewById(R.id.rdetail_row_time)).setText("--");
				((TextView) rowView.findViewById(R.id.rdetail_row_time1)).setText("--");
				((TextView) rowView.findViewById(R.id.rdetail_row_time2)).setText("--");
				((TextView) rowView.findViewById(R.id.rdetail_row_rawTime)).setText("--:--");
				((TextView) rowView.findViewById(R.id.rdetail_row_rawTime1)).setText("--:--");
				((TextView) rowView.findViewById(R.id.rdetail_row_rawTime2)).setText("--:--");
			}
			MainActivity mainActivity = (MainActivity)getActivity();
			if(mainActivity!=null) {
				node.KMBEtaReader.executeOnExecutor(mainActivity.EtaUpdateThreadPool);
			}
		}
	}

	public void onKMBEtaReaderUpdate(int id, String[] time, String[] rawtime) {
		BkmarkEtaNode node = stopListViewAdapter.getItem(id);
		if (node == null) {
			return;
		}
		node.rawTime = rawtime;
		node.time = time;
		if (id < stopListView.getFirstVisiblePosition() || id > stopListView.getFirstVisiblePosition() + stopListView.getChildCount()) {
			return;
		}
		View rowView = stopListView.getChildAt(id - stopListView.getFirstVisiblePosition());
		if (rowView != null) {
			if (time.length >= 1) {
				((TextView) rowView.findViewById(R.id.rdetail_row_time)).setText(time[0]);
			}
			if (time.length >= 2) {
				((TextView) rowView.findViewById(R.id.rdetail_row_time1)).setText(time[1]);
			}
			if (time.length >= 3) {
				((TextView) rowView.findViewById(R.id.rdetail_row_time2)).setText(time[2]);
			}
			if (rawtime.length >= 1) {
				((TextView) rowView.findViewById(R.id.rdetail_row_rawTime)).setText(rawtime[0]);
			}
			if (rawtime.length >= 2) {
				((TextView) rowView.findViewById(R.id.rdetail_row_rawTime1)).setText(rawtime[1]);
			}
			if (rawtime.length >= 3) {
				((TextView) rowView.findViewById(R.id.rdetail_row_rawTime2)).setText(rawtime[2]);
			}
		}
	}
	private static BkmarkEtaNode[] stopNodeConvert(DBStopInfoNode[] data) {
		if(data==null){
			return new BkmarkEtaNode[0];
		}
		BkmarkEtaNode[] etaList = new BkmarkEtaNode[data.length];
		for (int i = 0; i < etaList.length; i++) {
			etaList[i] = new BkmarkEtaNode(data[i]);
		}
		return etaList;
	}


	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param route       Parameter 1.
	 * @param bound       Parameter 2.
	 * @param serviceType Parameter 3.
	 * @return A new instance of fragment RouteDetailFragment.
	 */
	public static RouteDetailFragment newInstance(String route, int bound, String serviceType) {

		RouteDetailFragment fragment = new RouteDetailFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, route);
		args.putInt(ARG_PARAM2, bound);
		args.putString(ARG_PARAM3, serviceType);
		fragment.setArguments(args);
		return fragment;
	}

	private class ViewHolder {
		private final View rdetailRow;
		private TextView rdetailStop = null, rdetailFare;
		private TextView[] rdetailRawTime = null, rdetailTime = null;
		private LinearLayout[] rdetailMoreTimeLayout = null;

		ViewHolder(View rdetailRow) {
			this.rdetailRow = rdetailRow;
		}

		TextView getRdetailStopText() {
			if (this.rdetailStop == null) {
				this.rdetailStop = rdetailRow.findViewById(R.id.rdetail_row_stop);
			}
			return this.rdetailStop;
		}

		TextView getRdetailFareText() {
			if (this.rdetailFare == null) {
				this.rdetailFare = rdetailRow.findViewById(R.id.rdetail_row_fare);
			}
			return rdetailFare;
		}

		TextView[] getRdetailTimeText() {
			if (this.rdetailTime == null) {
				this.rdetailTime = new TextView[3];
				rdetailTime[0] = rdetailRow.findViewById(R.id.rdetail_row_time);
				rdetailTime[1] = rdetailRow.findViewById(R.id.rdetail_row_time1);
				rdetailTime[2] = rdetailRow.findViewById(R.id.rdetail_row_time2);
			}
			return this.rdetailTime;
		}

		LinearLayout[] getRdetailMoreTimeLayout() {
			if (this.rdetailMoreTimeLayout == null) {
				this.rdetailMoreTimeLayout = new LinearLayout[2];
				rdetailMoreTimeLayout[0] = rdetailRow.findViewById(R.id.rdetail_row_time1_LinearLayout);
				rdetailMoreTimeLayout[1] = rdetailRow.findViewById(R.id.rdetail_row_time2_LinearLayout);

			}
			return this.rdetailMoreTimeLayout;
		}

		TextView[] getRdetailRawTimeText() {
			if (this.rdetailRawTime == null) {
				this.rdetailRawTime = new TextView[3];
				rdetailRawTime[0] = rdetailRow.findViewById(R.id.rdetail_row_rawTime);
				rdetailRawTime[1] = rdetailRow.findViewById(R.id.rdetail_row_rawTime1);
				rdetailRawTime[2] = rdetailRow.findViewById(R.id.rdetail_row_rawTime2);
			}

			return this.rdetailRawTime;
		}
	}

	private class StopAdapter extends ArrayAdapter<BkmarkEtaNode> {
		StopAdapter(RouteDetailFragment a, BkmarkEtaNode[] data) {
			super(a.getActivity(), R.layout.rdetail_row, data);
		}

		@Override
		@NonNull
		public View getView(int position, View convertView, @Nullable ViewGroup parent) {

			RouteDetailFragment.ViewHolder holder;
			LayoutInflater inflater = getActivity().getLayoutInflater();
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.rdetail_row, parent, false);
				holder = new RouteDetailFragment.ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (RouteDetailFragment.ViewHolder) convertView.getTag();
			}
			setDetailText(holder, position);
			setTimeText(holder, position);
			setShowMore(holder, position);
			return convertView;
		}

		void switchShowMore(RouteDetailFragment.ViewHolder holder, int position) {
			BkmarkEtaNode node = this.getItem(position);
			if (node != null) {
				node.toggleShowMore();
			}
			setShowMore(holder, position);
		}

		void setShowMore(RouteDetailFragment.ViewHolder holder, int position) {
			BkmarkEtaNode node = this.getItem(position);
			if (node != null) {
				holder.getRdetailMoreTimeLayout()[0].setVisibility(node.isShowMore ? View.VISIBLE : View.GONE);
				holder.getRdetailMoreTimeLayout()[1].setVisibility(node.isShowMore ? View.VISIBLE : View.GONE);
			}
		}

		void setDetailText(RouteDetailFragment.ViewHolder holder, int position) {
			BkmarkEtaNode node = this.getItem(position);
			if (node != null) {
				holder.getRdetailStopText().setText(node.stop);
				holder.getRdetailFareText().setText(node.fare);
			}
		}

		void setTimeText(ViewHolder holder, int position) {
			TextView[] timeTextView = holder.getRdetailTimeText();
			TextView[] rawTimeTextView = holder.getRdetailRawTimeText();
			BkmarkEtaNode node = this.getItem(position);
			if (node != null) {
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

			Log.d("rdetail onCreate", getArguments().getString(ARG_PARAM1) + " created");
		} else {
			throw new NullPointerException();
		}
		//TODO toolbar back button
        /*((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);*/

	}

	/*void showRefreshStatus() {
			int statusCode = 0;
			for (BkmarkEtaNode node : stopList) {
					if (node.getUpdated()==0) {
							return;
					}
					if (node.getUpdated()!=1){
							statusCode=-1;
					}
			}
			if (getActivity()!=null)
					if (statusCode==0)
							((MainActivity) getActivity()).writeStatus("新已更", false);
					else
							((MainActivity) getActivity()).writeStatus("線已斷", false);
	}*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.route = KMBDB.getRoute(getArguments().getString(ARG_PARAM1), getArguments().getInt(ARG_PARAM2), getArguments().getString(ARG_PARAM3));
		Log.d("rdetail", route.route + " created");
		stopList = stopNodeConvert(KMBDB.getStopList(route));

		return inflater.inflate(R.layout.rdetail_main, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getActivity().setTitle(route.route + " 往 " + route.bound);

		stopListView = getView().findViewById(R.id.rdetail_stop_list);
		stopListViewAdapter = new StopAdapter(this, stopList);
		stopListView.setAdapter(stopListViewAdapter);
		TextView serviceTypeDescText = getView().findViewById(R.id.rdetail_servicetype_desc);
		//if there are service type desc to show
		if (route.serviceTypeDesc.length() > 0) {
			serviceTypeDescText.setText(route.serviceTypeDesc);
			serviceTypeDescText.setVisibility(TextView.VISIBLE);
		}

		stopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				stopListViewAdapter.switchShowMore((RouteDetailFragment.ViewHolder) view.getTag(), position);
			}
		});
		stopListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			                                        @Override
			                                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				                                        BookmarkEditFragment editDialog = BookmarkEditFragment.newInstance(stopListViewAdapter.getItem(position));
				                                        editDialog.show(getActivity().getFragmentManager(), BookmarkEditFragment.fragmentId);
				                                        return true;
			                                        }
		                                        }
		);


		FloatingActionButton refresh_fab = getView().findViewById(R.id.rdetail_refresh_fab);
		refresh_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshTime();
			}
		});
		FloatingActionButton restart_fab = getView().findViewById(R.id.rdetail_restart_fab);
		restart_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RouteDetailFragment.this.onStop();
				Fragment fragment = RouteDetailFragment.newInstance(route.route, route.boundSeq, route.serviceType);
				//change fragment
				FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
			}
		});
		FloatingActionButton info_fab = getView().findViewById(R.id.rdetail_info_fab);
		info_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Fragment fragment = RouteInfoFragment.newInstance(route.route, route.boundSeq, route.serviceType);

				//change fragment
				FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
				fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment).commit();

			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		refreshTime();
	}

	@Override
	public void onStop() {
		super.onStop();
		for (BkmarkEtaNode node : stopList) {
			if (node.KMBEtaReader != null) {
				node.KMBEtaReader.cancel(true);
			}
		}
	}

	@Override
	public void onDestroy() {
		//Log.d("RouteDetailFragment","onDestroy");
		super.onDestroy();
	}
}
