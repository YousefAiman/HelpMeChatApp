package app.alhamad.helpmechat;


import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseIdService extends FirebaseMessagingService {


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        FirebaseFirestore.getInstance().collection("users").whereEqualTo("id", user.getUid()).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                FirebaseFirestore.getInstance().collection("users").document(snapshots.getDocuments().get(0).getId()).update("token", s).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MyFirebaseIdService.this, "UPDATED", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
