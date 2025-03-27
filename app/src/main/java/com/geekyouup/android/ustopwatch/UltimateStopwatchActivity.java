package com.geekyouup.android.ustopwatch;

import static com.geekyouup.android.ustopwatch.constant.UstopwatchConsts.LOG_TAG;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import com.geekyouup.android.ustopwatch.adapter.AlarmUpdater;
import com.geekyouup.android.ustopwatch.adapter.SoundManager;
import com.geekyouup.android.ustopwatch.adapter.TabsFragmentAdapter;
import com.geekyouup.android.ustopwatch.constant.UstopwatchConsts;
import com.geekyouup.android.ustopwatch.fragments.CountdownFragment;
import com.geekyouup.android.ustopwatch.fragments.LapTimeRecorder;
import com.geekyouup.android.ustopwatch.fragments.LapTimesFragment;

import com.geekyouup.android.ustopwatch.fragments.StopwatchFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import java.util.Arrays;

public class UltimateStopwatchActivity extends AppCompatActivity {

    private static final String KEY_AUDIO_STATE = "key_audio_state";
    private static final String KEY_JUMP_TO_PAGE = "key_start_page";
    public static final String PREFS_NAME = "USW_PREFS";

    private LapTimesFragment mLapTimesFragment;
    private CountdownFragment mCountdownFragment;
    private StopwatchFragment mStopwatchFragment;
    private SoundManager mSoundManager;
    private ViewPager2 mViewPager2;
    private TabsFragmentAdapter mTabsFragmentAdapter;
    private Menu mMenu;
    private boolean isLapTimesEnabled = false;
    private ActivityResultLauncher<Intent> mSettingLauncher;

