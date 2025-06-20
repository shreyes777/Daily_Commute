package com.example.dailycommute;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class TutorialFeatures extends AppCompatActivity {
    private TextView text;
    private Button next,previous;
    private VideoView video;
    public static int Counter=1;
    String step1url="https://youtube.com/shorts/OgX8DjoxK1M?si=cbVssYqRah5Kt6Fq",
            step2url="https://youtube.com/shorts/bApK2BEX3sY?si=KMeC911ascP4EJaB",
            option1url="https://youtube.com/shorts/bApK2BEX3sY?si=KMeC911ascP4EJaB",
            option2url="https://youtube.com/shorts/OgX8DjoxK1M?si=cbVssYqRah5Kt6Fq";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutorial);
        text=findViewById(R.id.text_steps);
        video=findViewById(R.id.videoview);
        next=findViewById(R.id.next_steps);
        previous=findViewById(R.id.previous_steps);




        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Counter=Counter+1;
                if(Counter>6)
                {
                    Counter=6;
                }
                ViewSetter();
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Counter=Counter-1;
                if(Counter<0)
                {
                    Counter=1;
                }
                ViewSetter();
            }
        });
        ViewSetter();
    }

    private void ViewSetter()
    {
        String  option1text="Search for Locations or Routes on Map also save or load",
                option2text="Public Transport lets you check nearby Bus stops",
                option3text="Commute option lets you check traffic for specified Route",
                option4text="Settings allows you to change theme and check username",
                option1video="android.resource://" + getPackageName() + "/" + R.raw.spiderman,
                option2video="android.resource://" + getPackageName() + "/" + R.raw.batman;


        if(Counter==1)
        {
            text.setText(option1text);
            videoAutoOptions(option1video);
        }
        if(Counter==2)
        {
            text.setText(option2text);
            videoAutoOptions(option2video);
        }
        if(Counter==3)
        {
            text.setText(option3text);
        }
        if(Counter==4)
        {
            text.setText(option4text);
        }
    }

    private void videoAutoOptions(String path)
    {
        String videoPath = path;;
        Uri videoUri = Uri.parse(videoPath);
        video.setVideoURI(videoUri);
        video.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            video.start();
        });
    }
}