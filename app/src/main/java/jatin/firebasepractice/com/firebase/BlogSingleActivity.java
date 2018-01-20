package jatin.firebasepractice.com.firebase;

import android.content.Intent;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogSingleActivity extends AppCompatActivity {

    private ImageButton mblogsingleimage;
    private TextView mblogsingletitle;
    private TextView mblogsingledesc;
    private FirebaseAuth mauth;
    private Button msingleremovebtn;
    private String mpost_key=null;
    private TextView likes;
    private DatabaseReference mDatabase,mDatabaselikes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blog_single);

        mblogsingleimage=(ImageButton)findViewById(R.id.blogsingleimage);
        mblogsingledesc=(TextView)findViewById(R.id.blogsingledesc);
        mblogsingletitle=(TextView)findViewById(R.id.blogsingletitle);
        msingleremovebtn=(Button)findViewById(R.id.removebutton);
        likes=(TextView)findViewById(R.id.nooflikes);
        mauth=FirebaseAuth.getInstance();

        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaselikes=FirebaseDatabase.getInstance().getReference().child("Likes");


        mpost_key=getIntent().getExtras().getString("blog_id");

        mDatabase.child(mpost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mDatabaselikes.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        long k;
                        try {
                            k = (long) dataSnapshot.child(mpost_key).child("count").getValue();
                        }catch (NullPointerException e){
                            k=0;
                        }

                        if(k!=0)
                        likes.setText("Seen : "+k);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                String post_title= (String) dataSnapshot.child("title").getValue();
                final String post_desc=(String) dataSnapshot.child("desc").getValue();
                final String post_image=(String)dataSnapshot.child("imageurl").getValue();
                String post_uid=(String) dataSnapshot.child("uid").getValue();

                mblogsingletitle.setText(post_title);
                mblogsingledesc.setText(post_desc);
                Picasso.with(BlogSingleActivity.this).load(post_image).into(mblogsingleimage);

                mblogsingleimage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent i=new Intent(BlogSingleActivity.this,ImageShow.class);
                        i.putExtra("url",post_image);
                        startActivity(i);
                    }
                });

                if(mauth.getCurrentUser().getUid().equals(post_uid)){

                    msingleremovebtn.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        msingleremovebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase.child(mpost_key).removeValue();
                Intent mainIntent=new Intent(BlogSingleActivity.this, MainActivity.class);
               // mainIntent.putExtra("Repeat",1);
                finish();
                startActivity(mainIntent);

            }
        });
    }
}
