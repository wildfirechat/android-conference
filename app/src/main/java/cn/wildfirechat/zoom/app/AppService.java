/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.zoom.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallback2;
import cn.wildfirechat.zoom.Config;
import cn.wildfirechat.zoom.model.ConferenceInfo;
import cn.wildfirechat.zoom.model.FavConferences;
import cn.wildfirechat.zoom.model.LoginResult;
import cn.wildfirechat.zoom.net.BooleanCallback;
import cn.wildfirechat.zoom.net.OKHttpHelper;
import cn.wildfirechat.zoom.net.SimpleCallback;
import cn.wildfirechat.zoom.net.base.StatusResult;
import okhttp3.MediaType;

public class AppService {
    private static AppService Instance = new AppService();

    /**
     * App Server默认使用的是8888端口，替换为自己部署的服务时需要注意端口别填错了
     * <br>
     * <br>
     * 正式商用时，建议用https，确保token安全
     * <br>
     * <br>
     */
    public static String APP_SERVER_ADDRESS/*请仔细阅读上面的注释*/ = "http://wildfirechat.net:8888";
//    public static String APP_SERVER_ADDRESS/*请仔细阅读上面的注释*/ = "https://app.wildfirechat.net";

    private AppService() {

    }

    public static AppService Instance() {
        return Instance;
    }

    public interface LoginCallback {
        void onUiSuccess(LoginResult loginResult);

        void onUiFailure(int code, String msg);
    }

    @Deprecated //"已经废弃，请使用smsLogin"
    public void namePwdLogin(String account, String password, LoginCallback callback) {

        String url = APP_SERVER_ADDRESS + "/api/login";
        Map<String, Object> params = new HashMap<>();
        params.put("name", account);
        params.put("password", password);

        try {
            params.put("clientId", ChatManager.Instance().getClientId());
        } catch (Exception e) {
            e.printStackTrace();
            callback.onUiFailure(-1, "网络出来问题了。。。");
            return;
        }

        OKHttpHelper.post(url, params, new SimpleCallback<LoginResult>() {
            @Override
            public void onUiSuccess(LoginResult loginResult) {
                callback.onUiSuccess(loginResult);
            }

            @Override
            public void onUiFailure(int code, String msg) {
                callback.onUiFailure(code, msg);
            }
        });
    }

    public void smsLogin(String phoneNumber, String authCode, LoginCallback callback) {

        String url = APP_SERVER_ADDRESS + "/login";
        Map<String, Object> params = new HashMap<>();
        params.put("mobile", phoneNumber);
        params.put("code", authCode);


        //Platform_iOS = 1,
        //Platform_Android = 2,
        //Platform_Windows = 3,
        //Platform_OSX = 4,
        //Platform_WEB = 5,
        //Platform_WX = 6,
        params.put("platform", new Integer(2));

        try {
            params.put("clientId", ChatManager.Instance().getClientId());
        } catch (Exception e) {
            e.printStackTrace();
            callback.onUiFailure(-1, "网络出来问题了。。。");
            return;
        }

        OKHttpHelper.post(url, params, new SimpleCallback<LoginResult>() {
            @Override
            public void onUiSuccess(LoginResult loginResult) {
                callback.onUiSuccess(loginResult);
            }

            @Override
            public void onUiFailure(int code, String msg) {
                callback.onUiFailure(code, msg);
            }
        });
    }

    public void getMyPrivateConferenceId(GeneralCallback2 callback) {
        if (callback == null) {
            return;
        }
        String url = APP_SERVER_ADDRESS + "/conference/get_my_id";
        OKHttpHelper.post(url, null, new SimpleCallback<String>() {

            @Override
            public void onUiSuccess(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.optInt("code", -1) == 0) {
                        String conferenceId = object.getString("result");
                        callback.onSuccess(conferenceId);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onFail(-1);
            }

            @Override
            public void onUiFailure(int code, String msg) {
                callback.onFail(code);
            }
        });
    }

