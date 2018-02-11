package com.example.alfasunny.homeuser.completed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.alfasunny.homeuser.GlideApp;
import com.example.alfasunny.homeuser.More;
import com.example.alfasunny.homeuser.Profile;
import com.example.alfasunny.homeuser.R;
import com.example.alfasunny.homeuser.backend.DataHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Thread.sleep;

public class AddReward extends AppCompatActivity {
    DataHelper d;
    TextView uidText;
    ImageView qrImage;
    int counter=0;
    volatile boolean stop=false;
    CircleImageView profilePic;
    Drawable loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reward);
        d = DataHelper.getInstance();

        profilePic = (CircleImageView) findViewById(R.id.profilePic);
        loading = profilePic.getDrawable();

        stop=false;

        d.getmAuth().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null) {
                    finish();
                }
            }
        });

        d.getSummary().child(d.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                counter++;
                if(counter>1) finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        uidText = (TextView) findViewById(R.id.uidText);
        uidText.setText(d.getUid());

        qrImage = (ImageView) findViewById(R.id.qrImage);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(d.getUid(), BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Common task for many activities


        TextView name = (TextView) findViewById(R.id.displayName);
        name.setText(d.getName());

        new Thread(new Runnable() {
            Integer earned;
            Integer redeemed;
            Integer totalpoints;
            TextView earnedTxt;
            TextView redeemTxt;
            TextView totalPointsTxt;
            @Override
            public void run() {
                while (true) {
                    try {
                        earned = d.getTotalEarning();
                        earnedTxt = (TextView) findViewById(R.id.totalEarned);

                        redeemed = d.getTotalRedeem();
                        redeemTxt = (TextView) findViewById(R.id.totalRedeemed);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                earnedTxt.setText(getString(R.string.earned_points) + earned.toString());
                                redeemTxt.setText(getString(R.string.redeemed_points) + redeemed.toString());
                            }
                        });


                        sleep(1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        findViewById(R.id.btnHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(getBaseContext(), Home.class);
                startActivity(homeIntent);
            }
        });

        findViewById(R.id.btnNotification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent notificationIntent = new Intent(getBaseContext(), Notifications.class);
                startActivity(notificationIntent);
            }
        });

        findViewById(R.id.btnProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(getBaseContext(), Profile.class);
                startActivity(profileIntent);
            }
        });

        findViewById(R.id.btnMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moreIntent = new Intent(getBaseContext(), More.class);
                startActivity(moreIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        stop=false;

        new Thread(new Runnable() {
            String oldAddress = "";
            String newAddress = "";
            @Override
            public void run() {
                try {

                    while (true) {
                        if(stop) return;

                        newAddress = d.getUserProfilePictureAddress();
                        if (!stop && newAddress != null && newAddress != oldAddress) {
                            runOnUiThread(() -> {
                                GlideApp.with(AddReward.this).load(newAddress).placeholder(loading).listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        newAddress = "";
                                        return true;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                }).into(profilePic);



                            });
                            oldAddress = newAddress;
                        }
                        Thread.sleep(500);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        stop=true;
    }
}
