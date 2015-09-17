package com.example.android.mediabrowserservice;

/**
 * Created by sebastian.sokolowski on 2015-09-17.
 */
public class Presenter {
    private Playback playback;
    private QueueFragment queueFragment;
    public Playback getPlayback() {
        return playback;
    }

    public void setPlayback(Playback playback) {
        this.playback = playback;
    }

    public QueueFragment getQueueFragment() {
        return queueFragment;
    }

    public void setQueueFragment(QueueFragment queueFragment) {
        this.queueFragment = queueFragment;
    }
}
