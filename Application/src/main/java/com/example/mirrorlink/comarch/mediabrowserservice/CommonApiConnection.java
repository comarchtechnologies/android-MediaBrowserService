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
package com.example.mirrorlink.comarch.mediabrowserservice;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.mirrorlink.android.commonapi.Defs;
import com.mirrorlink.android.commonapi.ICommonAPIService;

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
