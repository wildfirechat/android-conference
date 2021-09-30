package cn.wildfirechat.zoom.voip.conference;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.zoom.R;
import cn.wildfirechat.zoom.WfcBaseActivity;
import cn.wildfirechat.zoom.app.AppService;
import cn.wildfirechat.zoom.model.ConferenceInfo;

public class ConferenceInfoActivity extends WfcBaseActivity {
    private String conferenceId;
    private String password;
    private ConferenceInfo conferenceInfo;
    @BindView(R.id.titleTextView)
    TextView titleTextView;
    @BindView(R.id.ownerTextView)
    TextView ownerTextView;
    @BindView(R.id.callIdTextView)
    TextView callIdTextView;
    @BindView(R.id.startDateTimeTextView)
    TextView startDateTimeView;
    @BindView(R.id.endDateTimeTextView)
    TextView endDateTimeView;
    @BindView(R.id.audioSwitch)
    SwitchMaterial audioSwitch;
    @BindView(R.id.videoSwitch)
    SwitchMaterial videoSwitch;
    @BindView(R.id.joinConferenceBtn)
    Button joinConferenceButton;

    private MenuItem destroyItem;
    private MenuItem favItem;
    private MenuItem unFavItem;

    @Override
    protected int contentLayout() {
        return R.layout.conference_info_activity;
    }

    @Override
    protected int menu() {
        return R.menu.conference_info;
    }

    @Override
    protected void afterMenus(Menu menu) {
        super.afterMenus(menu);
        destroyItem = menu.findItem(R.id.destroy);
        favItem = menu.findItem(R.id.fav);
        unFavItem = menu.findItem(R.id.unfav);
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        Intent intent = getIntent();
        conferenceId = intent.getStringExtra("conferenceId");
        password = intent.getStringExtra("password");
        AppService.Instance().queryConferenceInfo(conferenceId, password, new AppService.QueryConferenceInfoCallback() {
            @Override
            public void onSuccess(ConferenceInfo info) {
                setupConferenceInfo(info);
            }

            @Override
            public void onFail(int code, String msg) {
                Toast.makeText(ConferenceInfoActivity.this, "获取会议详情失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.destroy) {
            AppService.Instance().destroyConference(conferenceId, new GeneralCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ConferenceInfoActivity.this, "销毁会议成功", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFail(int i) {
                    Toast.makeText(ConferenceInfoActivity.this, "销毁会议失败 " + i, Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.conferenceQRCodeLinearLayout)
    void showConferenceQRCode() {
        // TODO
    }

    @OnClick(R.id.joinConferenceBtn)
    void joinConference() {
        ConferenceInfo info = conferenceInfo;
        AVEngineKit.CallSession session = AVEngineKit.Instance().startConference(info.getConferenceId(), false, info.getPin(), ChatManager.Instance().getUserId(), info.getConferenceTitle(), "", info.isAudience(), info.isAdvance(), false, null);
        if (session != null) {
            session.muteAudio(!videoSwitch.isChecked());
            session.muteVideo(!audioSwitch.isChecked());
            Intent intent = new Intent(this, ConferenceActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "加入会议失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupConferenceInfo(ConferenceInfo info) {
        conferenceInfo = info;
        titleTextView.setText(info.getConferenceTitle());
        String owner = info.getOwner();
        String ownerName = ChatManager.Instance().getUserDisplayName(owner);
        ownerTextView.setText(ownerName);
        callIdTextView.setText(info.getConferenceId());
        startDateTimeView.setText(new Date(info.getStartTime() * 1000).toString());
        endDateTimeView.setText(new Date(info.getEndTime() * 1000).toString());

        long now = System.currentTimeMillis() / 1000;
        if (now > info.getEndTime()) {
            joinConferenceButton.setEnabled(false);
            joinConferenceButton.setText("会议已结束");
        } else if (now < info.getStartTime()) {
            joinConferenceButton.setEnabled(false);
            joinConferenceButton.setText("会议未开始");
        } else {
            joinConferenceButton.setEnabled(true);
            joinConferenceButton.setText("加入会议");
        }

        if (Objects.equals(owner, ChatManager.Instance().getUserId())) {
            destroyItem.setVisible(true);
        } else {
            // TODO
        }
    }
}
