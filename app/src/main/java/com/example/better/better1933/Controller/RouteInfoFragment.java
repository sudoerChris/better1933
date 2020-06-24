package com.example.better.better1933.Controller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.better.better1933.Infrastructure.KMB.KMBDB;
import com.example.better.better1933.Model.*;
import com.example.better.better1933.R;

public class RouteInfoFragment extends Fragment {
	private static final String ARG_PARAM1 = "route";
	private static final String ARG_PARAM2 = "bound";
	private static final String ARG_PARAM3 = "serviceType";

	private RouteInfo route;


	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param route       Parameter 1.
	 * @param bound       Parameter 2.
	 * @param serviceType Parameter 3.
	 * @return A new instance of fragment RouteDetailFragment.
	 */
	public static RouteInfoFragment newInstance(String route, int bound, String serviceType) {

		RouteInfoFragment fragment = new RouteInfoFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, route);
		args.putInt(ARG_PARAM2, bound);
		args.putString(ARG_PARAM3, serviceType);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

			Log.d("rdetail onCreate", getArguments().getString(ARG_PARAM1) + " created");
		} else {
			throw new NullPointerException();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.route = KMBDB.getRoute(getArguments().getString(ARG_PARAM1), getArguments().getInt(ARG_PARAM2), getArguments().getString(ARG_PARAM3));
		Log.d("rdetail", route.route + " created");

		return inflater.inflate(R.layout.rinfo_content, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().setTitle(route.route + " 往 " + route.bound);
		TextView specialNoteText = getView().findViewById(R.id.rinfo_special_note);
		TextView mfFirstText = getView().findViewById(R.id.rinfo_mf_first);
		TextView mfLastText = getView().findViewById(R.id.rinfo_mf_last);
		TextView satFirstText = getView().findViewById(R.id.rinfo_sat_first);
		TextView satLastText = getView().findViewById(R.id.rinfo_sat_last);
		TextView hdFirstText = getView().findViewById(R.id.rinfo_hd_first);
		TextView hdLastText = getView().findViewById(R.id.rinfo_hd_last);

		//get info
		String[] firstLastInfo = KMBDB.getFirstLastInfo(route);
		mfFirstText.setText(firstLastInfo[0]);
		mfLastText.setText(firstLastInfo[1]);
		satFirstText.setText(firstLastInfo[2]);
		satLastText.setText(firstLastInfo[3]);
		hdFirstText.setText(firstLastInfo[4]);
		hdLastText.setText(firstLastInfo[5]);
		String specialNote = KMBDB.getSpecialNoteInfo(route.route);
		if (specialNote != null) {
			specialNoteText.setText(specialNote);
			specialNoteText.setVisibility(View.VISIBLE);
		}

		boolean skipMF = false, skipSat = false, skipH = false;
		//process MS,MF,D in "MF" section
		FreqNode[] freqMFList = KMBDB.getFreqNode(route, "MF");
		if (freqMFList == null) {
			freqMFList = KMBDB.getFreqNode(route, "MS");
			if (freqMFList != null) {
				((TextView) getView().findViewById(R.id.rinfo_mf_title)).setText("一至六");
				skipSat = true;
			} else {
				freqMFList = KMBDB.getFreqNode(route, "D");
				if (freqMFList != null) {
					((TextView) getView().findViewById(R.id.rinfo_mf_title)).setText("每日");
					skipSat = true;
					skipH = true;
				} else {
					skipMF = true;
				}
			}
		}

		if (skipMF || freqMFList == null) {
			getView().findViewById(R.id.rinfo_mf_layout).setVisibility(View.GONE);
		} else {
			ListView freqMFListView = getView().findViewById(R.id.rinfo_mf_frequecy);
			FreqAdapter freqMFListViewAdapter = new FreqAdapter(this, freqMFList);
			freqMFListView.setAdapter(freqMFListViewAdapter);
			View listItem = freqMFListViewAdapter.getView(0, null, freqMFListView);
			listItem.measure(0, 0);
			Log.wtf("measure", Integer.toString(listItem.getMeasuredHeight()));
			ViewGroup.LayoutParams params = freqMFListView.getLayoutParams();
			params.height = freqMFListViewAdapter.getCount() * listItem.getMeasuredHeight() + (freqMFListView.getDividerHeight() * (freqMFListViewAdapter.getCount() - 1));
			freqMFListView.setLayoutParams(params);
		}

		FreqNode[] freqSList = KMBDB.getFreqNode(route, "S");
		if (skipSat || freqSList == null) {
			getView().findViewById(R.id.rinfo_s_layout).setVisibility(View.GONE);
		} else {

			ListView freqSListView = getView().findViewById(R.id.rinfo_s_frequecy);
			FreqAdapter freqSListViewAdapter = new FreqAdapter(this, freqSList);
			freqSListView.setAdapter(freqSListViewAdapter);

			View listItem = freqSListViewAdapter.getView(0, null, freqSListView);
			listItem.measure(0, 0);
			Log.wtf("measure", Integer.toString(listItem.getMeasuredHeight()));
			ViewGroup.LayoutParams params = freqSListView.getLayoutParams();
			params.height = freqSListViewAdapter.getCount() * listItem.getMeasuredHeight() + (freqSListView.getDividerHeight() * (freqSListViewAdapter.getCount() - 1));
			freqSListView.setLayoutParams(params);
		}

		FreqNode[] freqHList = KMBDB.getFreqNode(route, "H");
		if (skipH || freqHList == null) {
			getView().findViewById(R.id.rinfo_h_layout).setVisibility(View.GONE);
		} else {

			ListView freqHListView = getView().findViewById(R.id.rinfo_h_frequecy);
			FreqAdapter freqHListViewAdapter = new FreqAdapter(this, freqHList);
			freqHListView.setAdapter(freqHListViewAdapter);


			View listItem = freqHListViewAdapter.getView(0, null, freqHListView);
			listItem.measure(0, 0);
			Log.wtf("measure", Integer.toString(listItem.getMeasuredHeight()));
			ViewGroup.LayoutParams params = freqHListView.getLayoutParams();
			params.height = freqHListViewAdapter.getCount() * listItem.getMeasuredHeight() + (freqHListView.getDividerHeight() * (freqHListViewAdapter.getCount() - 1));
			freqHListView.setLayoutParams(params);
		}

	}


	private class ViewHolder {
		private final View rinfoRow;
		private TextView rinfoTimeSlot = null, rinfoTime = null;

		ViewHolder(View rinfoRow) {
			this.rinfoRow = rinfoRow;
		}

		TextView getRinfoTimeSlot() {
			if (this.rinfoTimeSlot == null) {
				this.rinfoTimeSlot = rinfoRow.findViewById(R.id.rinfo_row_time_slot);
			}
			return this.rinfoTimeSlot;
		}

		TextView getRinfoTime() {
			if (this.rinfoTime == null) {
				this.rinfoTime = rinfoRow.findViewById(R.id.rinfo_row_time);
			}
			return this.rinfoTime;
		}
	}

	private class FreqAdapter extends ArrayAdapter<FreqNode> {
		FreqAdapter(RouteInfoFragment a, FreqNode[] data) {
			super(a.getActivity(), R.layout.rinfo_row, data);
		}

		@Override
		@NonNull
		public View getView(int position, View convertView, @Nullable ViewGroup parent) {

			RouteInfoFragment.ViewHolder holder;
			LayoutInflater inflater = getActivity().getLayoutInflater();
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.rinfo_row, parent, false);
				holder = new RouteInfoFragment.ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (RouteInfoFragment.ViewHolder) convertView.getTag();
			}
			setDetailText(holder, position);
			return convertView;
		}

		void setDetailText(RouteInfoFragment.ViewHolder holder, int position) {
			FreqNode node = this.getItem(position);
			if (node != null) {
				holder.getRinfoTime().setText(node.time);
				holder.getRinfoTimeSlot().setText(node.timeSlot);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