    public void createConference(ConferenceInfo info, GeneralCallback2 callback) {
        String url = APP_SERVER_ADDRESS + "/conference/create";
        OKHttpHelper.post(url, info, new SimpleCallback<String>() {
            @Override
            public void onUiSuccess(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.optInt("code", -1) == 0) {
                        String conferenceId = object.getString("result");
                        callback.onSuccess(conferenceId);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onFail(-1);
            }

            @Override
            public void onUiFailure(int code, String msg) {
                callback.onFail(code);
            }
        });
    }

    public void queryConferenceInfo(String conferenceId, String password, QueryConferenceInfoCallback callback) {
        if (callback == null) {
            return;
        }
        String url = APP_SERVER_ADDRESS + "/conference/info";
        Map<String, String> map = new HashMap<>();
        map.put("conferenceId", conferenceId);
        if (!TextUtils.isEmpty(password)) {
            map.put("password", password);
        }
        OKHttpHelper.post(url, map, new SimpleCallback<ConferenceInfo>() {

            @Override
            public void onUiSuccess(ConferenceInfo info) {
                callback.onSuccess(info);
            }

            @Override
            public void onUiFailure(int code, String msg) {
                callback.onFail(code, msg);
            }
        });
    }

    public void destroyConference(String conferenceId, GeneralCallback callback) {
        String url = APP_SERVER_ADDRESS + "/conference/destroy/" + conferenceId;
        OKHttpHelper.post(url, null, new SimpleCallback<StatusResult>() {

            @Override
            public void onUiSuccess(StatusResult statusResult) {
                if (statusResult.isSuccess()) {
                    callback.onSuccess();
                } else {
                    callback.onFail(statusResult.getCode());
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                callback.onFail(code);
            }
        });
    }

    public void favConference(String conferenceId, GeneralCallback callback) {
        String url = APP_SERVER_ADDRESS + "/conference/fav/" + conferenceId;
        OKHttpHelper.post(url, null, new SimpleCallback<StatusResult>() {
            @Override
            public void onUiSuccess(StatusResult statusResult) {
                if (callback != null) {
                    if (statusResult.isSuccess()) {
                        callback.onSuccess();
                    } else {
                        callback.onFail(statusResult.getCode());
                    }
                }

            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (callback != null) {
                    callback.onFail(code);
                }
            }
        });
    }

    public void unfavConference(String conferenceId, GeneralCallback callback) {
        String url = APP_SERVER_ADDRESS + "/conference/unfav/" + conferenceId;
        OKHttpHelper.post(url, null, new SimpleCallback<StatusResult>() {
            @Override
            public void onUiSuccess(StatusResult statusResult) {
                if (callback != null) {
                    if (statusResult.isSuccess()) {
                        callback.onSuccess();
                    } else {
                        callback.onFail(statusResult.getCode());
                    }
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (callback != null) {
                    callback.onFail(code);
                }
            }
        });
    }

    public void isFavConference(String conferenceId, BooleanCallback callback) {
        String url = APP_SERVER_ADDRESS + "/conference/is_fav/" + conferenceId;
        OKHttpHelper.post(url, null, new SimpleCallback<StatusResult>() {
            @Override
            public void onUiSuccess(StatusResult statusResult) {
                if (callback != null) {
                    if (statusResult.getCode() == 0) {
                        callback.onSuccess(true);
                    } else if (statusResult.getCode() == 16) {
                        callback.onSuccess(false);
                    } else {
                        callback.onFail(-1, "");
                    }
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (callback != null) {
                    callback.onFail(code, msg);
                }
            }
        });
    }

    public void getFavConferences(FavConferenceCallback callback) {
        String url = APP_SERVER_ADDRESS + "/conference/fav_conferences";
        OKHttpHelper.post(url, null, new SimpleCallback<FavConferences>() {
            @Override
            public void onUiSuccess(FavConferences favConferences) {
                if (callback != null) {
                    callback.onSuccess(favConferences.getConferenceInfos());
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (callback != null) {
                    callback.onFail(code, msg);
                }
            }
        });
    }


    public interface QueryConferenceInfoCallback {
        void onSuccess(ConferenceInfo info);

        void onFail(int code, String msg);
    }

    public interface FavConferenceCallback {
        void onSuccess(List<ConferenceInfo> infos);

        void onFail(int code, String msg);
    }


    public interface SendCodeCallback {
        void onUiSuccess();

        void onUiFailure(int code, String msg);
    }

    public void requestAuthCode(String phoneNumber, SendCodeCallback callback) {

        String url = APP_SERVER_ADDRESS + "/send_code";
        Map<String, Object> params = new HashMap<>();
        params.put("mobile", phoneNumber);
        OKHttpHelper.post(url, params, new SimpleCallback<StatusResult>() {
            @Override
            public void onUiSuccess(StatusResult statusResult) {
                if (statusResult.getCode() == 0) {
                    callback.onUiSuccess();
                } else {
                    callback.onUiFailure(statusResult.getCode(), "");
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                callback.onUiFailure(code, msg);
            }
        });

    }

    public void uploadLog(SimpleCallback<String> callback) {
        List<String> filePaths = ChatManager.Instance().getLogFilesPath();
        if (filePaths == null || filePaths.isEmpty()) {
            if (callback != null) {
                callback.onUiFailure(-1, "没有日志文件");
            }
            return;
        }
        Context context = ChatManager.Instance().getApplicationContext();
        if (context == null) {
            if (callback != null) {
                callback.onUiFailure(-1, "not init");
            }
            return;
        }
        SharedPreferences sp = context.getSharedPreferences("log_history", Context.MODE_PRIVATE);

        String userId = ChatManager.Instance().getUserId();
        String url = APP_SERVER_ADDRESS + "/logs/" + userId + "/upload";

        int toUploadCount = 0;
        Collections.sort(filePaths);
        for (int i = 0; i < filePaths.size(); i++) {
            String path = filePaths.get(i);
            File file = new File(path);
            if (!file.exists()) {
                continue;
            }
            // 重复上传最后一个日志文件，因为上传之后，还会追加内容
            if (!sp.contains(path) || i == filePaths.size() - 1) {
                toUploadCount++;
                OKHttpHelper.upload(url, null, file, MediaType.get("application/octet-stream"), new SimpleCallback<Void>() {
                    @Override
                    public void onUiSuccess(Void aVoid) {
                        if (callback != null) {
                            callback.onSuccess(url);
                        }
                        sp.edit().putBoolean(path, true).commit();
                    }

                    @Override
                    public void onUiFailure(int code, String msg) {
                        if (callback != null) {
                            callback.onUiFailure(code, msg);
                        }
                    }
                });
            }
        }
        if (toUploadCount == 0) {
            if (callback != null) {
                callback.onUiFailure(-1, "所有日志都已上传");
            }
        }
    }

    public static void validateConfig(Context context) {
        if (TextUtils.isEmpty(Config.IM_SERVER_HOST)
            || Config.IM_SERVER_HOST.startsWith("http")
            || Config.IM_SERVER_HOST.contains(":")
            || TextUtils.isEmpty(APP_SERVER_ADDRESS)
            || (!APP_SERVER_ADDRESS.startsWith("http") && !APP_SERVER_ADDRESS.startsWith("https"))
            || Config.IM_SERVER_HOST.equals("127.0.0.1")
            || APP_SERVER_ADDRESS.contains("127.0.0.1")
            || (!Config.IM_SERVER_HOST.contains("wildfirechat.net") && APP_SERVER_ADDRESS.contains("wildfirechat.net"))
            || (Config.IM_SERVER_HOST.contains("wildfirechat.net") && !APP_SERVER_ADDRESS.contains("wildfirechat.net"))
        ) {
            Toast.makeText(context, "配置错误，请检查配置，应用即将关闭...", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> {
                throw new IllegalArgumentException("config error\n 参数配置错误\n请仔细阅读配置相关注释，并检查配置!\n");
            }, 5 * 1000);
        }

        for (String[] ice : Config.ICE_SERVERS) {
            if (!ice[0].startsWith("turn")) {
                Toast.makeText(context, "Turn配置错误，请检查配置，应用即将关闭...", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(() -> {
                    throw new IllegalArgumentException("config error\n 参数配置错误\n请仔细阅读配置相关注释，并检查配置!\n");
                }, 5 * 1000);
            }
        }
    }
}
