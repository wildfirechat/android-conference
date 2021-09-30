/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.zoom.voip.conference;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;
import cn.wildfirechat.zoom.R;
import cn.wildfirechat.zoom.UserViewModel;
import cn.wildfirechat.zoom.WfcBaseActivity;
import cn.wildfirechat.zoom.app.AppService;
import cn.wildfirechat.zoom.model.ConferenceInfo;
import cn.wildfirechat.zoom.widget.DateTimePickerHelper;
import cn.wildfirechat.zoom.widget.FixedTextInputEditText;

public class CreateConferenceActivity extends WfcBaseActivity {
    @BindView(R.id.conferenceTitleTextInputEditText)
    FixedTextInputEditText titleEditText;
    @BindView((R.id.audienceSwitch))
    SwitchMaterial audienceSwitch;
    @BindView((R.id.modeSwitch))
    SwitchMaterial modeSwitch;
    @BindView((R.id.advanceSwitch))
    SwitchMaterial advancedSwitch;
    @BindView(R.id.callIdSwitch)
    SwitchMaterial callIdSwitch;

    @BindView(R.id.joinConferenceBtn)
    Button joinConferenceButton;

    @BindView(R.id.endDateTimeTextView)
    TextView endDateTimeTextView;
    @BindView(R.id.callIdTextView)
    TextView callIdTextView;

    private Date endDateTime;

    private MenuItem createConferenceMenuItem;

    private String title;
    private String conferenceId;
    private String password;
    private String callId;
    private boolean enableVideo = false;
    private boolean enableAudio = true;

    private static final String TAG = "createConference";

    @Override
    protected int contentLayout() {
        return R.layout.create_conference_activity;
    }

    @Override
    protected int menu() {
        return R.menu.create_conference;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void afterMenus(Menu menu) {
        createConferenceMenuItem = menu.findItem(R.id.create);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create) {
            createConference(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        UserInfo userInfo = userViewModel.getUserInfo(ChatManager.Instance().getUserId(), false);
        if (userInfo != null) {
            titleEditText.setText(userInfo.displayName + "的会议");
        } else {
            titleEditText.setText("会议");
        }
        advancedSwitch.setChecked(false);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        endDateTime = calendar.getTime();
        endDateTimeTextView.setText(endDateTime.toString());

        AppService.Instance().getMyPrivateConferenceId(new GeneralCallback2() {
            @Override
            public void onSuccess(String s) {
                conferenceId = s;
                callIdTextView.setText(conferenceId);
            }

            @Override
            public void onFail(int i) {

            }
        });
    }

    @OnCheckedChanged(R.id.audienceSwitch)
    void audienceChecked(CompoundButton button, boolean checked) {
        if (checked) {
            modeSwitch.setChecked(true);
            modeSwitch.setEnabled(false);
        } else {
            modeSwitch.setChecked(true);
            modeSwitch.setEnabled(true);
        }
    }

    @OnCheckedChanged(R.id.passwordSwitch)
    void passwordChecked(CompoundButton button, boolean checked) {
        if (checked) {
            new MaterialDialog.Builder(this)
                .content("请输入密码")
                .input("请输入6位数字", "123456", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        password = input.toString();
                    }
                })
                .inputRange(6, 6)
                .inputType(2)
                .cancelable(false)
                .build()
                .show();
        } else {
            password = null;
        }
    }

    @OnTextChanged(value = R.id.conferenceTitleTextInputEditText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void conferenceTitle(Editable editable) {
        this.title = editable.toString();
        if (!TextUtils.isEmpty(title)) {
            joinConferenceButton.setEnabled(true);
            if (createConferenceMenuItem != null) {
                createConferenceMenuItem.setEnabled(true);
            }
        } else {
            joinConferenceButton.setEnabled(false);
            if (createConferenceMenuItem != null) {
                createConferenceMenuItem.setEnabled(false);
            }
        }
    }

    @OnClick(R.id.endDateTimeRelativeLayout)
    void pickEndDateTime() {
        DateTimePickerHelper.pickDateTime(this, new DateTimePickerHelper.PickDateTimeCallBack() {
            @Override
            public void onPick(Date date) {
                endDateTimeTextView.setText(date.toString());
                endDateTime = date;
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @OnClick(R.id.joinConferenceBtn)
    public void onClickJoinBtn() {
        createConference(true);
    }

    private void createConference(boolean join) {
        ConferenceInfo info = new ConferenceInfo();
        if (callIdSwitch.isChecked()) {
            info.setConferenceId(conferenceId);
        }
        info.setPassword(password);
        info.setConferenceTitle(titleEditText.getText().toString());
        Random random = new Random();
        String pin = String.format("%d%d%d%d", random.nextInt() % 10, random.nextInt() % 10, random.nextInt() % 10, random.nextInt() % 10);
        info.setPin(pin);

        info.setOwner(ChatManager.Instance().getUserId());
        info.setStartTime(System.currentTimeMillis() / 1000);
        info.setEndTime(endDateTime.getTime() / 1000);

        AppService.Instance().createConference(info, new GeneralCallback2() {
            @Override
            public void onSuccess(String s) {
                if (join) {
                    AVEngineKit.CallSession session = AVEngineKit.Instance().startConference(info.getConferenceId(), false, info.getPin(), ChatManager.Instance().getUserId(), info.getConferenceTitle(), "", audienceSwitch.isChecked(), advancedSwitch.isChecked(), false, null);
                    if (session != null) {
                        session.muteAudio(!enableAudio);
                        session.muteVideo(!enableVideo);
                        Intent intent = new Intent(CreateConferenceActivity.this, ConferenceActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(CreateConferenceActivity.this, "创建会议失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    finish();
                }
            }

            @Override
            public void onFail(int i) {
                Log.e(TAG, "createConference fail" + i);
            }
        });
    }


}
