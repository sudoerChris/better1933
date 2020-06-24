package com.example.better.better1933.Controller;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.better.better1933.R;

public class DBUpadteProgressFragment extends DialogFragment {
    private static final String ARG_COMPANY = "company";

    private String mCompany;


    public DBUpadteProgressFragment() {
        // Required empty public constructor
    }

    public static DBUpadteProgressFragment newInstance(String company) {
        DBUpadteProgressFragment fragment = new DBUpadteProgressFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMPANY, company);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCompany = getArguments().getString(ARG_COMPANY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dbupadte_progress, container, false);
    }

}
