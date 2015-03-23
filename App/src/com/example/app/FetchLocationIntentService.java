package com.example.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

public class FetchLocationIntentService extends IntentService {

	ResultReceiver mReceiver;
	
	public FetchLocationIntentService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public FetchLocationIntentService() {
		super("FetchAddressIntentService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());

		String errorMessage = "";

		// Get the location passed to this service through an extra.
		Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
		mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
		
		List<Address> addresses = null;

		try {
			// In this sample, get just a single address.
			addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
		} catch (IOException ioException) {
			// Catch network or other I/O problems.
			errorMessage = getString(R.string.service_not_avaiable);
			Log.e("MY", errorMessage, ioException);
		} catch (IllegalArgumentException illegalArgumentException) {
			// Catch invalid latitude or longitude values.
			errorMessage = "illegal coordinates";
			Log.e("MY",	errorMessage + ". " + "Latitude = "
						+ location.getLatitude() + ", Longitude = "
						+ location.getLongitude(), illegalArgumentException);
		}

		// Handle case where no address was found.
		if (addresses == null || addresses.size() == 0) {
			if (errorMessage.isEmpty()) {
				errorMessage = "no address found";
				Log.e("MY", errorMessage);
			}
			deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
		} else {
			Address address = addresses.get(0);
			ArrayList<String> addressFragments = new ArrayList<String>();

			// Fetch the address lines using getAddressLine,
			// join them, and send them to the thread.
			for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
				addressFragments.add(address.getAddressLine(i));
			}
			Log.i("MY", "address found");
			deliverResultToReceiver(Constants.SUCCESS_RESULT, TextUtils.join(
					System.getProperty("line.separator"), addressFragments));
		}
	}

	private void deliverResultToReceiver(int resultCode, String message) {
		Bundle bundle = new Bundle();
		bundle.putString(Constants.RESULT_DATA_KEY, message);
		mReceiver.send(resultCode, bundle);

	}

}
