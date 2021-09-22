package app.alhamad.helpmechat;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SpecialHelpChatFragment extends DialogFragment implements PlaceSelectionListener {
    private static View view;
    PlacesClient placesClient;
    List<Place.Field> placeFields;
    AutocompleteSupportFragment autocompleteSupportFragment;
    ProgressDialog dialog;

    public SpecialHelpChatFragment() {
    }

    static SpecialHelpChatFragment newInstance() {
        return new SpecialHelpChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), "AIzaSyCIT3LahI8uKK0qLHVlZJ03oYZh2GWPS_E");
            placesClient = Places.createClient(getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_special_help_chat, container, false);
        } catch (InflateException ignored) {
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        placeFields = Collections.singletonList(Place.Field.NAME);
        autocompleteSupportFragment = (AutocompleteSupportFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        final Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, placeFields)
                .setHint(getString(R.string.city_hint))
                .setTypeFilter(TypeFilter.CITIES)
                .build(getContext());
        startActivityForResult(intent, 2);


        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                dialog = ProgressDialog.show(getContext(), getString(R.string.searching),
                        getString(R.string.Please_Wait), true);
                //   Place place = Autocomplete.getPlaceFromIntent(intent);
                FirebaseFirestore.getInstance().collection("users").whereEqualTo("city", place.getName()).get().addOnCompleteListener(task -> {
                    List<User> users = new ArrayList<>();
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot ds : task.getResult().getDocuments()) {
                                users.add(ds.toObject(User.class));
                            }

                            Intent newIntent = new Intent(getContext(), NearMeChattersActivity.class);
                            newIntent.putExtra("users", (Serializable) users);
                            newIntent.putExtra("area", place.getName());
                            dialog.dismiss();
                            startActivity(newIntent);
                        }
                    }
                });
            }

            @Override
            public void onError(@NonNull Status status) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Toast.makeText(getContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == AutocompleteActivity.RESULT_OK) {

            Place place = Autocomplete.getPlaceFromIntent(intent);
            String placeName = place.getName();
            dialog = ProgressDialog.show(getContext(), getString(R.string.searching),
                    getString(R.string.Please_Wait), true);
            //   Place place = Autocomplete.getPlaceFromIntent(intent);
            FirebaseFirestore.getInstance().collection("users").whereEqualTo("city", placeName).get().addOnCompleteListener(task -> {
                List<User> users = new ArrayList<>();
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (DocumentSnapshot ds : task.getResult().getDocuments()) {
                            users.add(ds.toObject(User.class));
                        }
                        Intent newIntent = new Intent(getContext(), NearMeChattersActivity.class);
                        newIntent.putExtra("users", (Serializable) users);
                        newIntent.putExtra("area", place.getName());
                        dialog.dismiss();
                        startActivity(newIntent);
                    } else {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "No Users Found in " + placeName, Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(e -> Log.d("ttt", e.getMessage()));

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(intent);
            Log.d("ttt", status.getStatusMessage());
        }

    }


    @Override
    public void onPlaceSelected(@NonNull Place place) {

//        dialog = ProgressDialog.show(getContext(), getString(R.string.searching),
//                getString(R.string.Please_Wait), true);
//        //   Place place = Autocomplete.getPlaceFromIntent(intent);
//        FirebaseFirestore.getInstance().collection("users").whereEqualTo("city", place.getName()).get().addOnCompleteListener(task -> {
//            List<User> users = new ArrayList<>();
//            if (task.isSuccessful()) {
//                if (!task.getResult().isEmpty()) {
//                    for (DocumentSnapshot ds : task.getResult().getDocuments()) {
//                        users.add(ds.toObject(User.class));
//                    }
//
//                    Intent newIntent = new Intent(getContext(), NearMeChattersActivity.class);
//                    newIntent.putExtra("users", (Serializable) users);
//                    newIntent.putExtra("area", place.getName());
//                    dialog.dismiss();
//                    startActivity(newIntent);
//                }
//            }
//        }).addOnFailureListener(e -> {
//            dialog.dismiss();
//            Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//        });
//

    }

    @Override
    public void onError(@NonNull Status status) {
        if (dialog != null) dialog.dismiss();
        Toast.makeText(getContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
    }
}
