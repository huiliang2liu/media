package com.lhl.media;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.lhl.media.widget.VideoView;

public class MainActivity extends Activity {
    VideoView view;
    private String path = "https://vd3.bdstatic.com/mda-kfdi04dfsgbgxyah/hd/mda-kfdi04dfsgbgxyah.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.video_view);
    }

    public void android(View v) {
        view.pause();
        view.changePlayer(IMedia.MediaType.ANDROID);
        view.play(path);
    }

    public void ijk(View v) {
        view.pause();
        view.changePlayer(IMedia.MediaType.IJK);
        view.play(path);
    }


    public void googleExo(View v) {
        view.pause();
        view.changePlayer(IMedia.MediaType.GOOGLE_EXO);
        view.play(path);
    }
}