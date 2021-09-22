package app.alhamad.helpmechat;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainFragment extends Fragment implements LocationListener, NumberPicker.OnValueChangeListener {
    static int REQUEST_CHECK_SETTINGS;
    private final int LOCATION_PERMISSION = 1001;
    LinearLayout specialChatBtn;
    LinearLayout aroundmeBtn;
    LinearLayout hideMeBtn;
    LinearLayout getRewardsBtn;
    LocationManager locationManager;
    String provider;
    double latitude;
    double longitude;
    int kilometerRadius;
    Location location1;
    LocationRequest locationRequest;
    LocationCallback mLocationCallback;
    ProgressDialog dialog;
    Intent intent;
    CollectionReference userRef;
    RewardedVideoAd mRewardedVideoAd;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    String country;
    String city;
    private FusedLocationProviderClient mFusedLocationClient;


    public MainFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        specialChatBtn = view.findViewById(R.id.specialChatBtn);
        aroundmeBtn = view.findViewById(R.id.aroundmeBtn);
        getRewardsBtn = view.findViewById(R.id.getRewardsBtn);
        hideMeBtn = view.findViewById(R.id.hideMeBtn);
        MobileAds.initialize(getContext(), initializationStatus -> {
            AdView mAdView = view.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        auth = FirebaseAuth.getInstance();
        userRef = FirebaseFirestore.getInstance().collection("users");

        getRewardsBtn.setOnClickListener(view13 -> {
            ProgressDialog adProgress = ProgressDialog.show(getContext(), getString(R.string.reward_loading),
                    getString(R.string.Please_Wait), true);
            loadRewardedVideoAd();
            mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    adProgress.dismiss();
                    mRewardedVideoAd.show();
                }

                @Override
                public void onRewardedVideoAdOpened() {

                }

                @Override
                public void onRewardedVideoStarted() {

                }

                @Override
                public void onRewardedVideoAdClosed() {

                }

                @Override
                public void onRewarded(RewardItem rewardItem) {
                    userRef.whereEqualTo("id", auth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot ds = task.getResult().getDocuments().get(0);
                            long currentPoints = ds.getLong("points");
                            userRef.document(ds.getId()).update("points", currentPoints + 7).addOnCompleteListener(task12 -> {
                                Toast.makeText(getContext(), getString(R.string.points_added), Toast.LENGTH_LONG).show();

                            }).addOnFailureListener(e -> {
                                Toast.makeText(getContext(), getString(R.string.adding_failed), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), getString(R.string.adding_failed), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onRewardedVideoAdLeftApplication() {
                    adProgress.dismiss();
                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int i) {
                    adProgress.dismiss();
                    Toast.makeText(getContext(), getString(R.string.reward_failed), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRewardedVideoCompleted() {
                }
            });
        });


        specialChatBtn.setOnClickListener(view12 -> {
            DialogFragment dialogFragment = SpecialHelpChatFragment.newInstance();
            dialogFragment.show(getChildFragmentManager(), "special");
        });

        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            }
        }

        aroundmeBtn.setOnClickListener(view1 -> {

            final BottomSheetDialog bsd = new BottomSheetDialog(getContext());
            View parentView = getLayoutInflater().inflate(R.layout.bottomsheetdrawer, null);
            bsd.setContentView(parentView);

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = displaymetrics.heightPixels;

            bsd.setContentView(parentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    height / 4));

            //     bsb.setPeekHeight(h/2);
//            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, getResources().getDisplayMetrics())
            bsd.show();
            parentView.findViewById(R.id.countryBtn).setOnClickListener(v -> {
                bsd.hide();


                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    dialog = ProgressDialog.show(getContext(), getString(R.string.searching),
                            getString(R.string.Please_Wait), true);
                    createAndCheckLocationRequest();

                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                    mLocationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null) {
                                return;
                            }
                            for (Location location : locationResult.getLocations()) {
                                location1 = location;
                            }
                            getActivity().runOnUiThread(() -> {
                                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location1.getLatitude(), location1.getLongitude(), 1);
                                    if (addresses != null && !addresses.isEmpty()) {
                                        country = addresses.get(0).getCountryName();
                                        city = addresses.get(0).getLocality();
                                        updateLocation(country, city);
                                    } else {
                                        fetchFromApi(location1.getLatitude(), location1.getLongitude());
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        }
                    };
                } else {
                    Toast.makeText(getContext(), getString(R.string.gps_disabled), Toast.LENGTH_SHORT).show();
                }
            });

            parentView.findViewById(R.id.kilometerBtn).setOnClickListener(v -> {
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (bsd.isShowing()) {
                        bsd.hide();
                    }
                    show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.gps_disabled), Toast.LENGTH_SHORT).show();
                }

            });
        });

        hideMeBtn.setOnClickListener(view14 -> {

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(getString(R.string.use_points));
            alert.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                userRef.whereEqualTo("id", auth.getCurrentUser().getUid()).limit(1).get().addOnSuccessListener(snapshots -> {
                    DocumentSnapshot ds = snapshots.getDocuments().get(0);
                    long points = ds.getLong("points");
                    if (points >= 50) {
                        DocumentReference dr = userRef.document(ds.getId());
                        dr.update("locationHidden", System.currentTimeMillis() / 1000).addOnSuccessListener(aVoid -> dr.update("points", FieldValue.increment(-50)).addOnSuccessListener(aVoid1 -> Toast.makeText(getContext(), getString(R.string.location_hidden), Toast.LENGTH_SHORT).show()));
                    } else {
                        Toast.makeText(getContext(), getString(R.string.no_enought_hide), Toast.LENGTH_SHORT).show();
                    }
                });
            });
            alert.setNegativeButton(getString(R.string.cancel), null);
            alert.show();
        });
    }


    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        location1 = new Location(provider);
        location1.setLatitude(latitude);
        location1.setLongitude(longitude);
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
        SettingsClient client = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(getActivity(), locationSettingsResponse -> requestLocationUpdate());
        task.addOnFailureListener(getActivity(), e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(getActivity(),
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException ignored) {
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                requestLocationUpdate();
            }
        }
    }

    private void requestLocationUpdate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
            }
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest,
                mLocationCallback,
                null);
    }

    @Override
    public void onDestroy() {

        if (intent != null) {
            getContext().stopService(intent);
        }
        if (mRewardedVideoAd != null) {
            mRewardedVideoAd.destroy(getContext());
        }

        super.onDestroy();
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getContext());
        mRewardedVideoAd.loadAd("ca-app-pub-4861675071646635/1258240928",
                new AdRequest.Builder().build());
    }

    @Override
    public void onPause() {
        if (mRewardedVideoAd != null) {
            mRewardedVideoAd.pause(getContext());
        }
        super.onPause();
    }

    public void fetchFromApi(double latitude, double longitude) {
        String url = "https://api.opencagedata.com/geocode/v1/json?key=078648c6ff684a8e851e63cbb1c8f6d8&q=" + latitude + "+" + longitude + "&pretty=1&no_annotations=1";

        RequestQueue queue = Volley.newRequestQueue(getContext());
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
                    updateLocation(country, city);
                    dialog.dismiss();
                }
            } catch (JSONException e) {
                Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        });

        queue.add(jsonObjectRequest);

        queue.start();
    }

    public void updateLocation(String country, String city) {
        userRef.whereEqualTo("id", auth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
            String documentId = task.getResult().getDocuments().get(0).getId();
            final DocumentReference documentReference = userRef.document(documentId);
            documentReference.update("country", country).addOnCompleteListener(task14 -> {
                documentReference.update("latitude", location1.getLatitude()).addOnCompleteListener(task13 -> documentReference.update("longitude", location1.getLongitude()).addOnCompleteListener(task12 -> {

                    FirebaseFirestore.getInstance().collection("users").whereEqualTo("country", country).get()
                            .addOnCompleteListener(task15 -> {
                        List<User> users = new ArrayList<>();
                        if (task15.isSuccessful()) {
                            if (!task15.getResult().isEmpty()) {
                                for (DocumentSnapshot ds : task15.getResult().getDocuments()) {
                                    if (ds.getString("id").equals(auth.getCurrentUser().getUid()))
                                        continue;
                                    users.add(ds.toObject(User.class));
                                }
                                dialog.dismiss();
                                Intent newIntent = new Intent(getContext(), NearMeChattersActivity.class);
                                newIntent.putExtra("users", (Serializable) users);
                                newIntent.putExtra("area", country);
                                startActivity(newIntent);
                            }
                        }

                        documentReference.update("city", city).addOnCompleteListener(task1 -> dialog.dismiss());
                    });

                })).addOnFailureListener(e -> dialog.dismiss());


            }).addOnFailureListener(e -> Toast.makeText(getContext(), getString(R.string.error_occurred), Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        kilometerRadius = numberPicker.getValue();
    }

    public void show() {

        final Dialog d2 = new Dialog(getActivity());
        d2.setContentView(R.layout.number_picker);
        Button searchBtn = d2.findViewById(R.id.button1);
        Button cancelBtn = d2.findViewById(R.id.button2);
        final NumberPicker np = d2.findViewById(R.id.numberPicker1);
        np.setMaxValue(100);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        searchBtn.setOnClickListener(view -> {
            final ArrayList<User> users = new ArrayList<>();
            dialog = ProgressDialog.show(getContext(), getString(R.string.search_in) + " " + np.getValue() + " " + getString(R.string.kilometer),
                    getString(R.string.Please_Wait), true);
            createAndCheckLocationRequest();
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        dialog.dismiss();
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        location1 = location;
                    }
                    final String currentId = auth.getCurrentUser().getUid();
                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                    userRef.whereEqualTo("id", currentId).limit(1).get().addOnSuccessListener(snapshots -> {
                        final DocumentReference dc = userRef.document(snapshots.getDocuments().get(0).getId());
                        dc.update("latitude", location1.getLatitude()).addOnSuccessListener(aVoid -> dc.update("longitude", location1.getLongitude()).addOnCompleteListener(task -> userRef.get().addOnSuccessListener(snapshots1 -> {
                            for (DocumentSnapshot documentSnapshot : snapshots1) {
                                if (documentSnapshot.getDouble("latitude") != null && documentSnapshot.getDouble("latitude") != 0 &&
                                        documentSnapshot.getDouble("longitude") != null && documentSnapshot.getDouble("longitude") != 0) {
                                    if (documentSnapshot.getString("id").equals(auth.getCurrentUser().getUid()))
                                        continue;
                                    Location newLocation = new Location("newLocation");
                                    newLocation.setLatitude(documentSnapshot.getDouble("latitude"));
                                    newLocation.setLongitude(documentSnapshot.getDouble("longitude"));
                                    if (location1.distanceTo(newLocation) <= np.getValue() * 1000) {
                                        users.add(documentSnapshot.toObject(User.class));
                                    }

                                }
                            }
                        }).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful() && task1.isComplete()) {
                                dialog.dismiss();
                                if (!users.isEmpty()) {
                                    Intent newIntent = new Intent(getContext(), NearMeChattersActivity.class);
                                    newIntent.putExtra("users", users);
                                    newIntent.putExtra("area", np.getValue() + getString(R.string.kilometer));
                                    startActivity(newIntent);
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.no_users) + np.getValue() + getString(R.string.kilometer), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })));
                    });
                }
            };
            d2.dismiss();
        });
        cancelBtn.setOnClickListener(view -> d2.dismiss());
        d2.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    dialog.dismiss();
                }
            }
        }
    }
}