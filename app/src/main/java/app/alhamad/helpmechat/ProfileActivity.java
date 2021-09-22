package app.alhamad.helpmechat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    Button logoutBtn;
    FirebaseAuth auth;
    TextView usernameProfileTv;
    ImageView profileImageView;
    TextView emailProfileTv;
    ImageView editImageView;
    Uri newImageUri;
    CollectionReference userRef;
    Query currentUserQuery;
    DocumentSnapshot documentSnapshot;
    StorageReference mStorageRef;
    String imageDownloadUri;
    TextView areaProfileTv;
    TextView pointsProfileTv;
    TextView countryProfileTv;
    ProgressBar imageProgressBar;
    boolean editWasClicked;
    String imageUrl;
    FirebaseStorage storage;
    User initialUserState;
    TextView birthProfileTv;
    TextView genderProfileTv;
    TextView locationProfileTv;
    ImageView locationIv;
    CollectionReference orderRef;
    FirebaseFirestore firestore;
    int editTimes;

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        logoutBtn = findViewById(R.id.logoutBtn);
        usernameProfileTv = findViewById(R.id.usernameProfileTv);
        profileImageView = findViewById(R.id.profileImageView);
        emailProfileTv = findViewById(R.id.emailProfileTv);
        editImageView = findViewById(R.id.editImageView);
        areaProfileTv = findViewById(R.id.areaProfileTv);
        pointsProfileTv = findViewById(R.id.pointsProfileTv);
        countryProfileTv = findViewById(R.id.countryProfileTv);
        imageProgressBar = findViewById(R.id.imageProgressBar);
        birthProfileTv = findViewById(R.id.birthProfileTv);
        genderProfileTv = findViewById(R.id.genderProfileTv);
        locationProfileTv = findViewById(R.id.locationProfileTv);
        locationIv = findViewById(R.id.locationIv);


        firestore = FirebaseFirestore.getInstance();
        userRef = firestore.collection("users");
        orderRef = firestore.collection("orders");

        ImageView backImage = findViewById(R.id.backImage);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (getIntent() != null && getIntent().hasExtra("userId")) {
            // locationLayout.setVisibility(View.INVISIBLE);
            logoutBtn.setVisibility(View.INVISIBLE);
            editImageView.setVisibility(View.INVISIBLE);
//            pointsLayout.setVisibility(View.GONE);
//            emailLinear.setVisibility(View.GONE);

            userRef.whereEqualTo("id", getIntent().getStringExtra("userId")).limit(1).get().addOnSuccessListener(snapshots -> {
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
                            Toast.makeText(getApplicationContext(), getString(R.string.error_loading_image) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if (user.getLocationHidden() != 0) {
                    if (((System.currentTimeMillis() / 1000) - user.getLocationHidden()) < 86400) {
//                        cityLayout.setVisibility(View.GONE);
//                        countryLayout.setVisibility(View.GONE);

                        countryProfileTv.setText(getResources().getString(R.string.hidden));
                        areaProfileTv.setText(getResources().getString(R.string.hidden));
                    } else {
                        countryProfileTv.setText(user.getCountry());
                        areaProfileTv.setText(user.getCity());

                    }
                } else {
                    countryProfileTv.setText(user.getCountry());
                    areaProfileTv.setText(user.getCity());
                }


                birthProfileTv.setText(user.getDateOfBirth());
                genderProfileTv.setText(user.getGender());

            });


        } else {
            auth = FirebaseAuth.getInstance();
            storage = FirebaseStorage.getInstance();
            mStorageRef = storage.getReference();
            currentUserQuery = userRef.whereEqualTo("id", auth.getCurrentUser().getUid()).limit(1);
            currentUserQuery.get().addOnCompleteListener(task -> {
                documentSnapshot = task.getResult().getDocuments().get(0);
                initialUserState = documentSnapshot.toObject(User.class);
                usernameProfileTv.setText(initialUserState.getUsername());
                if (initialUserState.getImageUrl() != null) {
                    imageProgressBar.setVisibility(View.VISIBLE);
                    profileImageView.setClickable(false);
                    imageUrl = initialUserState.getImageUrl();
                    Picasso.get().load(imageUrl).fit().centerInside().into(profileImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            imageProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_loading_image) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
//                else {
//                    editImageView.setVisibility(View.GONE);
//                }
                emailProfileTv.setText(auth.getCurrentUser().getEmail());
                String country = initialUserState.getCountry();
                String city = initialUserState.getCity();

                if (initialUserState.getLocationHidden() != 0) {
                    if (((System.currentTimeMillis() / 1000) - initialUserState.getLocationHidden()) < 86400) {
                        locationProfileTv.setText(getString(R.string.hidden));
                        locationIv.setImageResource(R.drawable.ic_location);
                    } else {
                        locationIv.setImageResource(R.drawable.ic_location_on);
                        locationProfileTv.setText(getString(R.string.location_show));
                        if (!country.isEmpty()) {
                            countryProfileTv.setText(country);
                        } else {
                            countryProfileTv.setText(getString(R.string.Unknown));
                        }
                        if (!city.isEmpty()) {
                            areaProfileTv.setText(city);
                        } else {
                            areaProfileTv.setText(getString(R.string.Unknown));
                        }
                    }
                } else {
                    locationIv.setImageResource(R.drawable.ic_location_on);
                    locationProfileTv.setText(getString(R.string.location_show));
                    if (!country.isEmpty()) {
                        countryProfileTv.setText(country);
                    } else {
                        countryProfileTv.setText(getString(R.string.Unknown));
                    }
                    if (!city.isEmpty()) {
                        areaProfileTv.setText(city);
                    } else {
                        areaProfileTv.setText(getString(R.string.Unknown));
                    }
                }

                long oldPoints = initialUserState.getPoints();
                pointsProfileTv.setText(oldPoints + "");
                birthProfileTv.setText(initialUserState.getDateOfBirth());
                genderProfileTv.setText(initialUserState.getGender());
                currentUserQuery.addSnapshotListener((snapshots, e) -> {
                    long points = snapshots.getDocuments().get(0).getLong("points");
                    if (points != oldPoints) {
                        pointsProfileTv.setText(points + "");
                    }
                });
            });

            logoutBtn.setOnClickListener(view13 -> {
                auth.signOut();
                Toast.makeText(getApplicationContext(), getString(R.string.signed_out), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                finish();
            });

            profileImageView.setOnClickListener(view12 -> {
                openGallery();
            });

            editImageView.setOnClickListener(view1 -> {
                editTimes += 1;
                editWasClicked = true;
                openGallery();
            });
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_IMAGE);
                }
            }
        }
    }

    public void openGallery() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == PICK_IMAGE && data != null && data.getData() != null) {

            imageProgressBar.setVisibility(View.VISIBLE);
            newImageUri = data.getData();
            profileImageView.setClickable(false);
            editImageView.setVisibility(View.VISIBLE);
            Bitmap imageBitmap1 = null;
            try {
                imageBitmap1 = decodeUri(getApplicationContext(), newImageUri, 250);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            profileImageView.setBackground(null);
            profileImageView.setImageBitmap(imageBitmap1);

            final StorageReference ref = mStorageRef.child("images/" + UUID.randomUUID().toString());

            if (imageDownloadUri != null) {
                storage.getReferenceFromUrl(imageDownloadUri).delete();
            }
            ref.putFile(newImageUri).addOnCompleteListener(task ->
                    ref.getDownloadUrl().addOnSuccessListener(uri -> imageDownloadUri = uri.toString())
                            .addOnCompleteListener(task12 -> userRef.document(documentSnapshot.getId())
                                    .update("imageUrl", imageDownloadUri).addOnSuccessListener(aVoid -> {
                                        imageProgressBar.setVisibility(View.GONE);
                                        if (editWasClicked) {
                                            if (imageUrl != null) {
                                                storage.getReferenceFromUrl(imageUrl).delete().addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(getApplicationContext(), getString(R.string.picture_updated), Toast.LENGTH_SHORT).show();
                                                });
                                            }
                                        } else {
                                            imageProgressBar.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(), getString(R.string.picture_added), Toast.LENGTH_SHORT).show();
                                        }

                                        orderRef.whereEqualTo("uid", auth.getCurrentUser().getUid()).get().addOnSuccessListener(snapshots -> {
                                            for (DocumentSnapshot snapshot : snapshots) {
                                                orderRef.document(snapshot.getId()).update("photoUrl", imageDownloadUri);
                                            }
                                        });

                                    })));
        }
    }


}
