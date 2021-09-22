package app.alhamad.helpmechat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagingActivity extends AppCompatActivity {
    Toolbar toolbar;
    String chattingUsername;
    FirebaseAuth auth;
    ArrayList<String> messages;
    RecyclerView messagesRv;
    Intent intent;
    EditText messageEd;
    FirebaseFirestore firestore;
    CollectionReference userRef;
    CollectionReference messagesRef;
    ImageView sendMessageImageView;
    String currentUserId;
    MessagesAdapter adapter;
    String chatterId;
    LinearLayoutManager manager;
    //    RequestQueue requestQueue;
    APIService apiService;
    boolean notify = false;
    DocumentReference dr;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);


//        requestQueue = Volley.newRequestQueue(getApplicationContext());
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        messagesRv = findViewById(R.id.messagesRv);
        toolbar = findViewById(R.id.toolbar);
        messageEd = findViewById(R.id.messageEd);
        sendMessageImageView = findViewById(R.id.senBtn);
        firestore = FirebaseFirestore.getInstance();
        userRef = firestore.collection("users");
        messagesRef = firestore.collection("messages");
        intent = getIntent();
        auth = FirebaseAuth.getInstance();

        chattingUsername = intent.getStringExtra("chatterName");
        chatterId = intent.getStringExtra("chatterId");

        ImageView backImage = findViewById(R.id.backImage);
        backImage.setOnClickListener(view -> finish());


        TextView appCompatTextView2 = findViewById(R.id.appCompatTextView2);
        TextView appCompatTextView = findViewById(R.id.appCompatTextView);
        appCompatTextView2.setText(chattingUsername);
        //  toolbar.setTitle(chattingUsername);
        toolbar.inflateMenu(R.menu.user_menu);
        ImageView circleImageView = findViewById(R.id.circleImageView);
        sharedPreferences = getSharedPreferences("help", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("messagingscreen", chatterId).apply();

        toolbar.setOnMenuItemClickListener(menuItem -> {


            if (menuItem.getItemId() == R.id.profileItem) {
//                Bundle bundle = new Bundle();
//                bundle.putString("userId", chatterId);
//                DialogFragment dialogFragment = ProfileFragment.newInstance();
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class).putExtra("userId", chatterId));
//                dialogFragment.setArguments(bundle);
//                dialogFragment.show(getSupportFragmentManager(), "profile");
            }
            return true;
        });

        userRef.whereEqualTo("id", chatterId).limit(1).get().addOnCompleteListener(task -> {
            DocumentSnapshot ds = task.getResult().getDocuments().get(0);
            String imageUrl = ds.getString("imageUrl");
            Picasso.get().load(imageUrl).fit().centerInside().into(circleImageView);
            if (imageUrl != null && imageUrl.isEmpty()) {
                circleImageView.setImageResource(R.color.white);
                TextView firstLetterMessageTv = findViewById(R.id.firstLetterMessageTv);
                firstLetterMessageTv.setText(Character.toString(chattingUsername.charAt(0)).toUpperCase());
            }
            dr = userRef.document(ds.getId());
            if (ds.getLong("locationHidden") != null) {
                if (((System.currentTimeMillis() / 1000) - ds.getLong("locationHidden")) < 86400) {
                    appCompatTextView.setText(getString(R.string.hidden));
                } else {
                    appCompatTextView.setText(ds.getString("country") + "-" + ds.getString("city"));
                }
            } else {
                appCompatTextView.setText(ds.getString("country") + "-" + ds.getString("city"));
            }
        });

        currentUserId = auth.getCurrentUser().getUid();
        manager = new LinearLayoutManager(getApplicationContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        messagesRv.setLayoutManager(manager);

        readMessages(currentUserId, chatterId);

        adapter = new MessagesAdapter(messages, currentUserId);
        messagesRv.setAdapter(adapter);

        messageEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!messageEd.getText().toString().trim().isEmpty()) {
                    sendMessageImageView.setImageResource(R.drawable.ic_send);
                } else {
                    sendMessageImageView.setImageResource(R.drawable.ic_send_grey);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        sendMessageImageView.setOnClickListener(view -> {
            notify = true;
            String message = messageEd.getText().toString();
            if (!message.equals("")) {
                sendMessage(currentUserId, chatterId, messageEd.getText().toString());
                messageEd.setText("");
            } else {
                Toast.makeText(MessagingActivity.this, getString(R.string.type_message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readMessages(final String myid, final String userid) {
        messages = new ArrayList<>();

        messagesRef.document(myid + "_" + userid).get().addOnCompleteListener(task -> {

            if (task.getResult().exists()) {
                final DocumentReference documentReference = messagesRef.document(myid + "_" + userid);
                documentReference.get().addOnSuccessListener(documentSnapshot -> {

                    messages.addAll((ArrayList<String>) documentSnapshot.get("messages"));
                    adapter.notifyDataSetChanged();
                    messagesRv.scrollToPosition(messages.size() - 1);

                    documentReference.addSnapshotListener((documentSnapshot12, e) -> {
                        if (documentSnapshot12.exists()) {

                            messages.clear();
                            messages.addAll((ArrayList<String>) documentSnapshot12.get("messages"));
                            adapter.notifyDataSetChanged();
                            messagesRv.scrollToPosition(messages.size() - 1);
                        }
                    });
                });
            } else {

                final DocumentReference documentReference = messagesRef.document(userid + "_" + myid);

                documentReference.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        messages.addAll((ArrayList<String>) documentSnapshot.get("messages"));
                        messagesRv.scrollToPosition(messages.size() - 1);
                        adapter.notifyDataSetChanged();
                        documentReference.addSnapshotListener((documentSnapshot1, e) -> {
                            messages.clear();
                            messages.addAll((ArrayList<String>) documentSnapshot1.get("messages"));
                            adapter.notifyDataSetChanged();
                            messagesRv.scrollToPosition(messages.size() - 1);
                        });
                    }
                });
            }
        }).addOnFailureListener(e -> Toast.makeText(MessagingActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());

    }

    public void sendNotificaiton(String receiver, final String message) {
        userRef.whereEqualTo("id", currentUserId).limit(1).get().addOnSuccessListener(snapshots -> {
            DocumentSnapshot ds = snapshots.getDocuments().get(0);
            String userName = ds.getString("username");
            Data data = new Data(currentUserId, R.mipmap.ic_launcher, userName + ": " + message, "New Message", currentUserId, ds.getString("imageUrl"), userName);
            dr.get().addOnSuccessListener(documentSnapshot -> {
                Sender sender = new Sender(data, documentSnapshot.getString("token"));
                apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        if (response.code() == 2) {
                            if (response.body().success != 1) {
                                Toast.makeText(MessagingActivity.this, "NOT sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MessagingActivity.this, "sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                        Toast.makeText(MessagingActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

    }

    private void sendMessage(final String sender, final String receiver, final String message) {

        //   documentExits = false;

        // sendNotification(currentUserId);
        // if(notify){
        sendNotificaiton(receiver, message);
//            notify =false;
//        }

        messagesRef.document(sender + "_" + receiver).get().addOnCompleteListener(task -> {
            if (task.getResult().exists()) {
                messagesRef.document(sender + "_" + receiver).update("messages", FieldValue.arrayUnion(message + "--" + System.currentTimeMillis() / 1000 + "--" + currentUserId)).addOnFailureListener(e -> Toast.makeText(MessagingActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
            } else {

                messagesRef.document(receiver + "_" + sender).get().addOnCompleteListener(task12 -> {
                    if (task12.getResult().exists()) {
                        messagesRef.document(receiver + "_" + sender).update("messages", FieldValue.arrayUnion(message + "--" + System.currentTimeMillis() / 1000 + "--" + currentUserId)).addOnFailureListener(e -> Toast.makeText(MessagingActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        ArrayList<String> sentmessages = new ArrayList<>();
                        sentmessages.add(message + "--" + System.currentTimeMillis() / 1000 + "--" + sender);
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", sender);
                        hashMap.put("receiver", receiver);
                        hashMap.put("messages", sentmessages);
                        hashMap.put("timsent", System.currentTimeMillis() / 1000);
                        hashMap.put(sender + ":LastSeenMessage", 0);
                        hashMap.put(receiver + ":LastSeenMessage", 0);
                        messagesRef.document(sender + "_" + receiver).set(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful() && task1.isComplete()) {
                                readMessages(sender, receiver);
                                adapter = new MessagesAdapter(messages, currentUserId);
                                messagesRv.setAdapter(adapter);
                            }
                        });
                    }
                });

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit().clear().apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.edit().clear().apply();
    }
}
