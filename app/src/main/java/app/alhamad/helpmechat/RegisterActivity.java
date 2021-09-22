package app.alhamad.helpmechat;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.jgabrielfreitas.core.BlurImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity implements LocationListener {
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    ProgressDialog dialog;
    CollectionReference userRef;
    DatePickerDialog.OnDateSetListener onDateSetListener;
    String date;
    String gender;
    int checkedId;
    String city;
    String country;
    double latitude;
    double longitude;
    LocationManager locationManager;
    FusedLocationProviderClient mFusedLocationClient;
    Location location1;
    LocationRequest locationRequest;
    LocationCallback mLocationCallback;
    String provider;
    Button registerBtn;
    boolean registerWasClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userRef = firestore.collection("users");

        BlurImageView blueImage = findViewById(R.id.blurImage);
        blueImage.setBlur(9);

        EditText usernameEd = findViewById(R.id.usernameEd);
        EditText emailEd = findViewById(R.id.emailEd);
        EditText passwordEd = findViewById(R.id.passEd);
        EditText passwordConfirmEd = findViewById(R.id.confPassEd);
        registerBtn = findViewById(R.id.registerBtn);

        TextView dateTv = findViewById(R.id.dateTv);
        RadioButton maleButton = findViewById(R.id.radioButton);
        RadioButton femaleButton = findViewById(R.id.radioButton2);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            } else {
                enableLoc();
            }
        } else {
            enableLoc();
        }
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        linearLayout.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, SigninActivity.class));
            RegisterActivity.this.finish();
        });

        maleButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (checkedId == R.id.radioButton) {
                maleButton.setChecked(false);
                femaleButton.setChecked(true);
            } else {
                maleButton.setChecked(true);
                femaleButton.setChecked(false);
                checkedId = R.id.radioButton;
            }
        });
        femaleButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (checkedId == R.id.radioButton2) {
                femaleButton.setChecked(false);
                maleButton.setChecked(true);
            } else {
                femaleButton.setChecked(true);
                maleButton.setChecked(false);
                checkedId = R.id.radioButton2;
            }
        });


        onDateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String monthName;
            switch (month) {
                case 1:
                    monthName = "January";
                    break;
                case 2:
                    monthName = "February";
                    break;
                case 3:
                    monthName = "March";
                    break;
                case 4:
                    monthName = "April";
                    break;
                case 5:
                    monthName = "May";
                    break;
                case 6:
                    monthName = "June";
                    break;
                case 7:
                    monthName = "July";
                    break;
                case 8:
                    monthName = "August";
                    break;
                case 9:
                    monthName = "September";
                    break;
                case 10:
                    monthName = "October";
                    break;
                case 11:
                    monthName = "November";
                    break;
                case 12:
                    monthName = "December";
                    break;
                default:
                    monthName = "Invalid month";
                    break;
            }
            date = day + " " + monthName + " " + year;
            dateTv.setText(date);
        };
        dateTv.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DATE);

            DatePickerDialog dialog = new DatePickerDialog(RegisterActivity.this, onDateSetListener, year, month, day);
            dialog.show();
        });
        registerBtn.setOnClickListener(view -> {
            registerWasClicked = true;
            final String username = usernameEd.getText().toString().trim();
            final String email = emailEd.getText().toString().trim();
            final String password = passwordEd.getText().toString().trim();
            final String confirmPassword = passwordConfirmEd.getText().toString().trim();
            if (checkedId != 0) {
                if (checkedId == R.id.radioButton) {
                    gender = "Male";
                } else if (checkedId == R.id.radioButton2) {
                    gender = "Female";
                }
            }
            if (country != null && city != null) {
                if (!password.isEmpty() && !email.isEmpty() && !username.isEmpty() && !confirmPassword.isEmpty() && gender != null && date != null && !date.isEmpty()) {

                    if (password.equals(confirmPassword)) {
                        dialog = ProgressDialog.show(RegisterActivity.this, getString(R.string.Creating_Account),
                                getString(R.string.Please_Wait), true);
                        userRef.whereEqualTo("username", username).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) {
                                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task12 -> {
                                        if (task12.isSuccessful()) {
                                            User user = new User();
                                            user.setId(auth.getUid());
                                            user.setUsername(username);
                                            user.setLatitude(0);
                                            user.setGender(gender);
                                            user.setDateOfBirth(date);
                                            user.setLongitude(0);
                                            user.setCity(city);
                                            user.setEmail(email);
                                            user.setPassword(password);
                                            user.setRemembered(false);
                                            user.setCountry(country);
                                            user.setPoints(25);
                                            userRef.add(user).addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    updateToken(FirebaseInstanceId.getInstance().getToken());
                                                    dialog.dismiss();
                                                    Toast.makeText(this, getString(R.string.created), Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                                    RegisterActivity.this.finish();
                                                }
                                            }).addOnFailureListener(e -> {
                                                dialog.dismiss();
                                                Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    }).addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, getString(R.string.taken), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(RegisterActivity.this, getString(R.string.dont_match), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, getString(R.string.fill), Toast.LENGTH_SHORT).show();
                }
            } else {
                enableLoc();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            location1 = new Location(provider);
            location1.setLatitude(latitude);
            location1.setLongitude(longitude);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getString(R.string.gps_disabled), Toast.LENGTH_SHORT).show();
                enableLoc();
            } else {
                enableLoc();
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void createAndCheckLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(100);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(getApplicationContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(locationSettingsResponse -> mFusedLocationClient.requestLocationUpdates(locationRequest,
                mLocationCallback,
                null));
    }

    public void fetchFromApi(double latitude, double longitude) {
        String url = "https://api.opencagedata.com/geocode/v1/json?key=078648c6ff684a8e851e63cbb1c8f6d8&q=" + latitude + "+" + longitude + "&pretty=1&no_annotations=1";

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, response -> {
            try {
                JSONObject Status = response.getJSONObject("status");

                if (Status.getString("message").equalsIgnoreCase("ok")) {
                    JSONArray Results = response.getJSONArray("results");
                    JSONObject zero = Results.getJSONObject(0);
                    JSONObject address_components = zero.getJSONObject("components");
                    country = address_components.getString("country");
                    if (address_components.has("region")) {
                        city = address_components.getString("region");
                    } else {
                        city = address_components.getString("city");
                    }
                    if (registerWasClicked) {
                        registerBtn.performClick();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        queue.add(jsonObjectRequest);
        queue.start();
    }

    public void returnAddress() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        createAndCheckLocationRequest();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(RegisterActivity.this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    location1 = location;
                }
                Geocoder geocoder = new Geocoder(RegisterActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location1.getLatitude(), location1.getLongitude(), 1);
                    if (!addresses.isEmpty()) {
                        Address a = addresses.get(0);
                        city = a.getLocality();
                        country = a.getCountryName();
                        if (registerWasClicked) {
                            registerBtn.performClick();
                        }
                    } else {
                        fetchFromApi(location1.getLatitude(), location1.getLongitude());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
        };
//        if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
//            } else {
//                mFusedLocationClient.requestLocationUpdates(locationRequest,
//                        mLocationCallback,
//                        null);
//            }
//        } else {
//            mFusedLocationClient.requestLocationUpdates(locationRequest,
//                    mLocationCallback,
//                    null);
//        }


    }

    public void updateToken(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore.getInstance().collection("users").whereEqualTo("id", user.getUid()).limit(1).get().addOnSuccessListener(snapshots -> FirebaseFirestore.getInstance().collection("users").document(snapshots.getDocuments().get(0).getId()).update("token", token));
    }

    private void enableLoc() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new
                LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        task.addOnCompleteListener(task1 -> {
            try {
                LocationSettingsResponse response = task1.getResult(ApiException.class);
                returnAddress();
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(
                                    RegisterActivity.this,
                                    101);
                        } catch (IntentSender.SendIntentException | ClassCastException ignored) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 101:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        returnAddress();
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        Toast.makeText(this, getResources().getString(R.string.gps_disabled), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }
}
