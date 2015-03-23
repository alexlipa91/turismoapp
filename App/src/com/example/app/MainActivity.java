package com.example.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.example.util.DataManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener,
		OnMarkerClickListener {

	private GoogleApiClient mGoogleApiClient;
	private SlidingUpFragment firstFragment;
	private Location loc;
	private String mAddressOutput;
	private AddressResultReceiver mResultReceiver;
	private GoogleMap myMap;

	// < id, {nome, coord1, coord2, id_city}>
	private Map<Integer, String[]> elements;
	// < marker, city>
	private Map<Marker, String> markerCity;

	class AddressResultReceiver extends ResultReceiver {
		public AddressResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			// Display the address string or an error message sent from the
			// intent service.
			mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
			firstFragment.cityName.setText(mAddressOutput);

			// Show a toast message if an address was found.
			if (resultCode == Constants.SUCCESS_RESULT) {
				showToast("address found");
			}
		}
	}

	private void showToast(String msg) {
		Toast toast = Toast.makeText(this.getApplicationContext(), msg, 1000);
		toast.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ADD SLIDING PANEL
		// Check that the activity is using the layout version with the
		// fragment_container FrameLayout
		if (findViewById(R.id.main_fragment_container) != null) {
			// However, if we're being restored from a previous state, then we
			// don't need to do anything
			if (savedInstanceState != null) {
				return;
			}
			// Create a new Fragment to be placed in the activity layout
			firstFragment = new SlidingUpFragment();
			// In case this activity was started with special instructions from
			// an Intent, pass the Intent's extras to the fragment as arguments
			firstFragment.setArguments(getIntent().getExtras());
			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.main_fragment_container, firstFragment).commit();
		}

		// GET PARAMETERS PASSED TO THE ACTIVITY
		Bundle b = this.getIntent().getExtras();
		loc = (Location) b.get("location");

		// INIT MAP
		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		myMap = mapFragment.getMap();
		mapFragment.getMapAsync(this);

		// BUILD GOOGLE API CLIENT
		this.buildGoogleApiClient();
		/*
		 * // GET LOCATION ADDRESS mResultReceiver = new
		 * AddressResultReceiver(new Handler());
		 * 
		 * // Only start the service to fetch the address if GoogleApiClient is
		 * // connected. if (mGoogleApiClient.isConnected() && mLastLocation !=
		 * null) { startIntentService(); } // If GoogleApiClient isn't
		 * connected, process the user's request by // setting mAddressRequested
		 * to true. Later, when GoogleApiClient // connects, // launch the
		 * service to fetch the address. As far as the user is // concerned,
		 * pressing the Fetch Address button // immediately kicks off the
		 * process of getting the address. mAddressRequested = true; //
		 * updateUIWidgets();
		 */
	}

	@Override
	protected void onStart() {
		super.onStart();
		// if (!mResolvingError) { // more about this later
		mGoogleApiClient.connect();
		// }
	}

	@Override
	protected void onStop() {
		super.onStop();
		mGoogleApiClient.disconnect();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	@Override
	public void onMapReady(GoogleMap map) {
		map.setMyLocationEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);
		map.setOnMarkerClickListener(this);

		map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
			@Override
			public boolean onMyLocationButtonClick() {
				Location l = LocationServices.FusedLocationApi
						.getLastLocation(mGoogleApiClient);
				if (l == null)
					createAlertDialog("attivare gps");
				else {
					Log.v("MY", "sto centrando la mappa");
					myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(l.getLatitude(), l.getLongitude()), 11));
					startIntentService(l);
				}
				return false;
			}
		});

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(loc.getLatitude(), loc.getLongitude()), 11));

		new PopulateMap(this.getApplicationContext()).execute();
	}

	public void createAlertDialog(String s) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(s);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private class PopulateMap extends AsyncTask<Void, String, Void> {

		Context context;

		public PopulateMap(Context c) {
			context = c;
		}

		@Override
		protected Void doInBackground(Void... params) {
			elements = DataManager.getElements(context);
			markerCity = new HashMap<Marker, String>();
			for (int id : elements.keySet())
				publishProgress(elements.get(id));
			return null;
		}

		@Override
		protected void onProgressUpdate(String... a) {
			// a = { name, coord1, coord2, id_city }
			if (!a[3].equals("0")) { // se non è una citta, rosso
				Marker m = myMap.addMarker(new MarkerOptions().position(
						new LatLng(Double.parseDouble(a[1]), Double
								.parseDouble(a[2]))).title(a[0]));
				markerCity.put(m, elements.get(Integer.parseInt(a[3]))[0]);
			} else {
				// altrimenti verde
				myMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(Double.parseDouble(a[1]), Double
										.parseDouble(a[2])))
						.title(a[0])
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnected(Bundle arg0) {
		/*
		 * // Determine whether a Geocoder is available. if
		 * (!Geocoder.isPresent()) { Toast.makeText(this,
		 * "no geocoder avaiable", Toast.LENGTH_LONG) .show(); return; }
		 * 
		 * if (mAddressRequested) { startIntentService(); }
		 */

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	protected void startIntentService(Location loc) {
		mResultReceiver = new AddressResultReceiver(new Handler());
		Intent intent = new Intent(this, FetchAddressIntentService.class);
		intent.putExtra(Constants.RECEIVER, mResultReceiver);
		intent.putExtra(Constants.LOCATION_DATA_EXTRA, loc);
		startService(intent);
	}

	@Override
	public boolean onMarkerClick(Marker m) {
		String cityName = markerCity.get(m);
		if (cityName == null) { // è una città
			firstFragment.cityName.setText(m.getTitle());
			firstFragment.placeName.setText("");
		} else {
			firstFragment.cityName.setText(cityName);
			firstFragment.placeName.setText(m.getTitle());
		}
		// TODO Auto-generated method stub
		return false;
	}
}
