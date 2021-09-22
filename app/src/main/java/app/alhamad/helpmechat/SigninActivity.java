package app.alhamad.helpmechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.jgabrielfreitas.core.BlurImageView;

public class SigninActivity extends AppCompatActivity  {
    EditText emailEd;
    EditText passwordEd;
    Button signinBtn;
    ProgressDialog dialog;
    FirebaseAuth auth;
    CollectionReference usersRef;
    DocumentReference dr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        BlurImageView blueImage = findViewById(R.id.blurImage);
        blueImage.setBlur(9);

        auth = FirebaseAuth.getInstance();
        emailEd = findViewById(R.id.emailEd);
        passwordEd = findViewById(R.id.passEd);
        signinBtn = findViewById(R.id.signinBtn);
        Intent intent = getIntent();

        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        linearLayout.setOnClickListener(view -> startActivity(new Intent(SigninActivity.this, RegisterActivity.class)));
        CheckBox rememberMe = findViewById(R.id.rememberMe);

        usersRef = FirebaseFirestore.getInstance().collection("users");
        signinBtn.setOnClickListener(view -> {
            final String email = emailEd.getText().toString().trim();
            final String password = passwordEd.getText().toString().trim();
            if (!password.isEmpty() && !email.isEmpty()) {
                dialog = ProgressDialog.show(SigninActivity.this, getString(R.string.signing_in),
                        getString(R.string.Please_Wait), true);
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isComplete() && task.isSuccessful()) {

                        usersRef.whereEqualTo("id", auth.getCurrentUser().getUid()).get().addOnSuccessListener(snapshots -> {
                            DocumentSnapshot ds = snapshots.getDocuments().get(0);
                            dr = usersRef.document(ds.getId());
                            if (!ds.contains("email")) {
                                dr.update("email", email).addOnSuccessListener(aVoid ->
                                        dr.update("password", password).addOnSuccessListener(aVoid1 -> {
                                            if (rememberMe.isChecked()) {
                                                dr.update("remembered", true).addOnCompleteListener(task1 -> {
                                                    updateToken(FirebaseInstanceId.getInstance().getToken());
                                                });
                                            } else {
                                                updateToken(FirebaseInstanceId.getInstance().getToken());
                                            }
                                        })
                                );
                            } else {
                                if (rememberMe.isChecked()) {
                                    dr.update("remembered", true).addOnCompleteListener(task1 -> {
                                        updateToken(FirebaseInstanceId.getInstance().getToken());
                                    });
                                } else {
                                    updateToken(FirebaseInstanceId.getInstance().getToken());
                                }
                            }
                        });
                    }
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(SigninActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }

        });

    }

    public void updateToken(String token) {
        FirebaseUser user = auth.getCurrentUser();
        usersRef.whereEqualTo("id", user.getUid()).limit(1).get().addOnSuccessListener(snapshots ->
                usersRef.document(snapshots.getDocuments().get(0).getId()).update("token", token).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dialog.dismiss();
                        startActivity(new Intent(SigninActivity.this, HomeActivity.class));
                        SigninActivity.this.finish();
                    }
                })
        );
    }


}
