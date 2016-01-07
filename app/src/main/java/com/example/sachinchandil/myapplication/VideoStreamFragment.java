package com.example.sachinchandil.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link VideoStreamFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoStreamFragment extends Fragment {


    private Button btnPlay;
    private MediaPlayer player;
    private SurfaceView surfaceView;

    private VideoView videoView;
    String videoPath = "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_5mb.mp4";
    String outFilePath = "";//

    File outFile;

    public VideoStreamFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoStreamFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoStreamFragment newInstance() {
        VideoStreamFragment fragment = new VideoStreamFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void prepareVideoView(View v) {
        MediaController mediaController = new MediaController(getActivity());
        videoView = (VideoView) v.findViewById(R.id.videoView);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        btnPlay = (Button) v.findViewById(R.id.btnPlayVideo);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_video_stream, container, false);
        ;

        outFilePath = getActivity().getExternalFilesDir("/") + "/video.mp4";
        prepareVideoView(v);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            Toast.makeText(getActivity(), "Permission is granted", Toast.LENGTH_SHORT).show();
        }
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
