package com.dashapps.nitish.dashmusicplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BaseColumns {

    ListView lv;
    String[] listed_track_names;
    static MediaPlayer mp;
    static String title;
    int pos;
    Button nowplaying;
    Intent np;
    Context ctx = this;
    int counter=0;
    ArrayList<File> songs;
    ArrayList<String> fetchedSongsURIArrayIndex;
    ArrayList<String> songNames;
    String[] songURI;
    static MediaMetadataRetriever metaRetriever;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialization
        lv = (ListView) findViewById(R.id.listView);
        nowplaying = (Button) findViewById(R.id.nowplaying);

        fetchedSongsURIArrayIndex = new ArrayList<>();
        songNames = new ArrayList<>();
        songURI = new String[songNames.size()];


        String[] fetchedSongNames;
        String[] fetchedSongsURI;

        /*THE DATABASE OPERATIONS:

        Here, I am creating the database operations:
        1. Check whether the app is in the first run state
        2. Check whether new media has been added
        3. Perform write or read operations accordingly

         */

        DBlogic DB=new DBlogic(ctx);

        //BELOW CODE IS FOR PERFORMING SOMETHING FOR THE FIRST TIME THE APP IS RUN
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.d("Comments >>>>>>>>>>>>", "First time");

            // first time task

            songs = listSongs(Environment.getExternalStorageDirectory());    //fetch all the songs
            songURI = new String[songs.size()];

            for (int i = 0; i < (songs.size()); i++)
            {

                songNames.add("" + audioTitleFinder(songs.get(i).getAbsolutePath()));
                songURI[i]=""+songs.get(i).getAbsolutePath();

            }

            listed_track_names = new String[songNames.size()];
            listed_track_names = songNames.toArray(listed_track_names);

            for (int i = 0; i < (songs.size()); i++)
            {
                DB.putInfo(DB, "" + listed_track_names[i], "" + songURI[i]);

            }
            finish();

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).commit();
        }

        //BELOW CODE IS FOR READING THE DATA FROM DATABASE AND POPULATING THE LISTVIEW

        DBlogic dBlogic = new DBlogic(ctx);
        Cursor CR = dBlogic.getInfo(dBlogic);
        CR.moveToFirst();
        counter=0;
        int size=0;

        //THIS DO WHILE IS FOR GETTING THE SIZE OF THE ARRAY
        do {
            size++;
        }while (CR.moveToNext());

        CR.moveToFirst();
        fetchedSongNames = new String[size];
        fetchedSongsURI =new String[size];


        //
        do {
            fetchedSongNames[counter] = "" + CR.getString(CR.getColumnIndex(TableData.TableInfo.SONG_NAME));
            fetchedSongsURI[counter] = "" + CR.getString(CR.getColumnIndex(TableData.TableInfo.URI_PATH));
            fetchedSongsURIArrayIndex.add(CR.getString(CR.getColumnIndex(TableData.TableInfo.URI_PATH)));
            counter++;

        }
        while(CR.moveToNext());



        ArrayAdapter<String> adp = new ArrayAdapter<>(getApplicationContext(), R.layout.songlist, R.id.song_title, fetchedSongNames);
        lv.setAdapter(adp);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                np = new Intent(getApplicationContext(), player.class).putExtra("pos", position).putExtra("songList", fetchedSongsURIArrayIndex);
                startActivity(np);
                pos = position;

            }
        });

    }

    public ArrayList<File> listSongs(File root) {
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isDirectory() && !file.isHidden()) {
                al.addAll(listSongs(file));
            } else {
                if (file.getName().endsWith(".mp3")) {
                    al.add(file);
                }
            }
        }
        return al;
    }

    @Override
    public void onClick(View v) {

    }



    //THIS METHOD HAS BEEN CREATED TO FETCH THE SONG NAME FROM THE META DATA RATHER THAN THE FILE NAME

    public static String audioTitleFinder(String str){
        title="BLAH!";

        try
        {
            metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(str);
            title = metaRetriever .extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

        } catch (Exception e)
        {
            title="crap";

        }
        return title;

    }


}
