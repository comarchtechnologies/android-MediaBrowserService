package com.example.android.mediabrowserservice;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.mirrorlink.android.commonapi.Defs;
import com.mirrorlink.android.commonapi.ICommonAPIService;
import com.mirrorlink.android.commonapi.IConnectionListener;
import com.mirrorlink.android.commonapi.IConnectionManager;
import com.mirrorlink.android.commonapi.IContextListener;
import com.mirrorlink.android.commonapi.IContextManager;

/**
 * Created by belickim on 18/09/15.
 */
public class CommonApiConnection implements ServiceConnection {
    /* MirrorLink connection related callbacks: */
    IConnectionListener mConnectionListener = new IConnectionListener.Stub() {
        @Override
        public void onMirrorLinkSessionChanged(boolean sessionEstablished) throws RemoteException {
            /* this will be called if head unit is connected or disconnected,
             * sessionEstablished argument will be set to true when the connection is started
             */
        }

        @Override
        public void onAudioConnectionsChanged(Bundle audioConnections) throws RemoteException { }

        @Override
        public void onRemoteDisplayConnectionChanged(int remoteDisplayConnection) throws RemoteException { }
    };
    /* MirrorLink context information related callbacks: */
    IContextListener mContextListener = new IContextListener.Stub() {
        @Override
        public void onAudioBlocked(int reason) throws RemoteException {
            /* REQUIRED:
             * this will be called when the head unit blocks audio playback,
             * the application should pause playback when this is called
             */
            if (mListener != null) {
                mListener.audioPauseRequested();
            }
        }

        @Override
        public void onAudioUnblocked() throws RemoteException {
            /* OPTIONAL:
             * this will be called when the head unit stops blocking audio playback,
             * the application should resume playback if the playback was previously
             * paused by blocking event
             */
        }

        @Override
        public void onFramebufferBlocked(int reason, Bundle framebufferArea) throws RemoteException { }

        @Override
        public void onFramebufferUnblocked() throws RemoteException { }

    };

    ICommonAPIService mService;

    IConnectionManager mConnectionManager;
    IContextManager mContextManager;

    final String mPackageName;
    OnAudioPauseRequestedListener mListener;

    public CommonApiConnection(String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("Package name cannot be null.");
        }
        mPackageName = packageName;
    }

    public void setAudioPauseRequestListener(OnAudioPauseRequestedListener listener) {
        mListener = listener;
    }

    public void setAudioContext(boolean isPlaying) {
        int categories[] = { Defs.ContextInformation.APPLICATION_CATEGORY_MEDIA_MUSIC };
        try {
            mContextManager.setAudioContextInformation(isPlaying, categories, true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ICommonAPIService mService = ICommonAPIService.Stub.asInterface(service);
        try {
            mService.applicationStarted(mPackageName, 1);
            /* Registering connection and context listeners is mandatory: */
            mConnectionManager = mService.getConnectionManager(mPackageName, mConnectionListener);
            mContextManager = mService.getContextManager(mPackageName, mContextListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    public interface OnAudioPauseRequestedListener {
        void audioPauseRequested();
    };
}
