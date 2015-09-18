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



package com.example.mirrorlink.comarch.mediabrowserservice.utils;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.mirrorlink.android.commonapi.Defs;

import java.util.List;

public class MirrorLinkHelper {

    public static boolean connectMirrorLinkService
            (Application context, ServiceConnection connection) {
        // new standard of binding Intents in Android 5.0
        Intent implicitIntent = new Intent(Defs.Intents.BIND_MIRRORLINK_API);
        Intent bindIntent = createExplicitFromImplicitIntent(context, implicitIntent);

        // verify if MirrrorLink is available - bindIntent != null
        if (bindIntent != null) {
            context.bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
            return true;
        } else {
            return false;
        }

    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
