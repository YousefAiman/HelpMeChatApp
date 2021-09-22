package app.alhamad.helpmechat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OrderInfoActivity extends AppCompatActivity {
    ImageView orderProfileIv;
    ProgressBar progressBar;
    TextView firstLetterMessageTv;
    TextView usernameTv;
    TextView genderTv;
    TextView birthTv;
    TextView titleTv;
    TextView commentsTv;
    TextView contentTv;
    TextView ratingTv;
    TextView timeTv;
    RatingBar ratingRb;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    CollectionReference usersRef;
    CollectionReference ordersRef;
    List<Double> ratings;
    boolean ratingChanged;
    double ratingSum;
    float ratingCalc;
    String ratingDocumentID = "";
    DocumentSnapshot ds;
    ImageView sendTv;
    EditText messageEd;
    DocumentReference dr;
    String currentUserId;
    RecyclerView commentsRv;
    RewardedVideoAd mRewardedVideoAd;
    ProgressDialog progressDialog;
    CommentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);

        orderProfileIv = findViewById(R.id.orderProfileIv);
        progressBar = findViewById(R.id.progressBar);
        firstLetterMessageTv = findViewById(R.id.firstLetterMessageTv);
        usernameTv = findViewById(R.id.usernameTv);
        genderTv = findViewById(R.id.genderTv);
        birthTv = findViewById(R.id.birthTv);
        titleTv = findViewById(R.id.titleTv);
        ratingTv = findViewById(R.id.ratingTv);
        ratingRb = findViewById(R.id.ratingRb);
        contentTv = findViewById(R.id.contentTv);
        timeTv = findViewById(R.id.timeTv);
        commentsTv = findViewById(R.id.commentsTv);
        // replyLayout = findViewById(R.id.replyLayout);
        sendTv = findViewById(R.id.senBtn);
        messageEd = findViewById(R.id.messageEd);
        commentsRv = findViewById(R.id.commentsRv);
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();
        usersRef = firestore.collection("users");
        ordersRef = firestore.collection("orders");

        ImageView backImage = findViewById(R.id.imageView3);
        backImage.setOnClickListener(view -> OrderInfoActivity.super.onBackPressed());

        Toolbar toolbar = findViewById(R.id.toolbar);


        messageEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!messageEd.getText().toString().trim().isEmpty()) {
                    sendTv.setImageResource(R.drawable.ic_send);
                } else {
                    sendTv.setImageResource(R.drawable.ic_send_grey);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        CardView imageLayout = findViewById(R.id.cardView3);
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        int height = displaymetrics.heightPixels;
        imageLayout.getLayoutParams().height = (int) (height / 2.5);

        Intent intent = getIntent();
        ratingRb.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> ratingChanged = fromUser);

        ordersRef.whereEqualTo("orderId", intent.getLongExtra("orderId", 0)).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                ds = snapshots.getDocuments().get(0);
                dr = ordersRef.document(ds.getId());
                Order order = ds.toObject(Order.class);


                contentTv.setText(order.getContent());
                ArrayList<String> comments = order.getComments();
                commentsRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                ArrayList<String> allComments = order.getComments();
                ArrayList<String> newComments = new ArrayList<>();
                commentsTv.setText("Replies (" + newComments.size() + ")");
                //      if(allComments.size() < 10){
                //          adapter = new CommentsAdapter(allComments, getApplicationContext(),order.getUid());
                //      commentsRv.setAdapter(adapter);
                //  }else{

//                            for (int i = 0; i < 10; i++) {
//                                newComments.add(allComments.get(i));
//                            }
                adapter = new CommentsAdapter(allComments, getApplicationContext(), order.getUid());
                commentsRv.setAdapter(adapter);
                //     }


