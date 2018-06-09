package com.example.better.better1933;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.example.better.better1933.Infrastructure.LocalDataDBReader;
import com.example.better.better1933.Model.DBStopInfoNode;

import java.util.Locale;

public class BookmarkEditFragment extends DialogFragment {
	final public static String fragmentId = "BookmarkEditFragment";
    private EditText alarmMin;
    private Switch notifySwitch;
    private DBStopInfoNode node;
    public static BookmarkEditFragment newInstance(DBStopInfoNode node){
        BookmarkEditFragment newFragment = new BookmarkEditFragment();
        newFragment.node = node;
        return newFragment;
    }
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TextView route, bound, stop;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle("Bookmark");
        final View view = inflater.inflate(R.layout.bkmark_edit_content, null);
        builder.setView(view);
        route = (TextView) view.findViewById(R.id.route);
        route.setText(node.route);
        bound = (TextView) view.findViewById(R.id.bound);
        bound.setText(node.bound);
        stop = (TextView) view.findViewById(R.id.stop);
        stop.setText(node.stop);
        alarmMin = (EditText)view.findViewById(R.id.alarm_min);
        if(node.alarmMin>=0){
        	alarmMin.setText(String.format(Locale.US,"%d", node.alarmMin));
        }
        notifySwitch = (Switch) view.findViewById(R.id.notify_switch);
        notifySwitch.setChecked(node.notify);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(alarmMin.getText().length()>0) {
                    node.alarmMin = Integer.parseInt(alarmMin.getText().toString());
                }
                node.notify = notifySwitch.isChecked();
	            LocalDataDBReader dbReader = new LocalDataDBReader(getActivity());
	            dbReader.addBookmark(node);
	            dbReader.close();
                Intent notifyServiceIntent = new Intent(getActivity(), NotifyService.class);
                getActivity().stopService(notifyServiceIntent);
                getActivity().startService(((MainActivity)getActivity()).notifyServiceIntent);
	            BookmarkEditFragment.this.getDialog().cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                BookmarkEditFragment.this.getDialog().cancel();
                if(getTargetFragment()!=null) {
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                }
            }
        });
        return builder.create();
    }
}