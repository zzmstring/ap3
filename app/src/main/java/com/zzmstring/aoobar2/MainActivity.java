package com.zzmstring.aoobar2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzmstring.aoobar2.music.MediaBinder;


public class MainActivity extends ActionBarActivity {
    public static final int SLIDING_MENU_SCAN = 0;// 侧滑->扫描歌曲
    public static final int SLIDING_MENU_ALL = 1;// 侧滑->全部歌曲
    public static final int SLIDING_MENU_FAVORITE = 2;// 侧滑->我的最爱
    public static final int SLIDING_MENU_FOLDER = 3;// 侧滑->文件夹
    public static final int SLIDING_MENU_EXIT = 4;// 侧滑->退出程序
    public static final int SLIDING_MENU_FOLDER_LIST = 5;// 侧滑->文件夹->文件夹列表

    public static final int DIALOG_DISMISS = 0;// 对话框消失
    public static final int DIALOG_SCAN = 1;// 扫描对话框
    public static final int DIALOG_MENU_REMOVE = 2;// 歌曲列表移除对话框
    public static final int DIALOG_MENU_DELETE = 3;// 歌曲列表提示删除对话框
    public static final int DIALOG_MENU_INFO = 4;// 歌曲详情对话框
    public static final int DIALOG_DELETE = 5;// 歌曲删除对话框

    public static final String PREFERENCES_NAME = "settings";// SharedPreferences名称
    public static final String PREFERENCES_MODE = "mode";// 存储播放模式
    public static final String PREFERENCES_SCAN = "scan";// 存储是否扫描过
    public static final String PREFERENCES_SKIN = "skin";// 存储背景图
    public static final String PREFERENCES_LYRIC = "lyric";// 存储歌词高亮颜色

    public static final String BROADCAST_ACTION_SCAN = "com.cwd.cmeplayer.action.scan";// 扫描广播标志
    public static final String BROADCAST_ACTION_MENU = "com.cwd.cmeplayer.action.menu";// 弹出菜单广播标志
    public static final String BROADCAST_ACTION_FAVORITE = "com.cwd.cmeplayer.action.favorite";// 喜爱广播标志
    public static final String BROADCAST_ACTION_EXIT = "com.cwd.cmeplayer.action.exit";// 退出程序广播标志
    public static final String BROADCAST_INTENT_PAGE = "com.cwd.cmeplayer.intent.page";// 页面状态
    public static final String BROADCAST_INTENT_POSITION = "com.cwd.cmeplayer.intent.position";// 歌曲索引
    public static final String BROADCASE_MP3PATH="com.zzmstring.aoobar.mp3path";
    private final String TITLE_ALL = "播放列表";
    private final String TITLE_FAVORITE = "我的最爱";
    private final String TITLE_FOLDER = "文件夹";
    private final String TITLE_NORMAL = "无音乐播放";
    private final String TIME_NORMAL = "00:00";

    private int skinId;// 背景图ID
    private int slidingPage = SLIDING_MENU_ALL;// 页面状态
    private int playerPage;// 发送给PlayerActivity的页面状态
    private int musicPosition;// 当前播放歌曲索引
    private int folderPosition;// 文件夹列表索引
    private int dialogMenuPosition;// 记住弹出歌曲列表菜单的歌曲索引

    private boolean canSkip = true;// 防止用户频繁点击造成多次解除服务绑定，true：允许解绑
    private boolean bindState = false;// 服务绑定状态

    private String mp3Current;// 歌曲当前时长
    private String mp3Duration;// 歌曲总时长
    private String dialogMenuPath;// 记住弹出歌曲列表菜单的歌曲路径

    private TextView mainTitle;// 列表标题
    private TextView mainSize;// 歌曲数量
    private TextView mainArtist;// 艺术家
    private TextView mainName;// 歌曲名称
    private TextView mainTime;// 歌曲时间
    private ImageView mainAlbum;// 专辑图片

    private ImageButton btnMenu;// 侧滑菜单按钮
    private ImageButton btnPrevious;// 上一首按钮
    private ImageButton btnPlay;// 播放和暂停按钮
    private ImageButton btnNext;// 下一首按钮

    private LinearLayout skin;// 背景图
    private LinearLayout viewBack;// 返回上一级
    private LinearLayout viewControl;// 底部播放控制视图

    private Intent playIntent;
    private MediaBinder binder;
    private MainReceiver receiver;

    private SharedPreferences preferences;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        toolbar.setLogo(R.mipmap.ic_launcher);
        toolbar.setTitle("My Title");
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_main);
        init();
        initListener();

    }
    private void init(){


    }
    private void initListener(){

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /**
     * 用于接收歌曲列表菜单及将歌曲标记为最爱的广播
     */
    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent != null) {
                final String action = intent.getAction();

                if (action.equals(BROADCAST_ACTION_EXIT)) {
//                    exitProgram();
                    return;
                }
                if(action.equals(BROADCASE_MP3PATH)){
                    String path=intent.getStringExtra("path");

                }
                // 没有传值的就是通过播放界面标记我的最爱的，所以默认赋值上次点击播放的页面，为0则默认为全部歌曲
//                slidingPage = intent.getIntExtra(BROADCAST_INTENT_PAGE,
//                        playerPage == 0 ? SLIDING_MENU_ALL : playerPage);
//                dialogMenuPosition = intent.getIntExtra(
//                        BROADCAST_INTENT_POSITION, 0);
//                MusicInfo info = null;


            }
        }
    }
}
