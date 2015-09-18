package com.example.android.mediabrowserservice;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.mirrorlink.android.commonapi.Defs;
import com.mirrorlink.android.commonapi.ICommonAPIService;

/**
 * Created by belickim on 18/09/15.
 */
public class CommonApiConnection implements ServiceConnection {
    ICommonAPIService mService;
    final String mPackageName;

    public CommonApiConnection(String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("Package name cannot be null.");
        }
        mPackageName = packageName;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ICommonAPIService mService = ICommonAPIService.Stub.asInterface(service);
        try {
            mService.applicationStarted(mPackageName, 1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }
}
