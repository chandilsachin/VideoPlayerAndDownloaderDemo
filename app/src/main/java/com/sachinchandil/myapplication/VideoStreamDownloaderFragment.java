package com.sachinchandil.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;

import com.sachinchandil.videodownloadandplay.VideoDownloadAndPlayService;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link VideoStreamDownloaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoStreamDownloaderFragment extends Fragment
{
    private String videoPath = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    private String serverPath = "127.0.0.1";
    private String outFilePath = Environment.getExternalStorageDirectory() + "/video.mp4";

    private MediaPlayer player;
    private Button btnPlay;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    public VideoStreamDownloaderFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoStreamFragment.
     */
    public static VideoStreamDownloaderFragment newInstance()
    {
        VideoStreamDownloaderFragment fragment = new VideoStreamDownloaderFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_video_stream_downloader, container, false);

        surfaceView = (SurfaceView) v.findViewById(R.id.surfaceViewVideo);
        surfaceHolder = surfaceView.getHolder();

        prepareVideoView(v);

        return v;
    }

    private void prepareVideoView(View v)
    {
        MediaController mediaController = new MediaController(getActivity());
        surfaceHolder.addCallback(new SurfaceHolder.Callback()
        {
            @Override
            public void surfaceCreated(SurfaceHolder holder)
            {
                player = new MediaPlayer();
                player.setDisplay(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                player.release();
            }
        });
        btnPlay = (Button) v.findViewById(R.id.btnPlayVideo);
        btnPlay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else
                    {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                } else
                {
                    startServer();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == 1){
            if(permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getActivity(), "Permission is granted. Please click play video button again.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void playVideo(final String url)
    {
        try
        {
            player.setDataSource(url);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {
                @Override
                public void onPrepared(MediaPlayer mp)
                {
                    player.start();
                }
            });
            player.prepareAsync();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private VideoDownloadAndPlayService videoService;


    private void startServer()
    {
        videoService = VideoDownloadAndPlayService.startServer(getActivity(), videoPath,outFilePath, serverPath, new VideoDownloadAndPlayService.VideoStreamInterface()
        {
            @Override
            public void onServerStart(String videoStreamUrl)
            {
                playVideo(videoStreamUrl);
            }
        });
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(videoService != null)
            videoService.stop();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }
}
