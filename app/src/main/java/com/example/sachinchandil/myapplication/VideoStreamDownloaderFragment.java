package com.example.sachinchandil.myapplication;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link VideoStreamDownloaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoStreamDownloaderFragment extends Fragment {
    private Button btnPlay;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    String videoPath = "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_5mb.mp4";
    String outFilePath = "";//

    File outFile;

    public VideoStreamDownloaderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoStreamFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoStreamDownloaderFragment newInstance() {
        VideoStreamDownloaderFragment fragment = new VideoStreamDownloaderFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    MediaPlayer player;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_video_stream_downloader, container, false);
        ;
        surfaceView = (SurfaceView) v.findViewById(R.id.surfaceViewVideo);
        surfaceHolder = surfaceView.getHolder();
        outFilePath = getActivity().getExternalFilesDir("/") + "/video.mp4";
        try {
            FileOutputStream fos = new FileOutputStream(new File(outFilePath), true);
            FileInputStream in = new FileInputStream(outFilePath);
            playVideo(in.getFD());
        } catch (IOException e) {
            e.printStackTrace();
        }


        prepareVideoView(v);
/*
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            Toast.makeText(getActivity(), "Permission is granted", Toast.LENGTH_SHORT).show();
        }*/
        return v;
    }

    private void prepareVideoView(View v) {
        MediaController mediaController = new MediaController(getActivity());

        btnPlay = (Button) v.findViewById(R.id.btnPlayVideo);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new VideoDownloader().execute(videoPath);
            }
        });


    }

    class VideoDownloader extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {


            outFile = new File(outFilePath);

            BufferedInputStream input = null;
            try {
                outFile.createNewFile();
                final FileOutputStream out = new FileOutputStream(outFile, true);

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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        playVideo(out.getFD());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

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
            //playVideo();

        }
    }

    private void playVideo(final FileDescriptor fd) {


        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                player = new MediaPlayer();
                player.setDisplay(holder);

                Uri source = Uri.parse("file://" + outFilePath);
                try {
                    player.setDataSource(fd);
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            player.start();
                        }
                    });
                    player.prepareAsync();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                player.release();
            }
        });
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
