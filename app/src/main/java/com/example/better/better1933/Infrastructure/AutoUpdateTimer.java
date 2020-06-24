package com.example.better.better1933.Infrastructure;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

public class AutoUpdateTimer extends AsyncTask<Void,Void,Void> {
	public interface IAutoUpdateTimerUpdate {
		void onAutoUpdateTimerPostExecute();
		void onAutoUpdateTimerTick();
	}

	private IAutoUpdateTimerUpdate updateInterface;
	private boolean repeat;
	private long sleepms;

	public AutoUpdateTimer(@NonNull IAutoUpdateTimerUpdate updateInterface, long sleepms, boolean repeat) {
		this.updateInterface = updateInterface;
		this.sleepms = sleepms;
		this.repeat = repeat;
	}

	@Override
	protected void onPostExecute(Void o) {
		super.onPostExecute(o);
		if(!isCancelled()) {
			this.updateInterface.onAutoUpdateTimerPostExecute();
		}
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		this.updateInterface.onAutoUpdateTimerTick();
	}

	@Override
	protected Void doInBackground(Void[] objects) {
		try {
			do {
				Thread.sleep(sleepms);
				publishProgress();
			} while (repeat);
		} catch (InterruptedException iex) {
			return null;
		}
		return null;
	}
}
