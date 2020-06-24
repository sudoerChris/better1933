package com.example.better.better1933.Controller;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.better.better1933.Infrastructure.KMB.KMBDB;
import com.example.better.better1933.MainActivity;
import com.example.better.better1933.Model.*;
import com.example.better.better1933.R;

public class RouteSearchFragment extends Fragment {
	public final static String fragmentId = "RouteSearchFragment";
	private ListView resultListView;
	private ResultAdapter resultListViewAdapter;
	private RouteInfo[] resultList;
	private final RouteSearchFragment searchFragment = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resultList = KMBDB.getSearchRoute("");
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		return inflater.inflate(R.layout.rsearch_content, container, false);

	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		super.onCreate(savedInstanceState);
		getActivity().setTitle(getString(R.string.RouteSearchTitle));
		((MainActivity) getActivity()).statusGone();
		EditText routeInput = getView().findViewById(R.id.rsearch_routeInput);
		resultListView = getView().findViewById(R.id.rsearch_result);
		resultListViewAdapter = new ResultAdapter(this, resultList);
		resultListView.setAdapter(resultListViewAdapter);

		routeInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				resultList = KMBDB.getSearchRoute(s.toString());
				resultListViewAdapter = new ResultAdapter(searchFragment, resultList);
				resultListView.setAdapter(resultListViewAdapter);

			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
		resultListView.setOnItemClickListener(
						new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
								enterRouteDetail(resultListViewAdapter.getItem(position));
							}
						});
		resultListView.setOnItemLongClickListener(
						new AdapterView.OnItemLongClickListener() {
							@Override
							public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
								RouteInfo node = resultListViewAdapter.getItem(position);
								if(node!=null) {
									Fragment fragment = RouteInfoFragment.newInstance(node.route, node.boundSeq, node.serviceType);
									//change fragment
									FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
									fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment).commit();
								}
								return true;
							}
						}
		);
	}

	private void enterRouteDetail(RouteInfo route) {
		//pass parameter to next fragment

		Fragment fragment = RouteDetailFragment.newInstance(route.route, route.boundSeq, route.serviceType);
		//change fragment
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment).commit();

	}


	private class ViewHolder {
		private final View searchRow;
		private TextView route = null, bound = null, serviceType = null;

		ViewHolder(View searchRow) {
			this.searchRow = searchRow;
		}

		TextView getRouteText() {
			if (this.route == null) {
				this.route = searchRow.findViewById(R.id.rsearch_row_route);
			}
			return this.route;
		}

		TextView getBoundText() {
			if (this.bound == null) {
				this.bound = searchRow.findViewById(R.id.rsearch_row_bound);
			}
			return this.bound;
		}

		TextView getServiceTypeText() {
			if (this.serviceType == null) {
				this.serviceType = searchRow.findViewById(R.id.rsearch_row_serviceTypeDesc);
			}
			return this.serviceType;
		}

	}

	private class ResultAdapter extends ArrayAdapter<RouteInfo> {

		ResultAdapter(RouteSearchFragment a, RouteInfo[] data) {
			super(a.getActivity(), R.layout.rsearch_row, data);
		}

		@Override
		@NonNull
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {

			RouteSearchFragment.ViewHolder holder;
			LayoutInflater inflater = getActivity().getLayoutInflater();
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.rsearch_row, parent, false);
				holder = new RouteSearchFragment.ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (RouteSearchFragment.ViewHolder) convertView.getTag();
			}
			setDetailText(holder, position);
			return convertView;
		}

		void setDetailText(RouteSearchFragment.ViewHolder holder, int position) {
			RouteInfo node = this.getItem(position);
			if (node != null) {
				holder.getBoundText().setText(node.bound);
				holder.getRouteText().setText(node.route);
				holder.getServiceTypeText().setText(node.serviceTypeDesc);
			}
		}
	}

	@Override
	public void onStop() {
		((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getRootView().getWindowToken(), 0);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
