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
import android.app.Fragment;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.mediabrowserservice.utils.LogHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class that shows the Media Queue to the user.
 */
public class QueueFragment extends Fragment
        implements CommonApiConnection.OnAudioPauseRequestedListener {

    private static final String TAG = LogHelper.makeLogTag(QueueFragment.class.getSimpleName());
    private static Presenter presenter;

    private ImageButton mSkipNext;
    private ImageButton mSkipPrevious;
    private ImageButton mPlayPause;

    private CommonApiConnection mApiConnection;

    private MediaBrowser mMediaBrowser;
    private MediaController.TransportControls mTransportControls;
    private MediaController mMediaController;
    private PlaybackState mPlaybackState;

    private ImageView mCover;
    private TextView mTitle;
    private TextView mAuthor;
    private ProgressBar mWaitIndicator;
    private ProgressBar mProgressBar;
    private QueueAdapter mQueueAdapter;

    private MediaBrowser.ConnectionCallback mConnectionCallback =
            new MediaBrowser.ConnectionCallback() {
                @Override
                public void onConnected() {
                    LogHelper.d(TAG, "onConnected: session token ", mMediaBrowser.getSessionToken());

                    if (mMediaBrowser.getSessionToken() == null) {
                        throw new IllegalArgumentException("No Session token");
                    }

                    mMediaController = new MediaController(getActivity(),
                            mMediaBrowser.getSessionToken());
                    mTransportControls = mMediaController.getTransportControls();
                    mMediaController.registerCallback(mSessionCallback);

                    getActivity().setMediaController(mMediaController);
                    mPlaybackState = mMediaController.getPlaybackState();

                    List<MediaSession.QueueItem> queue = mMediaController.getQueue();
                    if (queue != null) {
                        mQueueAdapter.clear();
                        mQueueAdapter.notifyDataSetInvalidated();
                        mQueueAdapter.addAll(queue);
                        mQueueAdapter.notifyDataSetChanged();
                    }
                    onPlaybackStateChanged(mPlaybackState);
                }

                @Override
                public void onConnectionFailed() {
                    LogHelper.d(TAG, "onConnectionFailed");
                }

                @Override
                public void onConnectionSuspended() {
                    LogHelper.d(TAG, "onConnectionSuspended");
                    mMediaController.unregisterCallback(mSessionCallback);
                    mTransportControls = null;
                    mMediaController = null;
                    getActivity().setMediaController(null);
                }
            };

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private MediaController.Callback mSessionCallback = new MediaController.Callback() {

        @Override
        public void onSessionDestroyed() {
            LogHelper.d(TAG, "Session destroyed. Need to fetch a new Media Session");
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            if (state == null) {
                return;
            }
            LogHelper.d(TAG, "Received playback state change to state ", state.getState());
            mPlaybackState = state;
            QueueFragment.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onQueueChanged(List<MediaSession.QueueItem> queue) {
            LogHelper.d(TAG, "onQueueChanged ", queue);
            if (queue != null) {
                mQueueAdapter.clear();
                mQueueAdapter.notifyDataSetInvalidated();
                mQueueAdapter.addAll(queue);
                mQueueAdapter.notifyDataSetChanged();
            }
        }
    };
    private MediaObserver observer;

    public static QueueFragment newInstance() {
        QueueFragment queueFragment = new QueueFragment();
        presenter = MusicPlayerActivity.presenter;
        presenter.setQueueFragment(queueFragment);
        return queueFragment;
    }

    public void setCommonApiConnection(CommonApiConnection connection) {
        mApiConnection = connection;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_media_player, container, false);

        mSkipPrevious = (ImageButton) rootView.findViewById(R.id.skip_previous);
        mSkipPrevious.setEnabled(false);
        mSkipPrevious.setOnClickListener(mButtonListener);

        mSkipNext = (ImageButton) rootView.findViewById(R.id.skip_next);
        mSkipNext.setEnabled(false);
        mSkipNext.setOnClickListener(mButtonListener);

        mPlayPause = (ImageButton) rootView.findViewById(R.id.play_pause);
        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mButtonListener);

        mWaitIndicator = (ProgressBar) rootView.findViewById(R.id.wait_indicator);

        mCover = (ImageView) rootView.findViewById(R.id.iv_cover);

        mTitle = (TextView) rootView.findViewById(R.id.tv_title);

        mAuthor = (TextView) rootView.findViewById(R.id.tv_author);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

        mQueueAdapter = new QueueAdapter(getActivity());

        mMediaBrowser = new MediaBrowser(getActivity(),
                new ComponentName(getActivity(), MusicService.class),
                mConnectionCallback, null);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
        }
        changeCover();
        if(presenter.getPlayback().isPlaying())
        {
            runProgressBar();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(observer!=null)
        {
            observer.stop();
        }
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mSessionCallback);
        }
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
    }


    private void onPlaybackStateChanged(PlaybackState state) {
        LogHelper.d(TAG, "onPlaybackStateChanged ", state);
        if (state == null) {
            return;
        }
        mQueueAdapter.setActiveQueueItemId(state.getActiveQueueItemId());
        mQueueAdapter.notifyDataSetChanged();
        StringBuilder statusBuilder = new StringBuilder();
        switch (state.getState()) {
            case PlaybackState.STATE_PLAYING:
                statusBuilder.append("playing");
                break;
            case PlaybackState.STATE_PAUSED:
                statusBuilder.append("paused");
                break;
            case PlaybackState.STATE_STOPPED:
                statusBuilder.append("ended");
                break;
            case PlaybackState.STATE_ERROR:
                statusBuilder.append("error: ").append(state.getErrorMessage());
                break;
            case PlaybackState.STATE_BUFFERING:
                statusBuilder.append("buffering");
                break;
            case PlaybackState.STATE_NONE:
                statusBuilder.append("none");
                break;
            case PlaybackState.STATE_CONNECTING:
                statusBuilder.append("connecting");
                break;
            default:
                statusBuilder.append(mPlaybackState);
        }
        statusBuilder.append(" -- At position: ").append(state.getPosition());
        LogHelper.d(TAG, statusBuilder.toString());

        updateMediaButton(state.getState());
        notifyCommonApi(state.getState());

        mSkipPrevious.setEnabled((state.getActions() & PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0);
        mSkipNext.setEnabled((state.getActions() & PlaybackState.ACTION_SKIP_TO_NEXT) != 0);

        LogHelper.d(TAG, "Queue From MediaController *** Title " +
                mMediaController.getQueueTitle() + "\n: Queue: " + mMediaController.getQueue() +
                "\n Metadata " + mMediaController.getMetadata());
    }

    private void notifyCommonApi(int state) {
        if (mApiConnection != null) {
            final boolean isPlaying = state == PlaybackState.STATE_PLAYING;
            mApiConnection.setAudioContext(isPlaying);
        }
    }

    private void updateMediaButton(int playbackState) {
        final boolean waitIndicatorVisible
                =  playbackState == PlaybackState.STATE_BUFFERING
                || playbackState == PlaybackState.STATE_CONNECTING
                || playbackState == PlaybackState.STATE_NONE
                || playbackState == PlaybackState.STATE_ERROR
                || playbackState == PlaybackState.STATE_STOPPED
                ;

        mWaitIndicator.setVisibility(waitIndicatorVisible ? View.VISIBLE : View.INVISIBLE);
        mWaitIndicator.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

        final boolean isPlaying = playbackState == PlaybackState.STATE_PLAYING;
        mPlayPause.setImageDrawable(getMediaButtonIcon(isPlaying, waitIndicatorVisible));
    }

    private Drawable getMediaButtonIcon(boolean isPlaying, boolean waiting) {
        final Activity activity = getActivity();
        if (waiting) {
            return null;
        } else if (isPlaying) {
            return activity.getDrawable(R.drawable.ic_pause);
        } else {
            return activity.getDrawable(R.drawable.ic_play);
        }
    }

    private View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int state = mPlaybackState == null ?
                    PlaybackState.STATE_NONE : mPlaybackState.getState();
            switch (v.getId()) {
                case R.id.play_pause:
                    LogHelper.d(TAG, "Play button pressed, in state " + state);
                    if (state == PlaybackState.STATE_PAUSED ||
                            state == PlaybackState.STATE_STOPPED ||
                            state == PlaybackState.STATE_NONE) {
                        playMedia();
                    } else if (state == PlaybackState.STATE_PLAYING) {
                        pauseMedia();
                    }
                    break;
                case R.id.skip_previous:
                    LogHelper.d(TAG, "Start button pressed, in state " + state);
                    skipToPrevious();
                    break;
                case R.id.skip_next:
                    skipToNext();
                    break;
            }
        }
    };

    private void playMedia() {
        if (mTransportControls != null) {
            mTransportControls.play();
        }
    }

    private void pauseMedia() {
        if (mTransportControls != null) {
            mTransportControls.pause();
        }
    }

    private void skipToPrevious() {
        if (mTransportControls != null) {
            mTransportControls.skipToPrevious();
        }
    }

    private void skipToNext() {

        if (mTransportControls != null) {
            mTransportControls.skipToNext();
        }
    }

    public void changeCover() {
        if (mMediaController == null) {
            return;
        }
        MediaMetadata mediaMetadata = mMediaController.getMetadata();

        MediaDescription description = mediaMetadata.getDescription();
        String fetchArtUrl = null;
        Bitmap art = null;
        if (description.getIconUri() != null) {
            // This sample assumes the iconUri will be a valid URL formatted String, but
            // it can actually be any valid Android Uri formatted String.
            // async fetch the album art icon
            String artUrl = description.getIconUri().toString();
            art = AlbumArtCache.getInstance().getBigImage(artUrl);
            if (art == null) {
                fetchArtUrl = artUrl;
                // use a placeholder art while the remote art is being downloaded
                art = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_default_art);
            }
            mCover.setImageBitmap(art);
        }
        mTitle.setText(description.getTitle());
        mAuthor.setText(description.getSubtitle());
    }

    public void runProgressBar() {
        MediaPlayer mediaPlayer = presenter.getPlayback().getMediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                observer.stop();
                mProgressBar.setProgress(mp.getCurrentPosition());
                skipToNext();
            }
        });
        if (observer != null) {
            observer.stop();
        }
        observer = new MediaObserver();
        new Thread(observer).start();
    }

    @Override
    public void audioPauseRequested() {
        if (mTransportControls != null) {
            mTransportControls.pause();
        }
    }

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            while (!stop.get()) {
                MediaPlayer mediaPlayer = presenter.getPlayback().getMediaPlayer();
                if (mediaPlayer == null) {
                    this.stop();
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    mProgressBar.setMax(mediaPlayer.getDuration());
                    mProgressBar.setProgress(mediaPlayer.getCurrentPosition());
                    Log.d("QueueFragment", "duration=" + mediaPlayer.getDuration());
                    Log.d("QueueFragment", "currentPosition=" + mediaPlayer.getCurrentPosition());
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
