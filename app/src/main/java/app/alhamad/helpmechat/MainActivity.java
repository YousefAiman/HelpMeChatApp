package app.alhamad.helpmechat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    CollectionReference usersRef;
    DocumentReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usersRef = FirebaseFirestore.getInstance().collection("users");
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            usersRef.whereEqualTo("id", auth.getCurrentUser().getUid()).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().getDocuments().get(0).contains("password")) {
                            auth.signOut();
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.signin_error), Toast.LENGTH_LONG).show();
                            new Handler().postDelayed(() -> {
                                MainActivity.this.startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                                MainActivity.this.finish();
                            }, 0);
                        } else {
                            updateToken(FirebaseInstanceId.getInstance().getToken());
                        }
                    }
                }
            });
//            auth.signOut();
        } else {
            new Handler().postDelayed(() -> {
                MainActivity.this.startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                MainActivity.this.finish();
            }, 500);
        }
    }

    public void updateToken(String token) {
        FirebaseUser user = auth.getCurrentUser();
        usersRef.whereEqualTo("id", user.getUid()).limit(1).get().addOnSuccessListener(snapshots ->
                usersRef.document(snapshots.getDocuments().get(0).getId()).update("token", token).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        new Handler().postDelayed(() -> {
                            MainActivity.this.startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            MainActivity.this.finish();
                        }, 500);
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
        ).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
