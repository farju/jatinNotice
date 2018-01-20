package jatin.firebasepractice.com.firebase;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private RecyclerView mBloglist;
    private DatabaseReference mdatabase;
    private DatabaseReference mdatabasusers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthlistener;
    private boolean mProcesslike=false;
    private DatabaseReference mdatabaselike;

    FragmentManager manager ;

    int flag=0;

    android.app.FragmentTransaction trans;

//   See last video if want to show only one user post


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        mAuthlistener =new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){

                    Intent loginIntent =new Intent(MainActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(loginIntent);
                }
            }
        };

        if(mAuth==null)
        {
            Intent loginIntent =new Intent(MainActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(loginIntent);
        }


        try {
            Log.d("aezakmisa", mAuth.getCurrentUser().getEmail());
        }catch (NullPointerException e){
            Intent loginIntent =new Intent(MainActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(loginIntent);
        }




        mdatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mdatabasusers=FirebaseDatabase.getInstance().getReference().child("Users");
        mdatabaselike=FirebaseDatabase.getInstance().getReference().child("Likes");
        mdatabasusers.keepSynced(true);
        mdatabase.keepSynced(true);
        mdatabaselike.keepSynced(true);
        mBloglist=(RecyclerView)findViewById(R.id.blog_list);
        mBloglist.setHasFixedSize(true);
        mBloglist.setLayoutManager(new LinearLayoutManager(this));
    }



    protected  void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthlistener);

        FirebaseRecyclerAdapter<Blog,BlogViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                mdatabase)
        {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {


               // final String post_key=getRef(position).toString();
                final String post_key=getRef(position).getKey();
                viewHolder.setTitle(model.getTitle());
              //  viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(),model.getImageurl());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setLikebtn(post_key);

               final String s=model.getUsername();

                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this,s,Toast.LENGTH_LONG).show();
                        Intent singleBlogIntent=new Intent(MainActivity.this,BlogSingleActivity.class);
                        singleBlogIntent.putExtra("blog_id",post_key);
                        startActivity(singleBlogIntent);
                    }
                });

                viewHolder.mlikebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                            mProcesslike=true;
                            mdatabaselike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(mProcesslike) {
                                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                            long k= (long) dataSnapshot.child(post_key).child("count").getValue();
                                            mdatabaselike.child(post_key).child("count").setValue(k-1);
                                            mdatabaselike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                            mProcesslike = false;
                                            //if not make false then problem
                                        } else {

                                            long k=0;
                                            try {
                                                Log.d("Val"," "+dataSnapshot.child(post_key).child("count").getValue());
                                                k = (long) dataSnapshot.child(post_key).child("count").getValue();
                                            }catch (NullPointerException e){
                                                k=0;
                                            }
                                            mdatabaselike.child(post_key).child("count").setValue(k+1);
                                            mdatabaselike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("RandomValue");
                                            mProcesslike = false;
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {


                                }
                            });


                    }
                });

            }
        };
        try {
            mBloglist.setAdapter(firebaseRecyclerAdapter);
        }
        catch (IndexOutOfBoundsException e){

        }
    }


    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View mview;
      //  TextView posttitle;
        ImageButton mlikebtn;

        DatabaseReference mdatabaselike;
        FirebaseAuth mauth;


        public BlogViewHolder(View itemView) {
            super(itemView);
            mview = itemView;
            mlikebtn=(ImageButton)mview.findViewById(R.id.likebtn);

            /*

            posttitle=(TextView)mview.findViewById(R.id.post_title);
            posttitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("MainActivity","Some Text");
                }
            });
            */
            mdatabaselike=FirebaseDatabase.getInstance().getReference().child("Likes");
            mauth=FirebaseAuth.getInstance();
            mdatabaselike.keepSynced(true);
        }


        public void setLikebtn(final String post_key){
            mdatabaselike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(post_key).hasChild(mauth.getCurrentUser().getUid())){

                        mlikebtn.setImageResource(R.mipmap.ic_done_all_black_24dp);

                    }else {
                        mlikebtn.setImageResource(R.drawable.ic_remove_red_eye_black_24dp);
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title) {

            TextView posttitle=(TextView)mview.findViewById(R.id.post_title);
            posttitle.setText(title);
        }
/*
        public void setDesc(String desc) {

            TextView postDesc=(TextView)mview.findViewById(R.id.post_descp);
            postDesc.setText(desc);
        }
        */
        public  void setUsername(String username){

            TextView post_username=(TextView)mview.findViewById(R.id.post_username);
            post_username.setText(username);

        }

        public  void setImage(Context ctx,String imageurl) {

            ImageView postimage=(ImageView)mview.findViewById(R.id.post_image);
            Picasso.with(ctx).load(imageurl).into(postimage);

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.action_add){
            finish();
            startActivity(new Intent(MainActivity.this,PostActivity.class));

        }
        if(item.getItemId()==R.id.action_logout){
            logout();
        }

        if(item.getItemId()==R.id.action_settings){

            Intent i=new Intent(MainActivity.this,MyPost.class);
            startActivity(i);

        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {

        mAuth.signOut();
        finish();
    }
}
