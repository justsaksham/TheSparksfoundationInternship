package com.example.sparkfoundation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private static final String Tag="FacebookAuthentication";
    private AccessTokenTracker accessTokenTracker;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mFirebaseAuth;
    private TextView textView;
    private ImageView imageView2;
    private LoginButton login_button;
    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private Button btnSignOut;
    private int RC_SIGN_IN = 1;



    private FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth=FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        textView=findViewById(R.id.textView);
        signInButton = findViewById(R.id.sign_in_button);

        imageView2=findViewById(R.id.imageView2);
        login_button = findViewById(R.id.login_button);
        login_button.setPermissions("email","public_profile");
        mCallbackManager = CallbackManager.Factory.create();












        btnSignOut = findViewById(R.id.sign_out_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
                login_button.setVisibility(View.INVISIBLE);
                signInButton.setVisibility(View.INVISIBLE);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleSignInClient.signOut();
               // mFirebaseAuth.signOut();
                Toast.makeText(MainActivity.this,"You are Logged Out",Toast.LENGTH_SHORT).show();
                btnSignOut.setVisibility(View.INVISIBLE);
                login_button.setVisibility(View.VISIBLE);
                signInButton.setVisibility(View.VISIBLE);

// default image add krna h abhi
                updateUI(null);
            }
        });














        login_button.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(Tag,"onsuccess"+loginResult);
                handleFacebookToken(loginResult.getAccessToken());
                signInButton.setVisibility(View.INVISIBLE);
                btnSignOut.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancel() {
                Log.d(Tag,"oncancel");
                signInButton.setVisibility(View.VISIBLE);
                login_button.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(FacebookException error) {
                signInButton.setVisibility(View.VISIBLE);
                login_button.setVisibility(View.VISIBLE);

                Log.d(Tag,"error"+error);

            }
        });
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                  //  Toast.makeText(MainActivity.this,user.getDisplayName().toString(), Toast.LENGTH_LONG).show();

                    if(login_button.getText().equals("Log out") ) {
                      //  Log.d(Tag,"hum bhi h");
                        updateUI(user);
                        signInButton.setVisibility(View.INVISIBLE);
                        btnSignOut.setVisibility(View.INVISIBLE);

                    }
                    else if(signInButton.getVisibility()==View.INVISIBLE)
                        updateUI2(user);
                }
                else{
                  //  updateUI(null);
                    signInButton.setVisibility(View.VISIBLE);
                    login_button.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.INVISIBLE);
                    updateUI(null);


                }
            }
        };
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
              //Logout of facebook
                if(currentAccessToken == null){
                    mFirebaseAuth.signOut();
                    signInButton.setVisibility(View.VISIBLE);

                    updateUI(null);
                }
            }
        };
    }
    private void handleFacebookToken(final AccessToken token){
        Log.d(Tag,"handleFacebookToken"+token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(Tag,"sign in with credential: Successfully");
                    FirebaseUser user=mFirebaseAuth.getCurrentUser();
                    updateUI(user);
                }
                else{
                    Log.d(Tag,"sign in with credential: Unsuccessfully",task.getException());
                    Toast.makeText(MainActivity.this,"error", Toast.LENGTH_LONG).show();
                    updateUI(null);

                }
            }
        });
    }
    private void updateUI(FirebaseUser user){
        if(user!=null){
            textView.setText(user.getDisplayName());
            if(user.getPhotoUrl()!=null){
                String photoUrl = user.getPhotoUrl().toString();
                photoUrl = photoUrl+"?type=large" ;
              //  photoUrl="http://graph.facebook.com/"+user.getUid()+"/picture?type=square";
                imageView2.setVisibility(View.VISIBLE);
                Picasso.get().load(photoUrl).into(imageView2);
              //  Toast.makeText(MainActivity.this,"saksham ", Toast.LENGTH_LONG).show();
              //  Log.d(Tag,"btao mujhe kahan ho tum line 207 in update ui");


            }
            else{
                textView.setText("");
                imageView2.setVisibility(View.INVISIBLE);
               // imageView2.setImageResource(R.drawable.com_facebook_favicon_blue);
            }
        }
        else{
            textView.setText("");
            imageView2.setVisibility(View.INVISIBLE);
           // imageView2.setImageResource(R.drawable.com_facebook_favicon_blue);
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }







    @Override
    protected void onStart() {

        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {

        super.onStop();

        if(authStateListener !=null){
            mFirebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try{

            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
            Toast.makeText(MainActivity.this,"Signed In Successfully",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(acc);
        }
        catch (ApiException e){
            Toast.makeText(MainActivity.this,"Sign In Failed",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount acct) {
        //check if the account is null
        if (acct != null) {
            AuthCredential authCredential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mFirebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mFirebaseAuth.getCurrentUser();
                        updateUI2(user);
                    } else {
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        updateUI2(null);
                    }
                }
            });
        }
        else{
            Toast.makeText(MainActivity.this, "acc failed", Toast.LENGTH_SHORT).show();
        }
    }






    private void updateUI2(FirebaseUser fUser){
        Log.d(Tag,"Update2 is called");

        btnSignOut.setVisibility(View.VISIBLE);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account !=  null){
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personId = account.getId();
            Uri personPhoto = account.getPhotoUrl();
            String photoUrl=personPhoto.toString();
            photoUrl = photoUrl+"?type=large" ;
            //  photoUrl="http://graph.facebook.com/"+user.getUid()+"/picture?type=square";
            imageView2.setVisibility(View.VISIBLE);
            Picasso.get().load(photoUrl).into(imageView2);
            textView.setText(personName);
            Toast.makeText(MainActivity.this,personName + personEmail ,Toast.LENGTH_SHORT).show();
        }

    }
}