//                commentsRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                    @Override
//                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                        super.onScrollStateChanged(recyclerView, newState);
//                        if (!commentsRv.canScrollVertically(View.SCROLL_AXIS_VERTICAL) && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                            if(newComments.size()<allComments.size()){
//                                if(allComments.size() > 10 && allComments.size() <= 20){
//                                    for (int i = 10; i <= newComments+10; i++) {
//                                        newComments.add(allComments.get(i));
//                                    }
//                                } else if(allComments.size() > 20 && allComments.size() <= 30){
//                                    for (int i = 20; i <= 30; i++) {
//                                        newComments.add(allComments.get(i));
//                                    }
//                                    adapter.notifyDataSetChanged();
//                                }else{
//                                    for (int i = 10; i < allComments.size(); i++) {
//                                        newComments.add(allComments.get(i));
//                                    }
//                                    adapter.notifyDataSetChanged();
//                                }
//                            }
//                          //      getUpdatedPromotions();
//                        }
//                    }
//
//                });

                dr.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        ArrayList<String> newComments = (ArrayList<String>) documentSnapshot.get("comments");


                        for (String comment : newComments) {
                            if (!comments.contains(comment)) {
                                comments.add(comment);
                                adapter.notifyItemInserted(comments.size());
                                commentsTv.setText("Replies: " + comments.size());
                            }
                        }
                    }
                });

                if (order.getUid().equals(currentUserId)) {
                    View view = findViewById(R.id.view);
                    view.setVisibility(View.GONE);
                    messageEd.setVisibility(View.GONE);
                    sendTv.setVisibility(View.GONE);
                    ratingRb.setIsIndicator(true);
                } else {
                    toolbar.inflateMenu(R.menu.order_menu);
                    toolbar.setOnMenuItemClickListener(menuItem -> {

                        switch (menuItem.getItemId()) {
                            case R.id.profileItem:
                                Bundle bundle = new Bundle();
                                bundle.putString("userId", order.getUid());
                                DialogFragment dialogFragment = ProfileFragment.newInstance();
                                dialogFragment.setArguments(bundle);
                                dialogFragment.show(getSupportFragmentManager(), "profile");
                                break;
                            case R.id.messageItem:
                                usersRef.whereEqualTo("id", currentUserId).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot snapshots) {
                                        DocumentSnapshot document = snapshots.getDocuments().get(0);
                                        long currentPoints = document.getLong("points");
                                        if (currentPoints >= 9) {
                                            usersRef.document(document.getId()).update("points", currentPoints - 9).addOnCompleteListener(task11 -> {
                                                if (task11.isSuccessful()) {
                                                    Intent intent = new Intent(OrderInfoActivity.this, MessagingActivity.class);
                                                    intent.putExtra("chatterName", order.getUsername());
                                                    intent.putExtra("chatterId", order.getUid());
                                                    startActivity(intent);
                                                }
                                            });
                                        } else {
                                            AlertDialog.Builder alert = new AlertDialog.Builder(OrderInfoActivity.this);
                                            alert.setTitle(getString(R.string.not_enough) + currentPoints);
                                            alert.setMessage(getString(R.string.not_enough_points));
                                            alert.setPositiveButton(getString(R.string.get_points), (dialogInterface, i) -> {
                                                showAdd();
                                            });
                                            alert.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                            });
                                            alert.create().show();
                                        }
                                    }
                                });
                                break;
                            case R.id.reportItem:
                                if (!order.getReports().contains(currentUserId)) {
                                    dr.update("reports", FieldValue.arrayUnion(currentUserId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(OrderInfoActivity.this, "Order reported successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(OrderInfoActivity.this, "Order reporting failed", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }

                                break;
                        }

                        return true;
                    });
                    ratingRb.setIsIndicator(false);
                }
                String username = order.getUsername();
                usernameTv.setText(username);
                String imageUrl = order.getPhotoUrl();
                if (imageUrl != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    Picasso.get().load(imageUrl).fit().centerInside().into(orderProfileIv, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
                } else {
                    orderProfileIv.setImageResource(R.color.white);
                    firstLetterMessageTv.setText(Character.toString(username.charAt(0)).toUpperCase());
                }


                usersRef.whereEqualTo("id", order.getUid()).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        DocumentSnapshot ds = snapshots.getDocuments().get(0);
                        genderTv.setText(ds.getString("gender"));
                        birthTv.setText(ds.getString("dateOfBirth"));
                    }
                });


                titleTv.setText(order.getTitle() + "");
                timeTv.setText(TimeConvertor.getTimeAgo(order.getPublishTime(), getApplicationContext()));


                dr.collection("ratings").get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    ratings = new ArrayList<>();
                    Iterator<QueryDocumentSnapshot> iterator = queryDocumentSnapshots1.iterator();
                    while (iterator.hasNext()) {
                        //     double currentRating = ;
                        // if (currentRating != 0) {
                        ratings.add(Double.valueOf(iterator.next().get("rating").toString()));
                        //  }
                    }
                    for (int i = 0; i < ratings.size(); i++) {
                        ratingSum += ratings.get(i);
                    }
                    ratingCalc = (float) (ratingSum / ratings.size());
                    ratingTv.setText("(" + ratings.size() + ")");
                    ratingRb.setRating((float) Math.ceil(ratingCalc));
                });

            }
        });
        sendTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!messageEd.getText().toString().isEmpty()) {
                    String message = messageEd.getText().toString();
                    if (!message.contains("|")) {
                        dr.update("comments", FieldValue.arrayUnion(message + "~" + currentUserId + "~" + System.currentTimeMillis() / 1000)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                messageEd.setText("");
                            }
                        });
                    } else {
                        Toast.makeText(OrderInfoActivity.this, "Your reply contains illegal character '|'", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(OrderInfoActivity.this, "You need to type a reply to send!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (ratingChanged) {
            Toast.makeText(this, "RATING CHANGED", Toast.LENGTH_SHORT).show();
            float rating = ratingRb.getRating();
            if (rating > 0) {
                CollectionReference documentReference = dr.collection("ratings");
                if (!documentReference.getId().isEmpty()) {
                    documentReference.whereEqualTo("userid", currentUserId).get().addOnCompleteListener(task1 -> {
                        if (task1.getResult().isEmpty()) {
                            addRatingSubCollection();
                        } else {
                            ratingDocumentID = task1.getResult().getDocuments().get(0).getId();
                            dr.collection("ratings").document(ratingDocumentID).update("rating", rating).addOnSuccessListener(aVoid -> {
                                //  ordersRef.document(ds.getId()).update("rating", FieldValue.increment(ratingRb.getRating()));
                            });
                        }
                    });
                } else {
                    addRatingSubCollection();
                }
            }
        }
        OrderInfoActivity.this.finish();
    }

    public void addRatingSubCollection() {
        PromoRating promoRating = new PromoRating();
        promoRating.setUserid(currentUserId);
        promoRating.setRating(ratingRb.getRating());
        ordersRef.document(ds.getId()).collection("ratings").add(promoRating);
    }


    public void showAdd() {
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getApplicationContext());

        progressDialog = ProgressDialog.show(OrderInfoActivity.this, getString(R.string.reward_loading),
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
                usersRef.whereEqualTo("id", currentUserId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot ds = task.getResult().getDocuments().get(0);
                        final long currentPoints = ds.getLong("points");
                        usersRef.document(ds.getId()).update("points", currentPoints + 7).addOnCompleteListener(task1 -> {
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
}
