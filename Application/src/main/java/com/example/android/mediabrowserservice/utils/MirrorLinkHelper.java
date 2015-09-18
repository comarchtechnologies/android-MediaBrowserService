package com.example.android.mediabrowserservice.utils;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.mirrorlink.android.commonapi.Defs;

import java.util.List;

/**
 * Created by belickim on 18/09/15.
 */
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
