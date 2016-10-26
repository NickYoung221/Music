package com.young.music;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.young.application.MyApplication;
import com.young.entity.MusicInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 主页面，显示所有音乐信息,继承的是ListActivity，里面自带一个ListView
 */
public class MainActivity extends ListActivity implements AdapterView.OnItemClickListener{

    List<MusicInfo> lists=new ArrayList<MusicInfo>();
    MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //获取listview
        ListView lv=getListView();
        //设置listview的adapter
        getData();
        //设置item点击事件
        lv.setOnItemClickListener(this);
    }

    //读取外部设备的多媒体资源
    public void getData(){
        String[] projection = {
                MediaStore.Audio.Media._ID,       //唯一id
                MediaStore.Audio.Media.ALBUM,     //专辑名
                MediaStore.Audio.Media.ARTIST,   //演唱者
                MediaStore.Audio.Media.TITLE,    //音乐名称
                MediaStore.Audio.Media.DATA,     //音乐文件路径
                MediaStore.Audio.Media.DURATION,//播放时长，毫秒数
        };

        //获取多媒体资源
        //Cursor cursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        Cursor cursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Audio.Media.TITLE); //根据名称排序

        //将cursor转化成List<MediaInfo>
        while(cursor.moveToNext()){
            int id=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String album=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));//歌手
            String title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));//标题
            String url=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//数据源
            int duration=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//播放时长

            MusicInfo musicInfo=new MusicInfo(id, album, title, artist, duration, url);
            lists.add(musicInfo);
        }

        //cursoradapter
        SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,R.layout.media_item, cursor, new String[]{MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.DURATION},
                new int[]{R.id.textView2,R.id.textView1,R.id.textView3});
        //设置适配器
        setListAdapter(adapter);
        //cursor.close(); //关闭cursor，关闭就没有数据？？

        myApplication= (MyApplication) getApplication();
        //list保存在application中
        myApplication.setMusicInfoList(lists);
        Log.i("MainActivity", "getData: ");
        Log.i("MainActivity", "getData: 2");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean isSame=false;//不是同一次点击

        Intent intent=new Intent(this,PlayMusicActivity.class);
        //判断当前点击的item是否是正在播放的item
        if(position==myApplication.getPosition()){
            isSame=true;//是同一次点击
        }else{
            isSame=false;//不是同一次点击
        }
        //传值isSame
        intent.putExtra("isSame",isSame);
        startActivity(intent);

        //position保存在application
        myApplication.setPosition(position);
    }

}
