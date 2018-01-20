package jatin.firebasepractice.com.firebase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageShow extends AppCompatActivity {

    ImageView single;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);

        single= (ImageView) findViewById(R.id.singleImage);

        Picasso.with(getApplicationContext()).load(getIntent().getStringExtra("url").toString()).into(single);

    }
}
