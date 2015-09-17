package com.example.mirrorlink.comarch.mediabrowserservice;
/*
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
