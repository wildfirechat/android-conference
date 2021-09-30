package cn.wildfirechat.zoom.app;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.king.zxing.Intents;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.zoom.R;
import cn.wildfirechat.zoom.WfcBaseNoToolbarActivity;
import cn.wildfirechat.zoom.qrcode.ScanQRCodeActivity;
import cn.wildfirechat.zoom.voip.conference.ConferenceInfoActivity;
import cn.wildfirechat.zoom.voip.conference.CreateConferenceActivity;
import cn.wildfirechat.zoom.voip.conference.OrderConferenceActivity;

public class MainActivity extends WfcBaseNoToolbarActivity {

    @BindView(R.id.portraitImageView)
    ImageView portraitImageView;
    @BindView(R.id.nameTextView)
    TextView nameTextView;

    private static final int REQUEST_CODE_SCAN_QR_CODE = 100;

    @Override
    protected int contentLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        UserInfo userInfo = ChatManager.Instance().getUserInfo(ChatManager.Instance().getUserId(), false);
        RequestOptions options = new RequestOptions()
            .placeholder(R.mipmap.avatar_def)
            .transforms(new CenterCrop(), new RoundedCorners(100));
        Glide.with(this)
            .load(userInfo.portrait)
            .apply(options)
            .into(portraitImageView);
        nameTextView.setText(userInfo.displayName);
    }

    @OnClick(R.id.startConferenceLinearLayout)
    void startConference() {
        Intent intent = new Intent(this, CreateConferenceActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.scanQrImageView)
    void scanQRCode() {
        String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission(permissions)) {
                requestPermissions(permissions, 100);
                return;
            }
        }
        startActivityForResult(new Intent(this, ScanQRCodeActivity.class), REQUEST_CODE_SCAN_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SCAN_QR_CODE:
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra(Intents.Scan.RESULT);
                    onScanPcQrCode(result);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void onScanPcQrCode(String qrcode) {
        String prefix = qrcode.substring(0, qrcode.lastIndexOf('/') + 1);
        String value = qrcode.substring(qrcode.lastIndexOf("/") + 1);
        switch (prefix) {
            case "wfzoom://":
                String[] pairs = value.split("&");
                Map<String, String> queryPairs = new HashMap<>();
                for (String pair : pairs) {
                    int index = pair.indexOf("=");
                    queryPairs.put(pair.substring(0, index), pair.substring(index + 1));
                }
                String conferenceId = queryPairs.get("id");
                String password = queryPairs.get("pwd");
                Intent intent = new Intent(this, ConferenceInfoActivity.class);
                intent.putExtra("conferenceId", conferenceId);
                intent.putExtra("password", password);
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, "qrcode: " + qrcode, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @OnClick(R.id.joinConferenceLinearLayout)
    void joinConference() {
        View view = LayoutInflater.from(this).inflate(R.layout.join_conference_dialog, null);
        new MaterialDialog.Builder(this)
            .customView(view, false)
            .cancelable(false)
            .negativeText("取消")
            .positiveText("确认")
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    EditText callIdEditText = view.findViewById(R.id.callIdEditText);
                    EditText passwordEditText = view.findViewById(R.id.passwordEditText);
                    Intent intent = new Intent(MainActivity.this, ConferenceInfoActivity.class);
                    intent.putExtra("conferenceId", callIdEditText.getText().toString());
                    intent.putExtra("password", passwordEditText.getText().toString());
                    startActivity(intent);
                }
            })
            .build()
            .show();
    }

    @OnClick(R.id.orderConferenceLinearLayout)
    void orderConference() {
        Intent intent = new Intent(this, OrderConferenceActivity.class);
        startActivity(intent);
    }
}