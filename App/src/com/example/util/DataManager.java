package com.example.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class DataManager {

	// returns title,coord1,coord2 as Strings
	public static Map<Integer, String[]> getElements(Context c) {
		// TODO usare sparse array, + efficiente
		Map<Integer, String[]> elements = new HashMap<Integer, String[]>();
		try {
			InputStream is = ConnectionManager.getDataMainFile(c);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			StringTokenizer st;
			while ((line = br.readLine()) != null) {
				st = new StringTokenizer(line, ",");
				int id = Integer.parseInt(st.nextToken()); // id
				// create marker data
				String[] data = new String[4];
				data[0] = st.nextToken(); // nome
				data[1] = st.nextToken(); // coord1
				data[2] = st.nextToken(); // coord2
				data[3] = st.nextToken(); // id_città
				Log.v("MY", id+" , "+Arrays.toString(data));
				elements.put(id, data);
			}
			br.close();
		} catch (IOException e) {
			// TODO
			// You'll need to add proper error handling here
		}
		return elements;
	}

	public static ArrayList<String> getCities(Context c) throws IOException {
		ArrayList<String> cities = new ArrayList<String>();
		AssetManager am = c.getAssets();
		InputStream is = am.open("cities");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			cities.add(line);
		}
		br.close();
		return cities;
	}
}
