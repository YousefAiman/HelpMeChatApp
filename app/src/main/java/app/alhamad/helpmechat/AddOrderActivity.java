package app.alhamad.helpmechat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AddOrderActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    CollectionReference usersRef;
    String userDocumentId;
    String city;
    String username;
    String userImageUrl;
    String currentUserId;
    CollectionReference ordersRef;
    long count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        usersRef = firestore.collection("users");
        ordersRef = firestore.collection("orders");
        View orderBtn = findViewById(R.id.view5);
        TextView textView2 = findViewById(R.id.textView2);
        ImageView imageView = findViewById(R.id.imageView);

        EditText orderTitleTv = findViewById(R.id.orderTitleTv);
        EditText orderDescTv = findViewById(R.id.orderDescTv);
        currentUserId = auth.getCurrentUser().getUid();
        ImageView circleImageView = findViewById(R.id.circleImageView);
        usersRef.whereEqualTo("id", currentUserId).limit(1).get().addOnSuccessListener(snapshots -> {
            DocumentSnapshot ds = snapshots.getDocuments().get(0);
            userDocumentId = ds.getId();
            username = ds.getString("username");
            userImageUrl = ds.getString("imageUrl");
            city = ds.getString("city");
            Picasso.get().load(userImageUrl).fit().centerInside().into(circleImageView);
        });

        ImageView backImage = findViewById(R.id.imageView3);
        backImage.setOnClickListener(view -> finish());


        imageView.setOnClickListener(view -> orderBtn.performClick());

        textView2.setOnClickListener(view -> orderBtn.performClick());

        orderBtn.setOnClickListener(view -> {
            String description = orderDescTv.getText().toString();
            String title = orderTitleTv.getText().toString();
            if (!description.isEmpty() && !title.isEmpty() && !city.isEmpty()) {
                final DocumentReference countDocument = ordersRef.document("orderCount");
                countDocument.get().addOnSuccessListener(ds -> count = ds.getLong("count")).addOnCompleteListener(task -> {
                    orderBtn.setClickable(false);


                    Order order = new Order();
                    order.setCity(city);
                    order.setContent(description);
                    order.setTitle(title);
                    order.setFavcount(0);
                    order.setUsername(username);
                    order.setPhotoUrl(userImageUrl);
                    order.setComments(new ArrayList<>());
                    order.setUid(currentUserId);
                    order.setOrderId(count + 1);
                    order.setReports(new ArrayList<>());
                    order.setPublishTime(System.currentTimeMillis() / 1000);

                    ordersRef.add(order).addOnCompleteListener(task12 -> {
                        if (task12.isSuccessful()) {
                            countDocument.update("count", FieldValue.increment(1)).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    AddOrderActivity.super.onBackPressed();
                                    AddOrderActivity.this.finish();
                                    Toast.makeText(AddOrderActivity.this, getString(R.string.order_added), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(e -> Toast.makeText(AddOrderActivity.this, getString(R.string.order_failed), Toast.LENGTH_SHORT).show());
                });

            }
        });

    }

}
