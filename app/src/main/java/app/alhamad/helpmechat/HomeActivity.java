package app.alhamad.helpmechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    CollectionReference userRef = FirebaseFirestore.getInstance().collection("users");
    RewardedVideoAd mRewardedVideoAd;
    ProgressDialog progressDialog;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


//        updateToken(FirebaseInstanceId.getInstance().getToken());
        fragmentManager = getSupportFragmentManager();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        CircleImageView circleImageView = findViewById(R.id.circleImageView);

        FirebaseFirestore.getInstance().collection("users").whereEqualTo("id", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(snapshots -> {
            DocumentSnapshot ds = snapshots.getDocuments().get(0);
            if (ds.getString("imageUrl") != null) {
                Picasso.get().load(ds.getString("imageUrl")).fit().centerInside().into(circleImageView);
            }
        });
        circleImageView.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.orderItem:
                    item.setChecked(true);
                    FragmentTransaction favTransaction = fragmentManager.beginTransaction();
                    favTransaction.replace(R.id.viewPager, new OrdersFragment(), "1").commit();
                    break;
                case R.id.messageItem:
                    item.setChecked(true);
                    FragmentTransaction favTransaction2 = fragmentManager.beginTransaction();
                    favTransaction2.replace(R.id.viewPager, new MessagesFragment(), "2").commit();
                    break;
                case R.id.homeItem:
                    item.setChecked(true);
                    FragmentTransaction favTransaction3 = fragmentManager.beginTransaction();
                    favTransaction3.replace(R.id.viewPager, new MainFragment(), "3").commit();
                    break;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.homeItem);

    }

    public void showAdd() {
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getApplicationContext());

        progressDialog = ProgressDialog.show(HomeActivity.this, getString(R.string.reward_loading),
                getString(R.string.Please_Wait), true);
        loadRewardedVideoAd();

        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
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
                userRef.whereEqualTo("id", currentUserId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot ds = task.getResult().getDocuments().get(0);
                        final long currentPoints = ds.getLong("points");
                        userRef.document(ds.getId()).update("points", currentPoints + 7).addOnCompleteListener(task1 -> {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), getString(R.string.points_added), Toast.LENGTH_LONG).show();

                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), getString(R.string.adding_failed), Toast.LENGTH_SHORT).show();
                        });
                    }
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), getString(R.string.adding_failed), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                progressDialog.dismiss();
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.reward_failed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoCompleted() {
            }
        });
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-4861675071646635/1258240928",
                new AdRequest.Builder().build());
    }

//    public void updateToken(String token) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        FirebaseFirestore.getInstance().collection("users").whereEqualTo("id", user.getUid()).limit(1).get().addOnSuccessListener(snapshots -> FirebaseFirestore.getInstance().collection("users").document(snapshots.getDocuments().get(0).getId()).update("token", token));
//    }
}
