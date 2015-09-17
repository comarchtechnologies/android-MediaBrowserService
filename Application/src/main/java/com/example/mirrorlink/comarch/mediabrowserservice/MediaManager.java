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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;

import com.example.mirrorlink.comarch.mediabrowserservice.MusicService;
import com.example.mirrorlink.comarch.mediabrowserservice.utils.LogHelper;

/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */
public class MediaManager extends BroadcastReceiver {
    private static final String TAG = LogHelper.makeLogTag(MediaManager.class);

    public static final String ACTION_PAUSE = "com.example.android.mediabrowserservice.pause";
    public static final String ACTION_PLAY = "com.example.android.mediabrowserservice.play";
    public static final String ACTION_PREV = "com.example.android.mediabrowserservice.prev";
    public static final String ACTION_NEXT = "com.example.android.mediabrowserservice.next";

    private final MusicService mService;
    private final Presenter presenter;
    private MediaSession.Token mSessionToken;
    private MediaController mController;
    private MediaController.TransportControls mTransportControls;


    private boolean mStarted = false;

    public MediaManager(MusicService service) {
        presenter = MusicPlayerActivity.presenter;
        mService = service;
        updateSessionToken();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogHelper.d(TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PAUSE:
                mTransportControls.pause();
                break;
            case ACTION_PLAY:
                mTransportControls.play();
                break;
            case ACTION_NEXT:
                mTransportControls.skipToNext();
                break;
            case ACTION_PREV:
                mTransportControls.skipToPrevious();
                break;
            default:
                LogHelper.w(TAG, "Unknown intent ignored. Action=", action);
        }
    }

    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     * (see {@link android.media.session.MediaController.Callback#onSessionDestroyed()})
     */
    private void updateSessionToken() {
        MediaSession.Token freshToken = mService.getSessionToken();
        if (mSessionToken == null || !mSessionToken.equals(freshToken)) {
            if (mController != null) {
                mController.unregisterCallback(mCb);
            }
            mSessionToken = freshToken;
            mController = new MediaController(mService, mSessionToken);
            mTransportControls = mController.getTransportControls();
            if (mStarted) {
                mController.registerCallback(mCb);
            }
        }
    }


    private final MediaController.Callback mCb = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            LogHelper.d(TAG, "Received new playback state", state);
            if (state.getState() == PlaybackState.STATE_PLAYING) {
            }
            if (state != null && (state.getState() == PlaybackState.STATE_STOPPED ||
                    state.getState() == PlaybackState.STATE_NONE)) {
            } else {

            }
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            presenter.getQueueFragment().changeCover();
            LogHelper.d(TAG, "Received new metadata ", metadata);

        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            LogHelper.d(TAG, "Session was destroyed, resetting to the new session token");
            updateSessionToken();
        }
    };


}
