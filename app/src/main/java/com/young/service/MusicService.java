package com.young.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.young.application.MyApplication;
import com.young.entity.MusicInfo;
import com.young.util.Constant;

import java.io.IOException;
import java.util.List;

/** 处理音乐播放的Service
 * Created by yang on 2016/10/14 0014.
 */
public class MusicService extends Service{

    MediaPlayer mediaPlayer;
    int playState;//播放状态

    Handler handler=new Handler();//主线程中

    MyApplication myApplication;
    List<MusicInfo> musicInfoList;
    int position;

    Runnable runnable;  //实现定时器时需要用到，这里提为全局变量可供下面停掉这个定时器

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer(); //创建MediaPlayer对象
        myApplication = (MyApplication) getApplication();

        //播放完成事件
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                position=myApplication.getPosition();//原来值
                position=++position%musicInfoList.size();//新值
                myApplication.setPosition(position);//更新application中数据
                //播放
                play(Uri.parse(myApplication.getMusicInfoList().get(position).getUrl()));
                //发广播，更新界面
                sendBroadcastToMusic(Constant.PLAY,1);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //根据不同的操作，实现播放，暂停
        //获取用户当前的操作
        int operation = intent.getIntExtra("operation",-1);
        musicInfoList = myApplication.getMusicInfoList();
        position = myApplication.getPosition();
        switch (operation){
            case 1: //点击的是“播放/暂停”按钮
                //判断当前的音乐播放状态
                if(playState==Constant.INIT_STOP){//如果状态是停止播放状态，就播放
                    play(Uri.parse(musicInfoList.get(position).getUrl()));
                }else if(playState==Constant.PLAY){
                    pause();
                }else if(playState==Constant.PAUSE){
                    rePlay();
                }
                sendBroadcastToMusic(playState,-1);//发送广播给activity，通知改变界面状态
                break;
            case 2: //点击的是“停止”按钮
                stop();
                sendBroadcastToMusic(playState,-1);
                break;
            case 3: //点击的是“上一首”
                //直接播放上一首:application中维护最新数据
                play(Uri.parse(musicInfoList.get(position).getUrl()));
                sendBroadcastToMusic(Constant.PLAY,1);//播放状态:1->更新界面信息
                break;
            case 4: //点击的是下一首
                //直接播放下一首
                play(Uri.parse(musicInfoList.get(position).getUrl()));
                sendBroadcastToMusic(Constant.PLAY,1);//播放状态:1->更新界面信息
                break;
            case 5: //改变进度条
                int progress = intent.getIntExtra("progress",-1);
                mediaPlayer.seekTo(progress);   //只改变进度
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //发送广播给PlayMusicActivity，通知其改变界面
    void sendBroadcastToMusic(int playState, int changeInfo){
        //不想传的值设为-1
        Intent intent = new Intent("com.young.myAction");
        intent.putExtra("playState",playState);
        intent.putExtra("changeInfo",changeInfo);
        sendBroadcast(intent);
    }

    //播放音乐
    public void play(Uri uri){
        mediaPlayer.reset(); //有可能用户点击的是播放上一首，这里要reset()
        try {
            mediaPlayer.setDataSource(this,uri);  //设置资源
            mediaPlayer.prepare();                //准备
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();      //开启
        playState= Constant.PLAY;//设置播放状态

        runnable=new Runnable() {
            @Override
            public void run() {
                //当前是播放状态，发广播给activity，activity中更新进度条
                if(playState==Constant.PLAY) {
                    int currentPosition = mediaPlayer.getCurrentPosition();//获取当前播放进度
                    //Log.i("MusicService", "run: "+currentPosition);
                    //发广播
                    Intent intent = new Intent("com.young.myAction");
                    intent.putExtra("changeSeekBar", 1);//播放状态:更新进度条
                    intent.putExtra("currentPosition", currentPosition);
                    sendBroadcast(intent);
                    handler.postDelayed(runnable,1000);//计时器一直在执行
                }

                //放在这里合适吗
                if(playState==Constant.INIT_STOP||playState==Constant.PAUSE){
                    //如果播放状态是停止或暂停，则remove定时器
                    handler.removeCallbacks(runnable);
                }
            }
        };
        handler.postDelayed(runnable,1000);//开启定时器
    }

    //暂停
    public void pause(){
        if(playState== Constant.PLAY){ //在播放状态才能暂停
            mediaPlayer.pause();//暂停
            playState=Constant.PAUSE;
        }
    }

    //停止
    public void stop(){
        if(playState==Constant.PLAY||playState==Constant.PAUSE){
            mediaPlayer.stop();//停止
            playState=Constant.INIT_STOP;//状态改变
        }
    }

    //重新播放：（暂停-播放）
    public void rePlay(){
        if(playState==Constant.PAUSE){
            mediaPlayer.start();
            playState=Constant.PLAY;//改变状态为播放状态
        }
        handler.postDelayed(runnable,1000);//从暂停到再次播放：再发一个消息，开启定时器
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mediaPlayer.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
