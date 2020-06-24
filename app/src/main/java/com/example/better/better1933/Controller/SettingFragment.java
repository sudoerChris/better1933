package com.example.better.better1933.Controller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.better.better1933.GlobalConst;
import com.example.better.better1933.Infrastructure.KMB.KMBDBAutoUpdate;
import com.example.better.better1933.Infrastructure.KMB.KMBDB;
import com.example.better.better1933.Infrastructure.LocalDataDB;
import com.example.better.better1933.MainActivity;
import com.example.better.better1933.Model.DBValue;
import com.example.better.better1933.R;

import java.util.Date;


public class SettingFragment extends Fragment implements KMBDBAutoUpdate.ResultUpdate {
	public final static String fragmentId = "SettingFragment";
	private KMBDBAutoUpdate kmbdbAutoUpdate = null;

	@Override
	public void KMBDBUpdate(String[] statements) {

		if (statements != null && statements.length > 0) {
			if (getActivity() != null) {
				((MainActivity) getActivity()).writeStatus("更新中", true);
			}
			KMBDB.UpdateDB(statements);
			LocalDataDB.SetDBValue(GlobalConst.DBValueName.LastUpdate, new DBValue(new Date()));
		}
		if (getActivity() != null)
			((MainActivity) getActivity()).writeStatus("新已更", false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.setting_content, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().setTitle("Setting");
		Button clearBookmarkButton = getView().findViewById(R.id.setting_clear_bookmark);
		clearBookmarkButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				LocalDataDB.deleteBookmarks();
				if (getActivity() != null)
					((MainActivity) getActivity()).writeStatus("設已重", false);
			}
		});
		Button updateKMBDBButton = getView().findViewById(R.id.setting_update_KMB_db);
		updateKMBDBButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (kmbdbAutoUpdate != null && !kmbdbAutoUpdate.isCancelled()) {
					kmbdbAutoUpdate.cancel(true);
				}
				DBValue lastUpdate = LocalDataDB.GetDBValue(GlobalConst.DBValueName.LastUpdate);
				kmbdbAutoUpdate = new KMBDBAutoUpdate(SettingFragment.this, (Date) (lastUpdate == null ? "20200616210000" : lastUpdate.GetValue()));
				if (getActivity() != null) {
					((MainActivity) getActivity()).writeStatus("下載中", true);
				}
				kmbdbAutoUpdate.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});

		Button resetDBButton = getView().findViewById(R.id.setting_reset_KMB_db);
		resetDBButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				KMBDB.ResetDB();
				LocalDataDB.SetDBValue(GlobalConst.DBValueName.LastUpdate, new DBValue(new Date(0)));
				if (getActivity() != null) {
					((MainActivity) getActivity()).writeStatus("設已重", false);
				}
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (kmbdbAutoUpdate != null && !kmbdbAutoUpdate.isCancelled()) {
			kmbdbAutoUpdate.cancel(true);
		}
	}
}