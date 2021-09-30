/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.zoom.voip.conference;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class OrderConferenceActivity extends WfcBaseActivity {
    @BindView(R.id.conferenceTitleTextInputEditText)
    FixedTextInputEditText titleEditText;
    @BindView((R.id.audienceSwitch))
    SwitchMaterial audienceSwitch;
    @BindView((R.id.modeSwitch))
    SwitchMaterial modeSwitch;
    @BindView((R.id.advanceSwitch))
    SwitchMaterial advancedSwitch;

    @BindView(R.id.endDateTimeTextView)
    TextView endDateTimeTextView;
    @BindView(R.id.startDateTimeTextView)
    TextView startDateTimeTextView;

    private Date endDateTime;
    private Date startDateTime;

    private MenuItem orderConferenceMenuItem;

    private String title;
    private String password;

    private static final String TAG = "orderConference";

    @Override
    protected int contentLayout() {
        return R.layout.order_conference_activity;
    }

    @Override
    protected int menu() {
        return R.menu.order_conference;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void afterMenus(Menu menu) {
        orderConferenceMenuItem = menu.findItem(R.id.create);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create) {
            createConference();
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
            if (orderConferenceMenuItem != null) {
                orderConferenceMenuItem.setEnabled(true);
            }
        } else {
            if (orderConferenceMenuItem != null) {
                orderConferenceMenuItem.setEnabled(false);
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

    @OnClick(R.id.startDateTimeRelativeLayout)
    void pickStartDateTime() {
        DateTimePickerHelper.pickDateTime(this, new DateTimePickerHelper.PickDateTimeCallBack() {
            @Override
            public void onPick(Date date) {
                startDateTimeTextView.setText(date.toString());
                startDateTime = date;
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void createConference() {
        if (startDateTime == null || endDateTime == null) {
            Toast.makeText(this, "请选择开始、结束时间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endDateTime.before(startDateTime)) {
            Toast.makeText(this, "结束时间，不能早于开始时间", Toast.LENGTH_SHORT).show();
            return;
        }
        ConferenceInfo info = new ConferenceInfo();
        info.setPassword(password);
        info.setConferenceTitle(titleEditText.getText().toString());
        Random random = new Random();
        String pin = String.format("%d%d%d%d", random.nextInt() % 10, random.nextInt() % 10, random.nextInt() % 10, random.nextInt() % 10);
        info.setPin(pin);

        info.setOwner(ChatManager.Instance().getUserId());
        info.setStartTime(startDateTime.getTime() / 1000);
        info.setEndTime(endDateTime.getTime() / 1000);

        AppService.Instance().createConference(info, new GeneralCallback2() {
            @Override
            public void onSuccess(String s) {
                Toast.makeText(OrderConferenceActivity.this, "预定会议成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFail(int i) {
                Log.e(TAG, "createConference fail" + i);
            }
        });
    }

}
