/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.zoom.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.zoom.BuildConfig;
import cn.wildfirechat.zoom.Config;
import cn.wildfirechat.zoom.R;
import cn.wildfirechat.zoom.net.OKHttpHelper;


public class MyApp extends Application implements AVEngineKit.AVEngineCallback {

    private AsyncPlayer ringPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        AppService.validateConfig(this);

        // 只在主进程初始化
        if (getCurProcessName(this).equals(BuildConfig.APPLICATION_ID)) {
            // IM 初始化
            ChatManager.init(this, Config.IM_SERVER_HOST);
            setupWFCDirs();

            // 音视频初始化
            ringPlayer = new AsyncPlayer(null);
            AVEngineKit.init(this, this);

            // 网络初始化
            OKHttpHelper.init(this);

            SharedPreferences sp = getSharedPreferences(Config.SP_CONFIG_FILE_NAME, Context.MODE_PRIVATE);
            String id = sp.getString("id", null);
            String token = sp.getString("token", null);
            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(token)) {
                //需要注意token跟clientId是强依赖的，一定要调用getClientId获取到clientId，然后用这个clientId获取token，这样connect才能成功，如果随便使用一个clientId获取到的token将无法链接成功。
                //另外不能多次connect，如果需要切换用户请先disconnect，然后3秒钟之后再connect（如果是用户手动登录可以不用等，因为用户操作很难3秒完成，如果程序自动切换请等3秒）
                ChatManager.Instance().connect(id, token);
            }
        }
    }

    private void setupWFCDirs() {
        File file = new File(Config.VIDEO_SAVE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(Config.AUDIO_SAVE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(Config.FILE_SAVE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(Config.PHOTO_SAVE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
            .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
            .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    @Override
    public void onReceiveCall(AVEngineKit.CallSession callSession) {

    }

    @Override
    public void shouldStartRing(boolean isIncoming) {
        if (isIncoming && ChatManager.Instance().isVoipSilent()) {
            Log.d("wfcUIKit", "用户设置禁止voip通知，忽略来电提醒");
            return;
        }
        ChatManager.Instance().getMainHandler().postDelayed(() -> {
            AVEngineKit.CallSession callSession = AVEngineKit.Instance().getCurrentSession();
            if (callSession == null || (callSession.getState() != AVEngineKit.CallState.Incoming && callSession.getState() != AVEngineKit.CallState.Outgoing)) {
                return;
            }

            if (isIncoming) {
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.incoming_call_ring);
                ringPlayer.play(this, uri, true, AudioManager.STREAM_RING);
            } else {
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.outgoing_call_ring);
                ringPlayer.play(this, uri, true, AudioManager.STREAM_RING);
            }
        }, 200);
    }

    @Override
    public void shouldSopRing() {
        Log.d("wfcUIKit", "showStopRing");
        ringPlayer.stop();
    }
}
