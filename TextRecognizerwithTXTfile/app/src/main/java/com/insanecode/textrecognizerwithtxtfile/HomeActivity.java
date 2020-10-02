package com.insanecode.textrecognizerwithtxtfile;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    androidx.appcompat.widget.Toolbar toolbar;
    ImageView imageHolder;
    Bitmap bitmap;
    EditText text_holder;
    FrameLayout frameLayout;
    Button choose_image_btn, click_image_btn, txt_file_btn, google_search_btn, start_recognition_btn;
    Uri filePath;
    private AdView mAdView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Hooks

        toolbar = findViewById(R.id.toolbar);
        frameLayout = (FrameLayout)findViewById(R.id.Home_frame);
        imageHolder = (ImageView)findViewById(R.id.image_placeholder);
        text_holder = (EditText)findViewById(R.id.text_holder);
        start_recognition_btn = (Button)findViewById(R.id.start_recognizing);
        choose_image_btn = (Button)findViewById(R.id.choose_image_btn);
        click_image_btn = (Button)findViewById(R.id.click_image_btn);
        txt_file_btn = (Button)findViewById(R.id.get_txt_file_btn);
        google_search_btn = (Button)findViewById(R.id.google_search_btn);
        mAdView = findViewById(R.id.adView);

        //Adds
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3484960083569974/7285836801");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        //toolbar
        setSupportActionBar(toolbar);


        //Button listner sections

        imageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
            }
        });



        start_recognition_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectTextFromImage();
            }
        });

        choose_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose_image();
            }
        });


        click_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                click_image();
            }
        });

        txt_file_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                make_txt_file();
            }
        });

        google_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                search_fragment fragment = new search_fragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack("search_fragment");
                fragmentTransaction.add(R.id.Home_frame,fragment).commit();
            }
        });
    }

    private void make_txt_file() {
    }

    private void click_image() {


            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }



    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data!= null && data.getData()!= null){


            filePath = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            }
            catch (IOException e){
                e.printStackTrace();
            }



            imageHolder.setImageBitmap(bitmap);

        }

        else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){

            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            imageHolder.setImageBitmap(bitmap);

        }
    }




        private void choose_image() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent,"Select Image"),1);


    }




    private void detectTextFromImage(){

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();

        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {

                displayTextFromImage(firebaseVisionText);


            }
        });
    }

    private void displayTextFromImage( FirebaseVisionText firebaseVisionText){

        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();

        if(blockList.size() == 0){
            Toast.makeText(this, "No Text Found on the Image", Toast.LENGTH_LONG).show();
        }

        else{

            String text = "";
            for(FirebaseVisionText.Block block: firebaseVisionText.getBlocks()) {

                text = text +""+ block.getText();
                text_holder.setText(text);

            }
        }

    }

}
