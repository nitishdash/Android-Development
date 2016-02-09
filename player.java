package com.dashapps.nitish.dashmusicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class player extends AppCompatActivity implements View.OnClickListener {
    static MediaPlayer mp;
    ArrayList songs;
    SeekBar sb;
    int pos;
    Uri u;
    Thread updateSeekBar;
    ImageButton bt_next, bt_previous, bt_pause, bt_ff, bt_fb;
    TextView dur,maxTV, alb, tit, art;
    String album, artist, genre;
    ImageView albumart;
    MediaMetadataRetriever metaRetriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //initialise
        dur = (TextView) findViewById(R.id.textView3);
        maxTV = (TextView) findViewById(R.id.textView2);
        bt_fb = (ImageButton) findViewById(R.id.bt_fb);
        bt_ff = (ImageButton) findViewById(R.id.bt_ff);
        bt_next = (ImageButton) findViewById(R.id.bt_next);
        bt_previous = (ImageButton) findViewById(R.id.bt_previous);
        bt_pause = (ImageButton) findViewById(R.id.bt_pause);

        bt_pause.setOnClickListener(this);
        bt_ff.setOnClickListener(this);
        bt_fb.setOnClickListener(this);
        bt_next.setOnClickListener(this);
        bt_previous.setOnClickListener(this);

        albumart = (ImageView) findViewById(R.id.iv_album);
        sb = (SeekBar) findViewById(R.id.seekBar);
        maxTV = (TextView) findViewById(R.id.textView2);
        art = (TextView) findViewById(R.id.textView4);
        alb = (TextView) findViewById(R.id.textView5);
        tit = (TextView) findViewById(R.id.textView6);



        /* HERE I CREATED 2 THREADS, 1 FOR CHANGING THE SEEKBAR POSIITION AND OTHER FOR CHANGING THE TIME PLAYED TEXTVIEW*/



        updateSeekBar = new Thread(){
            @Override
            public void run(){
                fileInfo();
                int totalDuration = mp.getDuration();
                int currentPosition = 0;
                maxTV.setText(""+getDurationv2(totalDuration));
                while(currentPosition<totalDuration){
                    try{
                        sleep(500);
                        currentPosition = mp.getCurrentPosition();

                        sb.setProgress(currentPosition);
                        dur.setText(getDurationv2(mp.getCurrentPosition()));  //fuck! finally could convert the milliseconds

                    } catch (Exception e)
                    {
                  //  giveAToast(""+e.toString());
                    }
                }
            }
        };

        if (mp!=null){
            mp.stop();
            mp.release();
        }

        Intent i= getIntent();
        Bundle b = i.getExtras();
        songs = (ArrayList) b.getParcelableArrayList("songList");
        pos = b.getInt("pos", 0);
        u = Uri.parse("" + songs.get(pos));
       // tv4.setText((CharSequence) u);
        mp = MediaPlayer.create(getApplicationContext(), u);
        mp.start();
        sb.setMax(mp.getDuration());  //maximum limit of the seekbar
        updateSeekBar.start();

        //update the album art with the song that was clicked
        albumArtUpdation();





        //  maxTV.setText(mp.getDuration());

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());  //when finger is lifted up, the player seeks to the location of the finger
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_pause:
                if(mp.isPlaying()){
                    mp.pause();
                    bt_pause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    giveAToast("Paused!");

                    //JJust an eye candy - Flash the elapsed-time textview when music is paused
                    dur.setVisibility(View.INVISIBLE);
                    try {
                        wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dur.setVisibility(View.VISIBLE);
                }
                else {
                    mp.start();
                    bt_pause.setImageResource(R.drawable.ic_pause_black_24dp);


                }



                break;
            case R.id.bt_fb:
                mp.seekTo(mp.getCurrentPosition()-5000);
                break;
            case R.id.bt_ff:
                mp.seekTo(mp.getCurrentPosition()+5000);
                break;
            case R.id.bt_next:
                mp.stop();
                mp.release();
                pos = (pos+1)%songs.size();   //modulus has been used over here to indicate that the end of the list has been reached
                u = Uri.parse(songs.get(pos).toString());
                mp = MediaPlayer.create(getApplicationContext(), u);
                mp.start();
                sb.setMax(mp.getDuration());  //maximum limit of the seekbar
                albumArtUpdation();     //update the album art when next is pressed
                fileInfo();
                break;
            case R.id.bt_previous:
                mp.stop();
                mp.release();
                pos = (pos-1<0) ? songs.size()-1: pos-1;   //modulus has been used over here to indicate that the end of the list has been reached
                u = Uri.parse(songs.get(pos).toString());
                mp = MediaPlayer.create(getApplicationContext(), u);
                mp.start();
                sb.setMax(mp.getDuration());
                albumArtUpdation();    //update the album art when previous is pressed
                fileInfo();
                break;

        }
    }
    public void giveAToast(String toastText){
        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();

    }

    public static String getDurationv2(long millis){

        return (String.format("%d:%d",
                TimeUnit.MILLISECONDS.toSeconds(millis)/60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60 ));
    }

    public void fileInfo(){

        metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(""+u);
        try
        {
           // art = metaRetriver.getEmbeddedPicture();
          //  Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
          //  album_art.setImageBitmap(songImage);

            album = metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            artist=metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            genre=metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

            alb.setText(album);
            art.setText(artist);
            tit.setText(genre);
        } catch (Exception e)
        {
          //  album_art.setBackgroundColor(Color.GRAY);
            alb.setText("Unknown Album");
            art.setText("Unknown Artist");
            tit.setText("Unknown Genre");

        }


    }


    //now album art updation -PRETTY FANTASTIC!
    public void albumArtUpdation(){
        try{
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(u.getPath());
            byte [] data = mmr.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            albumart.setImageBitmap(bitmap);

        }catch (Exception e){
            albumart.setImageResource(R.drawable.album_def);
        }
    }
}
