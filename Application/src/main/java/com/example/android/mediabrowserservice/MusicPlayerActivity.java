/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.mediabrowserservice;

import android.app.Activity;
import android.media.browse.MediaBrowser;
import android.os.Bundle;

import android.view.View;

/**
 * Main activity for the music player.
 */
public class MusicPlayerActivity extends Activity
        implements BrowseFragment.FragmentDataHelper {
    public static Presenter presenter;
    private final String mMediaId = "__BY_GENRE__/Rock|-1679589699";


    /* Enable immersive mode (hide software buttons on the bottom of the screen).
     * This is considered to be a good MirrorLink app practice since the head unit
     * is obliged to provide controls for Android buttons.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        final int flags
                = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (presenter == null) {
            presenter = new Presenter();
        }
        setContentView(R.layout.activity_player);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, BrowseFragment.newInstance(null))
                    .commit();
        }
    }

    @Override
    public void onMediaItemSelected(MediaBrowser.MediaItem item) {
        getMediaController().getTransportControls().playFromMediaId(mMediaId, null);
        QueueFragment queueFragment = QueueFragment.newInstance();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, queueFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        presenter.getPlayback().getMediaPlayer().stop();
        finish();
    }
}
