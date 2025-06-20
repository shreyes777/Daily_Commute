package com.example.dailycommute;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class TutorialSteps extends AppCompatActivity {
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
        String  step1text="Choose either Work or Friend or Gym",
                step2text="Type Location Name then set Source and Destination",
                step3text="Type Hour and Minute for Going and Coming from above Places",
                step4text="Save your Route to Notify you",
                step5text="Go to Main Screen and set Minutes to remind you beforehand",
                step6text="Set the pre-program and close the App",


                step1video="android.resource://" + getPackageName() + "/" + R.raw.batman,
                step2video="android.resource://" + getPackageName() + "/" + R.raw.spiderman;


        if(Counter==1)
        {
            text.setText(step1text);
            videoAutoSteps(step1video);
        }
        if(Counter==2)
        {
            text.setText(step2text);
            videoAutoSteps(step2video);
        }
        if(Counter==3)
        {
            text.setText(step3text);
        }
        if(Counter==4)
        {
            text.setText(step4text);
        }
        if(Counter==5)
        {
            text.setText(step5text);
        }
        if(Counter==6)
        {
            text.setText(step6text);
        }
    }

    private void videoAutoSteps(String path)
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