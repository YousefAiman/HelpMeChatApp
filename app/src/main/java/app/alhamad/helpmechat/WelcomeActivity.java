package app.alhamad.helpmechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.SignInButton;
import com.jgabrielfreitas.core.BlurImageView;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        SignInButton googleSigninBtn = findViewById(R.id.googleSigninButton);


        BlurImageView blueImage = findViewById(R.id.blurImage);
        blueImage.setBlur(9);

        googleSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, GoogleSigninActivity.class));
                WelcomeActivity.this.finish();
            }
        });

        findViewById(R.id.signinBtn).setOnClickListener(view -> {
            startActivity(new Intent(WelcomeActivity.this, SigninActivity.class));
            WelcomeActivity.this.finish();
        });
        findViewById(R.id.signupBtn).setOnClickListener(view -> {
            startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
            WelcomeActivity.this.finish();
        });
    }
}
