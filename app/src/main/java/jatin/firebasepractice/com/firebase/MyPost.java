package jatin.firebasepractice.com.firebase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.HashMap;

public class MyPost extends AppCompatActivity {


    private RecyclerView mBloglist;
    private DatabaseReference mdatabase;
    private DatabaseReference mdatabasusers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthlistener;
    private boolean mProcesslike=false;
    private DatabaseReference mdatabaselike;
    private ImageView profilepic;
    private TextView username;
    private ListView simpleList;
   // int flags[] = {R.drawable.cast_abc_scrubber_control_to_pressed_mtrl_000, R.drawable.cast_abc_scrubber_control_to_pressed_mtrl_000, R.drawable.cast_expanded_controller_actionbar_bg_gradient_light, R.drawable.cast_album_art_placeholder, R.drawable.cast_abc_scrubber_control_to_pressed_mtrl_005, R.drawable.cast_ic_expanded_controller_rewind30};
    ArrayList<String> title=new ArrayList<>();
    ArrayList<String> images=new ArrayList<>();
    ArrayList<String> post_key=new ArrayList<>();

    int i=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        mAuth=FirebaseAuth.getInstance();

        profilepic= (ImageView) findViewById(R.id.profilepic);
        username= (TextView) findViewById(R.id.profilename);

        mAuthlistener =new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){

                    Intent loginIntent =new Intent(MyPost.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        mdatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mdatabasusers=FirebaseDatabase.getInstance().getReference().child("Users");
        mdatabaselike=FirebaseDatabase.getInstance().getReference().child("Likes");
        mdatabasusers.keepSynced(true);
        mdatabase.keepSynced(true);
        mdatabaselike.keepSynced(true);
        simpleList = (ListView) findViewById(R.id.list);


        mdatabasusers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                Log.d("aezakmisa",""+dataSnapshot);
                String uri= (String) dataSnapshot.child("image").getValue();
                String name=(String)dataSnapshot.child("name").getValue();
                username.setText(name);
                Picasso.with(MyPost.this).load(uri).into(profilepic);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot domainSnapshot: dataSnapshot.getChildren()) {
                    // Log.d("aezakmisa"," "+domainSnapshot.getKey());


                    HashMap<String, String> data = (HashMap<String, String>) domainSnapshot.getValue();

                    Log.d("aezakmisa", "" + data.get("uid"));

                    try {
                        if (data.get("uid").equals(mAuth.getCurrentUser().getUid())) {
                            post_key.add(domainSnapshot.getKey());
                            title.add(data.get("title"));
                            images.add(data.get("imageurl"));
                        }
                    }
                catch (NullPointerException exception){

                    }

                }
                CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), title, images);
                simpleList.setAdapter(customAdapter);
                simpleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Toast.makeText(MyPost.this,post_key.get(position),Toast.LENGTH_LONG).show();
                        Intent singleBlogIntent=new Intent(MyPost.this,BlogSingleActivity.class);
                        singleBlogIntent.putExtra("blog_id",post_key.get(position));
                        finish();
                        startActivity(singleBlogIntent);

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public class CustomAdapter extends BaseAdapter {
        Context context;
        ArrayList<String> title;
        ArrayList<String> images;

        int flags[];
        LayoutInflater inflter;

        public CustomAdapter(Context applicationContext, ArrayList<String> title, ArrayList<String> images) {
            this.context = applicationContext;
            this.title=title;
            this.images=images;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return title.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.blog_rowextra, null);
            TextView country = (TextView) view.findViewById(R.id.post_title1);
            ImageView icon = (ImageView) view.findViewById(R.id.post_image1);
            Picasso.with(MyPost.this).load(images.get(i)).into(icon);
            country.setText(title.get(i));
            country.setTextColor(Color.BLACK);
            return view;
        }
    }
}
