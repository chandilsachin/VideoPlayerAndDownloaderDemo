package com.example.sachinchandil.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;


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
    int port = 3639;
    String videoPath = "http://127.0.0.1:" + port;//"http://techslides.com/demos/sample-videos/small.mp4";

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

        surfaceView = (SurfaceView) v.findViewById(R.id.surfaceViewVideo);
        surfaceHolder = surfaceView.getHolder();
        outFilePath = Environment.getExternalStorageDirectory() + "/video.mp4";

        prepareVideoView(v);

        startServer();
        try {
            System.out.println(getLocalIpAddress());
        } catch (SocketException e) {
            e.printStackTrace();
        }
/*
        */
        return v;
    }

    private void prepareVideoView(View v) {
        MediaController mediaController = new MediaController(getActivity());

        btnPlay = (Button) v.findViewById(R.id.btnPlayVideo);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                } else {
                    Toast.makeText(getActivity(), "Permission is granted", Toast.LENGTH_SHORT).show();
                    new VideoDownloader().execute(videoPath, outFilePath);
                }
            }
        });


    }

    public String getLocalIpAddress() throws SocketException {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        Log.i("tag", "***** IP=" + ip);
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("tag", ex.toString());
        }
        return null;
    }

    int fileLength;
    class VideoDownloader extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {



            BufferedInputStream input = null;
            try {
                final FileOutputStream out = new FileOutputStream(params[1]);

                try {
                    URL url = new URL(params[0]);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new RuntimeException("response is not http_ok");
                    }
                    int fileLength = connection.getContentLength();

                    input = new BufferedInputStream(connection.getInputStream());
                    byte data[] = new byte[4096];
                    long readBytes = 0;
                    int len;
                    boolean flag = true;
                    int readb = 0;
                    while ((len = input.read(data)) != -1) {
                        out.write(data, 0, len);
                        out.flush();
                        readBytes += len;
                        readb += len;
                        Log.w("download", (readBytes / 1024) + "kb of " + (fileLength / 1024) + "kb");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }
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

    private void playVideo(final String url) {


        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                player = new MediaPlayer();
                player.setDisplay(holder);

                Uri source = Uri.parse(url);
                try {

                    player.setDataSource(url);
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

    private void startServer(){
        LocalFileStreamingServer server = new LocalFileStreamingServer(new File(outFilePath));
        server.init("127.0.0.1");
        server.start();
        String url = server.getFileUrl();
        playVideo(url);
    }

    class VideoStreamServer {

        public void startServer() {
            outFile = new File(outFilePath);
            Runnable videoStreamTask = new Runnable() {
                @Override
                public void run() {
                    try {
                        ServerSocket socket = new ServerSocket(port);


                        System.out.println("Waiting for client to connect.");
                        while (true) {
                            Socket client = socket.accept();

                            BufferedOutputStream os = new BufferedOutputStream(client.getOutputStream());
                            BufferedInputStream in = new BufferedInputStream(new FileInputStream(outFile));

                            StringBuilder sb = new StringBuilder();
                            sb.append("HTTP/1.1 200 OK\r\n");
                            sb.append("Content-Type: video/mp4 \r\n");
                            sb.append("Accept-Ranges: bytes\r\n");
                            sb.append("\r\n");


                            byte[] data = new byte[200];
                            int length;
                            System.out.println("Thread Started");
                            //System.setProperty("http.keepAlive", "false");
                            os.write(sb.toString().getBytes());
                            while ((length = in.read(data)) != -1) {
                                os.write(data, 0, length);
                            }
                            os.flush();
                            os.close();
                            client.close();
                            socket.close();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            };

            Thread streamServer = new Thread(videoStreamTask);
            streamServer.start();
        }

    }

    /*class VideoTask implements Runnable {
        private Socket clientSocket;

        public VideoTask(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }


        @Override
        public void run() {
            System.out.println("client started");


            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}
