package app.alhamad.helpmechat;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends DialogFragment {
    TextView usernameProfileTv;
    ImageView profileImageView;
    TextView emailProfileTv;
    CollectionReference userRef;
    TextView areaProfileTv;
    TextView countryProfileTv;
    ProgressBar imageProgressBar;
    TextView birthProfileTv;
    TextView genderProfileTv;
    CollectionReference orderRef;
    FirebaseFirestore firestore;
    ImageView backImage;

    public ProfileFragment() {
    }

    static ProfileFragment newInstance() {
        return new ProfileFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        usernameProfileTv = view.findViewById(R.id.usernameProfileTv);
        profileImageView = view.findViewById(R.id.profileImageView);
        emailProfileTv = view.findViewById(R.id.emailProfileTv);
        areaProfileTv = view.findViewById(R.id.areaProfileTv);
        countryProfileTv = view.findViewById(R.id.countryProfileTv);
        imageProgressBar = view.findViewById(R.id.imageProgressBar);
        birthProfileTv = view.findViewById(R.id.birthProfileTv);
        genderProfileTv = view.findViewById(R.id.genderProfileTv);
        backImage = view.findViewById(R.id.backImage);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        userRef = firestore.collection("users");
        orderRef = firestore.collection("orders");
        backImage.setOnClickListener(v -> dismiss());
        if (getArguments() != null && getArguments().containsKey("userId")) {
            userRef.whereEqualTo("id", getArguments().getString("userId")).limit(1).get().addOnSuccessListener(snapshots -> {
                User user = snapshots.getDocuments().get(0).toObject(User.class);
                usernameProfileTv.setText(user.getUsername());
                if (user.getImageUrl() != null) {
                    imageProgressBar.setVisibility(View.VISIBLE);
                    Picasso.get().load(user.getImageUrl()).fit().centerInside().into(profileImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            imageProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getContext(), getString(R.string.error_loading_image) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if (user.getLocationHidden() != 0) {
                    if (((System.currentTimeMillis() / 1000) - user.getLocationHidden()) >= 86400) {
                        countryProfileTv.setText(user.getCountry());
                        areaProfileTv.setText(user.getCity());
                    }
                } else {
                    countryProfileTv.setText(getString(R.string.hidden));
                    areaProfileTv.setText(getString(R.string.hidden));
                }
                birthProfileTv.setText(user.getDateOfBirth());
                genderProfileTv.setText(user.getGender());
            });
        }
    }
}
