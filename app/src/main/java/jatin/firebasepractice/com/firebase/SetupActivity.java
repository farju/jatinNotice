package jatin.firebasepractice.com.firebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private ImageButton msetupImageBtn;
    private EditText mnamefield;
    private Button msubmitbtn;
    private FirebaseAuth mauth;
    private  Uri mimageuri=null;
    private DatabaseReference mdatabaseusers;
    private StorageReference mstorageimage;
    private ProgressDialog mprogress;

    private  static  final  int Gallery_request=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mauth=FirebaseAuth.getInstance();
        mprogress=new ProgressDialog(this);
        mstorageimage= FirebaseStorage.getInstance().getReference().child("Profile_images");
        mdatabaseusers= FirebaseDatabase.getInstance().getReference().child("Users");
        msetupImageBtn=(ImageButton)findViewById(R.id.imageButton);
        mnamefield=(EditText)findViewById(R.id.setupnamefield);
        msubmitbtn=(Button)findViewById((R.id.submitbutton));

        msubmitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startsetupaccount();
            }
        });

        msetupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryintent=new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent,Gallery_request);

            }
        });
    }

    private void startsetupaccount() {

        final String name=mnamefield.getText().toString().trim();
        final String user_id=mauth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(name) && mimageuri!=null){

            mprogress.setMessage("Finishing Profile...");
            mprogress.show();
            StorageReference filepath=mstorageimage.child(mimageuri.getLastPathSegment());
            filepath.putFile(mimageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloaduri=taskSnapshot.getDownloadUrl().toString();
                    mdatabaseusers.child(user_id).child("name").setValue(name);
                    mdatabaseusers.child(user_id).child("image").setValue(downloaduri);
                    mprogress.dismiss();

                    Intent loginIntent =new Intent(SetupActivity.this,MainActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(loginIntent);

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
                msetupImageBtn.setImageURI(mimageuri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
