package jatin.firebasepractice.com.firebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG ="LoginActivity" ;
    private EditText mloginemailfield;
    private  EditText mloginpassordfiled;
    private Button mloginbtn;
    private DatabaseReference mdatabaseusers;
    private FirebaseAuth mauth;
    private ProgressDialog mprogress;
    private SignInButton mgooglebtn;
    private  static final int RC_SIGN_IN=1;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Button mregisterbtn;


    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mauth=FirebaseAuth.getInstance();
        mAuthListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };


        mprogress= new ProgressDialog(this);
        mdatabaseusers= FirebaseDatabase.getInstance().getReference().child("Users");
        mdatabaseusers.keepSynced(true);
        mloginemailfield=(EditText)findViewById(R.id.loginemailfield);
        mloginpassordfiled=(EditText)findViewById(R.id.loginpasswordfield);
        mgooglebtn=(SignInButton)findViewById(R.id.googlebtn);
        mloginbtn=(Button)findViewById(R.id.loginbutton);
        mregisterbtn=(Button)findViewById(R.id.loginsignup);

        mloginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checklogin();

            }
        });
        mregisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        //---------------------------------Google Sign---------------------

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient=new GoogleApiClient.Builder(this)///   check
                      .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                          @Override
                          public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                          }
                      })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        mgooglebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            mprogress.setMessage("Signing In...");
            mprogress.show();
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                mprogress.dismiss();

            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else {


                            mprogress.dismiss();
                            checkuserExist();

                        }
                        // ...
                    }
                });
    }



    private void checklogin() {

        String email=mloginemailfield.getText().toString().trim();
        String password=mloginpassordfiled.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            mprogress.setMessage("Logging In....");
            mprogress.show();
            mauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        mprogress.dismiss();
                        checkuserExist();

                    }else
                    {
                        mprogress.dismiss();
                        Toast.makeText(LoginActivity.this,"Error Login",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void checkuserExist() {

        Toast.makeText(LoginActivity.this,"Checking for Existance",Toast.LENGTH_LONG).show();
        if (mauth.getCurrentUser()!=null) {
            final String user_id = mauth.getCurrentUser().getUid();

            mdatabaseusers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {   //Runs in realtime

                    if (dataSnapshot.hasChild(user_id)) {

                        Toast.makeText(LoginActivity.this,"You Exist...",Toast.LENGTH_LONG).show();

                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(mainIntent);

                    } else {


                        Toast.makeText(LoginActivity.this,"You don't Exist",Toast.LENGTH_LONG).show();
                        Intent Setupintentt = new Intent(LoginActivity.this, SetupActivity.class);
                        Setupintentt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(Setupintentt);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