    private static UltimateStopwatchActivity stopwatchActivity;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Window window = getWindow();
        window.setBackgroundDrawable(null);
        //
        Resources res = getResources();
        // 透明的话与顶部tab栏颜色不一致
        //window.setStatusBarColor(Color.TRANSPARENT);
        // 设置状态栏颜色与顶部tab栏颜色一致
        window.setStatusBarColor(res.getColor(R.color.actionbar_background, getTheme()));
        // 在action bar位置设置tab layout分页卡栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        // 右边多余菜单的按钮图标
        toolbar.setOverflowIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_more_vert_24dp, getTheme()));
        setSupportActionBar(toolbar);
        setTitle("");

        mSoundManager = SoundManager.getInstance(this);
        //
        mViewPager2 = findViewById(R.id.viewpager);
        mViewPager2.setOffscreenPageLimit(2);
        //
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SettingsActivity.loadSettings(settings);
        setupTabs();
        //
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // Create a notification channel (required for Android 8.0 and higher)
        createNotificationChannel();
        //
        setRequestedOrientation(SettingsActivity.getOrientation());

        //If launched from Countdown notification then goto countdown clock directly
        if (getIntent() != null && getIntent().getBooleanExtra(AlarmUpdater.INTENT_EXTRA_LAUNCH_COUNTDOWN, false)) {
            mViewPager2.setCurrentItem(2);
        }

        // setting button to launch setting activity
        mSettingLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        // laptimes状态改变则创建新的activity(会调用onCreate方法)
                        if (isLapTimesEnabled != SettingsActivity.isLaptimerEnabled()) {
                            @SuppressLint("UnsafeIntentLaunch")
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    }
                });

        stopwatchActivity = this;
        Log.i(LOG_TAG, "ultimate stopwatch activity onCreate() complete");
    }

    private void setupTabs() {
        TabLayout tl = findViewById(R.id.tablayout);

        mTabsFragmentAdapter = new TabsFragmentAdapter(this);
        mStopwatchFragment = new StopwatchFragment();
        mTabsFragmentAdapter.addTab(getString(R.string.stopwatch), mStopwatchFragment);
        if (SettingsActivity.isLaptimerEnabled()) {
            mTabsFragmentAdapter.addTab(getString(R.string.laptimes), new LapTimesFragment());
        }
        mCountdownFragment = new CountdownFragment();
        mTabsFragmentAdapter.addTab(getString(R.string.countdown), mCountdownFragment);
        mViewPager2.setAdapter(mTabsFragmentAdapter);
        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 刷新菜单，以显示出laptimes的菜单
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        // Link the TabLayout and ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(tl, mViewPager2,
                (tab, position) -> tab.setText(mTabsFragmentAdapter.getFragmentTitle(position))
        ).attach();
    }

    /**
     * 创建通知渠道
     * Create a notification channel for devices running Android 8.0 or higher.
     * A channel groups notifications with similar behavior.
     */
    private void createNotificationChannel() {
        // Request runtime permission for notifications on Android 13 and higher, api 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        // 计时渠道、倒计时渠道; Android 8.0, api 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // channel name 为通知类别条目的标题 channel description 为通知类别条目的内容描述
            NotificationChannel channelStart = new NotificationChannel(UstopwatchConsts.NOTIFICATION_CHANNEL_START,
                    getString(R.string.notification_channel_name_start), NotificationManager.IMPORTANCE_HIGH);
            channelStart.setDescription(getString(R.string.notification_channel_desc_start));
            channelStart.enableLights(true); // Turn on notification light
            channelStart.setLightColor(Color.GREEN);
            channelStart.enableVibration(true); // Allow vibration for notifications
            //
            NotificationChannel channelCountdown = new NotificationChannel(UstopwatchConsts.NOTIFICATION_CHANNEL_COUNTDOWN,
                    getString(R.string.notification_channel_name_countdown), NotificationManager.IMPORTANCE_HIGH);
            channelCountdown.setDescription(getString(R.string.notification_channel_desc_countdown));
            channelCountdown.enableLights(true);
            channelCountdown.setLightColor(Color.GREEN);
            channelCountdown.enableVibration(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannels(Arrays.asList(channelStart, channelCountdown));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_AUDIO_STATE, mSoundManager.isAudioOn());
        //if we're quitting with the countdown running and not the stopwatch then jump to countdown on relaunch
        if (mCountdownFragment.isRunning() && !mStopwatchFragment.isRunning()) {
            editor.putInt(KEY_JUMP_TO_PAGE, 2);
        } else {
            editor.putInt(KEY_JUMP_TO_PAGE, -1);
        }
        editor.apply();

        mStopwatchFragment.notifyIfNecessary(); // mStopwatchFragment not null

        LapTimeRecorder.getInstance().saveTimes(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检查屏幕方向设定是否改变
        int setOrientation = SettingsActivity.getOrientation();
        if (getRequestedOrientation() != setOrientation) setRequestedOrientation(setOrientation);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mSoundManager.setAudioState(settings.getBoolean(KEY_AUDIO_STATE, true));
        SettingsActivity.loadSettings(settings);

        isLapTimesEnabled = SettingsActivity.isLaptimerEnabled();
        if (isLapTimesEnabled) LapTimeRecorder.getInstance().loadTimes(this);

        if (mMenu != null) {
            MenuItem audioButton = mMenu.findItem(R.id.menu_audiotoggle);
            if (audioButton != null)
                audioButton.setIcon(mSoundManager.isAudioOn() ? R.drawable.ic_volume_black_24dp : R.drawable.ic_volume_mute_24dp);
        }

        //jump straight to countdown if it was only item left running
        int jumpToPage = settings.getInt(KEY_JUMP_TO_PAGE, -1);
        if (jumpToPage != -1) mViewPager2.setCurrentItem(2, false);

        // 屏幕常亮
        if (SettingsActivity.isKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * 创建菜单
     * <p>
     * 根据配置属性showAsAction（ifRoom-右空间则展示、never-不展示、always-一直展示），有多余空间则展示图标按钮，否则在右侧三点图标下，
     * 点击后展开显示下拉菜单
     *
     * @param menu The options menu in which you place your items.
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();

        final int currentTab = mViewPager2.getCurrentItem();
        if (SettingsActivity.isLaptimerEnabled()) {
            switch (currentTab) {
                case 1:
                    inflater.inflate(R.menu.menu_laptimes, menu);
                    break;
                case 2:
                    inflater.inflate(R.menu.menu_countdown, menu);
                    break;
                case 0:
                default:
                    inflater.inflate(R.menu.menu_stopwatch, menu);
                    break;
            }
        } else {
            switch (currentTab) {
                case 1:
                    inflater.inflate(R.menu.menu_countdown, menu);
                    break;
                case 0:
                default:
                    inflater.inflate(R.menu.menu_stopwatch, menu);
                    break;
            }
        }

        //get audio icon and set correct variant
        MenuItem audioButton = menu.findItem(R.id.menu_audiotoggle);
        if (audioButton != null)
            audioButton.setIcon(mSoundManager.isAudioOn() ? R.drawable.ic_volume_black_24dp : R.drawable.ic_volume_mute_24dp);
        mMenu = menu;
        return true;
    }

    /**
     * 菜单点击触发action
     *
     * @param item The menu item that was selected.
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_clearlaps) {
            LapTimeRecorder.getInstance().reset(this);
        } else if (item.getItemId() == R.id.menu_audiotoggle) {
            mSoundManager.setAudioState(!(mSoundManager.isAudioOn()));
            item.setIcon(mSoundManager.isAudioOn() ? R.drawable.ic_volume_black_24dp : R.drawable.ic_volume_mute_24dp);
        } else if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            this.mSettingLauncher.launch(intent);
        }

        return true;
    }

    public void registerLapTimeFragment(LapTimesFragment ltf) {
        mLapTimesFragment = ltf;
    }

    public LapTimesFragment getLapTimeFragment() {
        return mLapTimesFragment;
    }

    /**
     * 废弃
     * 不在activity手动实例化fragment的话就需要在fragment里调用这里
     *
     * @param cdf
     */
    @Deprecated
    public void registerCountdownFragment(CountdownFragment cdf) {
        mCountdownFragment = cdf;
    }

    @Deprecated
    public void registerStopwatchFragment(StopwatchFragment swf) {
        mStopwatchFragment = swf;
    }

    public static UltimateStopwatchActivity getInstance() {
        return stopwatchActivity;
    }
}