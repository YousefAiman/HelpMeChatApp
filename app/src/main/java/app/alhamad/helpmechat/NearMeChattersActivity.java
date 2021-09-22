package app.alhamad.helpmechat;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NearMeChattersActivity extends AppCompatActivity {
    ChattersAdapter adapter;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_me_chatters);
        Toolbar toolbar = findViewById(R.id.toolbar);


        MobileAds.initialize(getApplicationContext(), initializationStatus -> {
            AdView mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setTitle(getIntent().getStringExtra("area"));
        ArrayList<User> users = (ArrayList<User>) getIntent().getSerializableExtra("users");
        toolbar.setSubtitle(users.size() +" "+ getString(R.string.user_available));
        adapter = new ChattersAdapter(NearMeChattersActivity.this, users);
        ((RecyclerView)findViewById(R.id.chattersRv)).setAdapter(adapter);

    }


    public void showAdd() {
        RewardedVideoAd mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getApplicationContext());

        ProgressDialog progressDialog = new ProgressDialog(NearMeChattersActivity.this);
        progressDialog.setTitle(getString(R.string.reward_loading)+"\n"+getString(R.string.Please_Wait));
        progressDialog.setCancelable(false);

        mRewardedVideoAd.loadAd("ca-app-pub-4861675071646635/1258240928",
                new AdRequest.Builder().build());

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
                CollectionReference userRef = FirebaseFirestore.getInstance().collection("users");
                userRef.whereEqualTo("id", currentUserId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot ds = task.getResult().getDocuments().get(0);
                        userRef.document(ds.getId()).update("points", ds.getLong("points") + 7).addOnCompleteListener(task1 -> {
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

}
