package jatin.firebasepractice.com.firebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private EditText mposttitle;
    private  EditText mdesp;
    private Button msubmit;
    private Uri mimageuri=null;
    private ProgressDialog mprogress;
    private DatabaseReference mdatabase;
    private static final int Galler_R=1;
    private StorageReference mstorage;
    private FirebaseAuth mauth;
    private FirebaseUser mCurrentuser;
    private DatabaseReference mDatabaseusers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mauth=FirebaseAuth.getInstance();
        mCurrentuser=mauth.getCurrentUser();
        mstorage= FirebaseStorage.getInstance().getReference();
        mdatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseusers=FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentuser.getUid());
        mSelectImage=(ImageButton)findViewById(R.id.imageSelect);
        mposttitle=(EditText)findViewById(R.id.titleField);
        mdesp=(EditText)findViewById(R.id.descField);
        msubmit=(Button)findViewById(R.id.submitbtn);
        mprogress=new ProgressDialog(this);

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Galler_R);

            }
        });
        msubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startposting();

            }
        });
    }

    private void startposting() {

        mprogress.setMessage("Uploading....");
        final String title_val=mposttitle.getText().toString().trim();
        final String desc_val=mdesp.getText().toString().trim();

        if(!TextUtils.isEmpty(title_val) &&!TextUtils.isEmpty(desc_val)&& mimageuri!=null) {

            mprogress.show();
            StorageReference filepath=mstorage.child("Blog_images").child(mimageuri.getLastPathSegment());

            /*
            public static String random() {
    Random generator = new Random();
    StringBuilder randomStringBuilder = new StringBuilder();
    int randomLength = generator.nextInt(MAX_LENGTH);
    char tempChar;
    for (int i = 0; i < randomLength; i++){
        tempChar = (char) (generator.nextInt(96) + 32);
        randomStringBuilder.append(tempChar);
    }
    return randomStringBuilder.toString();
}
*/


            filepath.putFile(mimageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                   final  Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    final DatabaseReference newpost=mdatabase.push();// it means unique id cannot be overwritten


                    mDatabaseusers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            newpost.child("title").setValue(title_val);
                            newpost.child("desc").setValue(desc_val);
                            newpost.child("imageurl").setValue(downloadUrl.toString());
                            newpost.child("uid").setValue(mCurrentuser.getUid());
                            newpost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        Intent i=new Intent(PostActivity.this,MainActivity.class);
                                        i.putExtra("Repeat",1);
                                        finish();
                                        startActivity(i);

                                    }else {
                                        Toast.makeText(PostActivity.this,"Error",Toast.LENGTH_LONG);
                                    }
                                }
                            });
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    mprogress.dismiss();
                }
            });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Galler_R && resultCode==RESULT_OK){
            mimageuri=data.getData();
            mSelectImage.setImageURI(mimageuri);
        }
    }
}
