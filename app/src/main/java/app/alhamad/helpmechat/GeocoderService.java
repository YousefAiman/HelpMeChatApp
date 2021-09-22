package app.alhamad.helpmechat;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeocoderService extends IntentService {
    public static final String LOCATION_DATA_EXTRA = "CURRENT_LOCATION_LOCATION_DATA_EXTRA";
    public static final String RECEIVER = "CURRENT_LOCATION_RECEIVER";
    public static final int FAILURE_RESULT = 0;
    public static final int SUCCESS_RESULT = 1;
    private ResultReceiver mReceiver;


    public GeocoderService() {
        super("GeocoderService");
    }

    public GeocoderService(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(RECEIVER);
        List<Address> addresses = null;
        String errorMessage = "";

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException | IllegalArgumentException ioException) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();

        }

        if (addresses == null || addresses.size() == 0) {
            deliverResultToReceiver(FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getCountryName());
            }
            deliverResultToReceiver(SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"), addressFragments));
        }
    }


    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("result", message);
        mReceiver.send(resultCode, bundle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
