/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.zoom.voip;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.zoom.voip.conference.ConferenceActivity;

public class VoipDummyActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AVEngineKit.CallSession session = AVEngineKit.Instance().getCurrentSession();
        if (session == null || session.getState() == AVEngineKit.CallState.Idle) {
            finish();
        } else if (session.isConference()) {
            Intent intent = new Intent(this, ConferenceActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
