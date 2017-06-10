package com.core.mainscreen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.accelerometerplay.AccelerometerPlayActivity;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;

import com.example.android.accelerometerplay.R;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

// https://developers.facebook.com/docs/facebook-login/android
// https://developers.facebook.com/docs/facebook-login/android/accesstokens
public class MainActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private AccessToken accessToken;
    private ProfileTracker profileTracker;
    private Profile profile;

    private TextView txtProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtProfile = (TextView)findViewById(R.id.profile);
        txtProfile.setText("Hello World");

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        loginWithFB();
    }

    public void loadGame(View view) {
        Intent intent = new Intent(this, AccelerometerPlayActivity.class);

        startActivity(intent);
    }

    private void loginWithFB() {
        LoginManager.getInstance().registerCallback(callbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // App code
                    System.out.println("Login " + loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    // App code
                }

                @Override
                public void onError(FacebookException exception) {
                    // App code
                }
            });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                // set access token using currentAccessToken when it's loaded or set
                if (currentAccessToken != null)
                    System.out.println("Access Token: " + currentAccessToken.getToken());
            }
        };
        accessToken = AccessToken.getCurrentAccessToken();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                profile = currentProfile;

                if (profile != null) {
                    txtProfile.setText(profile.getName());
                    System.out.println("Profile: " + profile.getName());
                }
            }
        };
//
//        txtProfile = (TextView)findViewById(R.id.profile);
//        if (profile != null) {
//            txtProfile.setText(profile.getName());
//        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }
}
