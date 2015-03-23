package com.example.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.util.DataManager;

public class IntroActivity extends Activity {

	SharedPreferences prefs;
	AutoCompleteTextView autoCompView;
	ProgressBar pb;
	Button button;
	String selectedCity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		prefs = getSharedPreferences("com.mycompany.myAppName", MODE_PRIVATE);
		// code first run
		if ( prefs.getBoolean("firstrun", true) ) {
			new FirstRun().execute();
			prefs.edit().putBoolean("firstrun", false).commit();
		}

		setContentView(R.layout.activity_intro);

		pb = (ProgressBar) this.findViewById(R.id.progressBar1);
		pb.setVisibility(View.INVISIBLE);

		ArrayList<String> items = null;
		try {
			items = DataManager.getCities(this.getApplicationContext());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		autoCompView = (AutoCompleteTextView) findViewById(R.id.autocompleteTV);
		autoCompView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, items) );

		String x = prefs.getString("default_city", "");
		autoCompView.setText(x);
	}
	
	private class FirstRun extends AsyncTask<Void,Void,Void> {
		@Override
		protected Void doInBackground(Void... params) {
			return null;
			//DataManager.firstRun(c);

		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	public void onClick(View v) {
		Log.v("MY", "onClickButton called");

		selectedCity = autoCompView.getText().toString();
		Log.v("MY", "fetchloc launching called");
		new FetchLocation(this.getApplicationContext()).execute(selectedCity);
		Log.v("MY", "after fetchloc called");

		
	}

	private class FetchLocation extends AsyncTask<String, String, Location> {
		Context context;

		public FetchLocation(Context c) {
			context = c;
		}

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Location loc) {
			pb.setVisibility(View.INVISIBLE);
			
			if ( loc != null ) { //
				Log.v("MY", "loc not null");

				// salva città
				prefs.edit().putString("default_city", selectedCity).commit();
				
				// vai avanti
				Intent intent = new Intent(IntroActivity.this, MainActivity.class);
				intent.putExtra("location", loc);
				IntroActivity.this.startActivity(intent);			
				IntroActivity.this.finish();
			}
		}

		@Override
		protected Location doInBackground(String... params) {
			Log.v("MY", Thread.currentThread().getName());

			Geocoder coder = new Geocoder(context); 
			List<Address> addresses = null;
			String errorMessage = "";
			Location loc = null;

			try {
				// In this sample, get just a single address.
				addresses = coder.getFromLocationName(params[0]+", Italia", 1);
			} catch (IOException ioException) {
				Log.v("MY", "io exception");

				// Catch network or other I/O problems.
				errorMessage = getString(R.string.service_not_avaiable);
				publishProgress(errorMessage,"noserv");
				return null;
			}

			// Handle case where no address was found.
			if (addresses == null || addresses.size() == 0
					|| !addresses.get(0).getCountryCode().equals("IT")) {
				errorMessage = "no address found with the given name";
				publishProgress(errorMessage,"notfound");
				Log.e("MY", errorMessage);
			} else {
				Address address = addresses.get(0);
				loc = new Location("");
				loc.setLatitude(address.getLatitude());
				loc.setLongitude(address.getLongitude());
			}
			return loc;
		}

		@Override
		protected void onProgressUpdate(String... params) {
			Log.v("MY", Thread.currentThread().getName());

			String code = params[1];
			String errorMessage = params[0];
			if ( code.equals("notfound") ) 	
				autoCompView.setText("");
			createAlertDialog(errorMessage);
		}
	}

	public void createAlertDialog(String s) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(s);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.intro, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
