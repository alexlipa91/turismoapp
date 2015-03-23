package com.example.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SlidingUpFragment extends Fragment {
	
	TextView cityName;
	TextView placeName;
	SharedPreferences prefs;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		prefs = this.getActivity().getSharedPreferences("com.mycompany.myAppName", Context.MODE_PRIVATE);

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_sliding_up, container, false);

		cityName = (TextView)v.findViewById(R.id.cityNameTextView);
		placeName = (TextView)v.findViewById(R.id.placeNameTextView);
		
		cityName.setText(prefs.getString("default_city", ""));
		placeName.setText("");
		
		return v;
	}
}
