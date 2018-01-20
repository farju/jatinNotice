package jatin.firebasepractice.com.firebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class RegisterActivity extends AppCompatActivity {


    private EditText mnamefield;
    private EditText memailfield;
    private EditText mpasswordfield;

    private Button mRegisterBtn;
    private DatabaseReference mdatabase;
    private ImageButton  profileimage;
    private  Uri mimageuri=null;

    private FirebaseAuth mauth;
    private ProgressDialog mprogress;
    private StorageReference mstorageimage;

    private  static  final  int Gallery_request=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mauth=FirebaseAuth.getInstance();

        mdatabase= FirebaseDatabase.getInstance().getReference().child("Users");


        mnamefield=(EditText)findViewById(R.id.nameField);
        memailfield=(EditText)findViewById(R.id.emailField);
        mpasswordfield=(EditText)findViewById(R.id.passwordfield);
        mRegisterBtn=(Button) findViewById(R.id.signupbtn);
        profileimage=(ImageButton)findViewById(R.id.registerprofile);
        mstorageimage= FirebaseStorage.getInstance().getReference().child("Profile_images");


        mprogress = new ProgressDialog(this);

        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryintent=new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent,Gallery_request);

            }
        });
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                startRegistor();

            }
        });
    }

    private void startRegistor() {

        final String name=mnamefield.getText().toString().trim();
        String email=memailfield.getText().toString().trim();
        String password=mpasswordfield.getText().toString().trim();


        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)&&mimageuri!=null){

            final String[] downloaduri = new String[1];
            mprogress.setMessage("Registering....");
            mprogress.show();
            Log.d("val",""+mimageuri);
            StorageReference filepath=mstorageimage.child(mimageuri.getLastPathSegment());

            filepath.putFile(mimageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    Log.d("aezakmisa",taskSnapshot.getDownloadUrl().toString());
                    downloaduri[0] =taskSnapshot.getDownloadUrl().toString();
                    Log.d("Val",""+downloaduri[0]);
                }

            });

            mauth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){


                        String user_id=mauth.getCurrentUser().getUid();

                        DatabaseReference current_user_db = mdatabase.child(user_id);

                        current_user_db.child("name").setValue(name);
                        current_user_db.child("image").setValue(downloaduri[0]);
                        mprogress.dismiss();
                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(mainIntent);

                    }

                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Gallery_request && resultCode==RESULT_OK){

            Uri imageUri =data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mimageuri = result.getUri();
                profileimage.setImageURI(mimageuri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
