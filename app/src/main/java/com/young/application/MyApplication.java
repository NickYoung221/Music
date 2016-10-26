package com.young.application;

import android.app.Application;

import com.young.entity.MusicInfo;

import java.util.List;

/**
 * Created by yang on 2016/10/14 0014.
 */
public class MyApplication extends Application{

    List<MusicInfo> musicInfoList; //音乐集合
    int position;  //当前播放音乐的位置
    int playState; //音乐播放状态
    boolean isFirstToPlay = true; //是否是第一次进入播放的页面，如果是，则播放

    public List<MusicInfo> getMusicInfoList() {
        return musicInfoList;
    }
    public void setMusicInfoList(List<MusicInfo> musicInfoList) {
        this.musicInfoList = musicInfoList;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public int getPlayState() {
        return playState;
    }  //（似乎用不到?）
    public void setPlayState(int playState) {
        this.playState = playState;
    }

    public boolean isFirstToPlay() {
        return isFirstToPlay;
    }

    public void setFirstToPlay(boolean firstToPlay) {
        isFirstToPlay = firstToPlay;
    }
}
