package com.young.entity;

/** 音乐信息的实体类
 * Created by yang on 2016/10/14 0014.
 */
public class MusicInfo {
    private Integer _id;     //唯一id
    private String album;   //专辑名
    private String title;   //音乐名
    private String artist;  //歌手
    private int duration;  //歌曲时间
    private String url;    //歌曲的uri

    public MusicInfo(Integer _id, String album, String title, String artist,
                     int duration, String url) {
        super();
        this._id = _id;
        this.album = album;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.url = url;
    }
    public MusicInfo(String title, String artist, int duration, String url) {
        super();
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.url = url;
    }
    public Integer get_id() {
        return _id;
    }
    public void set_id(Integer _id) {
        this._id = _id;
    }
    public String getAlbum() {
        return album;
    }
    public void setAlbum(String album) {
        this.album = album;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}
