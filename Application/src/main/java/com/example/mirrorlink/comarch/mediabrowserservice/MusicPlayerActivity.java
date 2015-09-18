/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (C) 2015 Comarch Technologies
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
package com.example.mirrorlink.comarch.mediabrowserservice;

import android.app.Activity;
import android.media.browse.MediaBrowser;
import android.os.Bundle;

/**
 * Main activity for the music player.
 */
public class MusicPlayerActivity extends Activity
        implements BrowseFragment.FragmentDataHelper {
    public static Presenter presenter;
    private final String mMediaId = "__BY_GENRE__/Rock|-1679589699";

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