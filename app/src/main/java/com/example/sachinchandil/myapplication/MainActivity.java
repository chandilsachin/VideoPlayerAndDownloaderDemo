package com.example.sachinchandil.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    private Button btnPlay;
    private MediaPlayer player;
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        outFilePath = getExternalFilesDir("/") + "/video.mp4";
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        prepareVideoView();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission is granted", Toast.LENGTH_SHORT).show();
        }
    }

    private VideoView videoView;
    String videoPath = "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_5mb.mp4";
    String outFilePath = "";//

    private void prepareVideoView() {
        MediaController mediaController = new MediaController(this);
        videoView = (VideoView) findViewById(R.id.videoView);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        btnPlay = (Button) findViewById(R.id.btnPlayVideo);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new VideoDownloader().execute(videoPath);
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                player = mp;

                player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        Log.w("download", "size changed");
                    }
                });
            }
        });
    }

    File outFile;

    class VideoDownloader extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {


            outFile = new File(outFilePath);
            FileOutputStream out = null;
            BufferedInputStream input = null;
            try {
                outFile.createNewFile();
                out = new FileOutputStream(outFile, true);

                try {
                    URL url = new URL(videoPath);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new RuntimeException("response is not http_ok");
                    }
                    int fileLength = connection.getContentLength();

                    input = new BufferedInputStream(connection.getInputStream());
                    byte data[] = new byte[2048];
                    long readBytes = 0;
                    int len;
                    boolean flag = true;
                    int readb = 0;
                    while ((len = input.read(data)) != -1) {
                        out.write(data, 0, len);
                        readBytes += len;
                        readb += len;
                        if (readb > 1000000) {
                            out.flush();
                            //playVideo();
                            readb = 0;
                            break;
                        }
                        Log.w("download", (readBytes / 1024) + "kb of " + (fileLength / 1024) + "kb");
                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null)
                        out.flush();
                    out.close();
                    if (input != null)
                        input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.w("download", "Done");
            playVideo();

        }
    }

    private void playVideo() {

        videoView.setVideoPath(outFile.getAbsolutePath());
        videoView.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
