package com.example.better.better1933;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.better.better1933.Infrastructure.KmbDBReader;
import com.example.better.better1933.Infrastructure.LocalDataDBReader;



public class SettingFragment extends Fragment {
    final static String fragmentId = "SettingFragment";


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
        Button clearBookmarkButton = (Button) getView().findViewById(R.id.setting_clear_bookmark);
        clearBookmarkButton.setOnClickListener( new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                LocalDataDBReader db = new LocalDataDBReader(getContext());
                db.deleteBookmarks();
                db.close();
                if(getActivity()!=null)
                    ((MainActivity)getActivity()).writeStatus("設已重",false);
            }
        });


        Button resetDBButton = (Button) getView().findViewById(R.id.setting_reset_db);
        resetDBButton.setOnClickListener( new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
            KmbDBReader db = new KmbDBReader(((MainActivity)getActivity()).context);
            db.resetDB();
            db.close();
                if(getActivity()!=null)
                    ((MainActivity)getActivity()).writeStatus("新已更",false);
            }
        });

    }

}
