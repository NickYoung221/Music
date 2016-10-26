package com.young.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.young.application.MyApplication;
import com.young.entity.MusicInfo;
import com.young.service.MusicService;
import com.young.util.Constant;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 播放音乐的界面
 */
public class PlayMusicActivity extends AppCompatActivity {

    @InjectView(R.id.textView2)
    TextView textView2;
    @InjectView(R.id.textView3)
    TextView textView3;
    @InjectView(R.id.button2)
    Button button2;
    @InjectView(R.id.textView1)
    TextView textView1;
    @InjectView(R.id.button1)
    Button button1;
    @InjectView(R.id.button3)
    Button button3;
    @InjectView(R.id.button4)
    Button button4;
    @InjectView(R.id.seekBar)
    SeekBar seekBar;

    MyApplication myApplication;
    List<MusicInfo> musicInfoList;
    int position;

    MyBroadcastReceiver myBroadcastReceiver; //自定义的广播接受者
    @InjectView(R.id.textView4)
    TextView textView4;
    @InjectView(R.id.textView5)
    TextView textView5;

    //boolean isSetMax=false;//改动1：设置是否点击过播放，没有点击过
    private int currentPosition;   //当前位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        ButterKnife.inject(this);

        myApplication = (MyApplication) getApplication();
        if (myApplication.isFirstToPlay()) { //如果是第一次进入播放页面，则播放
            startServiceToMusic(1);
            myApplication.setFirstToPlay(false); //设置不是第一次进入，以后就不会执行了
        }
        musicInfoList = myApplication.getMusicInfoList(); //取出音乐列表
        position = myApplication.getPosition();           //获取当前播放位置
        initView();
        myBroadcastReceiver = new MyBroadcastReceiver();
        //IntentFilter：动态注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.young.myAction");
        registerReceiver(myBroadcastReceiver, intentFilter);

        //设置进度条的最大值
        seekBar.setMax(musicInfoList.get(position).getDuration());

        //seekBar的进度条改变事件
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) { //如果是用户拖动的进度条，才进行操作
                    Intent intent = new Intent(PlayMusicActivity.this, MusicService.class);
                    intent.putExtra("progress", progress);//用户拖到的进度
                    intent.putExtra("operation", 5);//改变进度条
                    startService(intent);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //获取isSame字段(进入的是否是正在播放的这首歌的页面)
        Intent intent = getIntent();
        //从其他界面跳转过来
        if (intent != null) {
            boolean isSame = intent.getBooleanExtra("isSame", false);
            if (isSame) {//是同一首
                //进度条变成最新播放值
                seekBar.setProgress(currentPosition);
            } else {//若不是同一首，停止播放前一首
                startServiceToMusic(2);
                //进度条归0
                seekBar.setProgress(0);

                startServiceToMusic(1);//播放这一首
            }
        }
    }

    //初始化界面
    void initView() {
        //显示歌名
        textView1.setText(musicInfoList.get(position).getTitle());
        //显示歌手
        textView2.setText(musicInfoList.get(position).getArtist());
        //显示播放时长（将毫秒数转化为“分：秒”格式）
        textView5.setText(formatDate(musicInfoList.get(position).getDuration()));
    }

    @OnClick({R.id.button2, R.id.button1, R.id.button3, R.id.button4})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1: //点击“播放/暂停”
                //判断如果没有设置过最大值，设置最大值,不用这种方式了
               /* if(isSetMax==false){
                    isSetMax=true;
                    seekBar.setMax(musicInfoList.get(position).getDuration());
                }*/
                startServiceToMusic(1);//operation为1：Service里对应的操作：播放或暂停
                break;
            case R.id.button2: //点击“停止”
                startServiceToMusic(2);//operation为1：Service里对应的操作：停止
                textView4.setText(formatDate(0));//设置时间
                break;
            case R.id.button3: //点击“上一首”
                //上一首: position:0
                //更新application的position
                //position>0
                position = myApplication.getPosition();//获取当前position值
                if (position > 0) { //不是第一首才能执行
                    position--;
                    myApplication.setPosition(position);//更新application
                } else { //若在第一首，则播放最后一首
                    position = musicInfoList.size() - 1;
                    myApplication.setPosition(position);
                }
                startServiceToMusic(3);
                break;
            case R.id.button4:
                //下一首:如果是最后一首，变成第一首
                position = myApplication.getPosition();
                myApplication.setPosition(++position % musicInfoList.size());
                startServiceToMusic(4);
                break;
        }
    }

    //启动service,在Service里进行相关操作
    void startServiceToMusic(int operation) {
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("operation", operation);
        startService(intent);
    }

    //格式化时间，将毫秒转化为“分：秒”的格式
    public String formatDate(int duration) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(duration);
    }

    //自定义广播接收者，activity里要动态注册广播，并且在onDestroy里要解除注册
    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //接受到：播放状态putExtra(i)
            int playState = intent.getIntExtra("playState", -1);
            //是否改变tv值
            int changeInfo = intent.getIntExtra("changeInfo", -1);
            //是否改变进度条
            int changeSeekBar = intent.getIntExtra("changeSeekBar", -1);

            //“播放”、“暂停”、“停止”按钮：更新按钮文本内容
            if (playState != -1) { //-1是默认值，不等于-1即表示获得到了值
                switch (playState) {
                    case Constant.INIT_STOP://如果Service广播里传来的是停止状态，则播放按钮文字为“播放”
                        button1.setText("播放");
                        seekBar.setProgress(0);    //进度条归0
                        break;
                    case Constant.PLAY://如果Service广播里传来的是播放状态，则播放按钮文字为“暂停”
                        button1.setText("暂停");
                        break;
                    case Constant.PAUSE://如果Service广播里传来的是暂停状态，则播放按钮文字为“播放”
                        button1.setText("播放");
                        break;
                }
            }

            //上一首，下一首：更新tv；更新seekbar的最大值
            // 如果changeTv=1,需要更新tv
            if (changeInfo != -1) { //-1是默认值，不等于-1即表示获得到了值
                //需要再获得一次位置，因为有可能是点击上/下一首，activity并不执行onCreate，需要手动获得一次位置
                position = myApplication.getPosition();
                initView(); //更新界面信息
                //更新进度条，不然的话点击上/下一首，进度条的最大值不变，每首歌的时长不一样所以进度条显示会有问题
                seekBar.setMax(musicInfoList.get(position).getDuration());
            }

            //mediaPlayer播放时，改变进度条
            if (changeSeekBar != -1) {
                //获取当前播放进度
                currentPosition = intent.getIntExtra("currentPosition", -1);
                //设置进度条当前值
                seekBar.setProgress(currentPosition);
                //设置播放时间改变
                textView4.setText(formatDate(currentPosition));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver); //要接触注册的广播，否则会报错
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("PlayMusicActivity", "onSaveInstanceState: ");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i("PlayMusicActivity", "onRestoreInstanceState: ");
    }
}
