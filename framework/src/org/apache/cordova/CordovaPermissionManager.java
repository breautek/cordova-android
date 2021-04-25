package org.apache.cordova;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * TODO(breautek): documentation
 */
class CordovaPermissionManager {
    private static final String LOG_TAG = CordovaPermissionManager.class.getName();

    protected AppCompatActivity activity;
    protected HashMap<Integer, CordovaPermissionRequestContext> contexts;
    private int requestCode = 0;

    public CordovaPermissionManager(AppCompatActivity activity) {
        this.activity = activity;
        this.contexts = new HashMap();
    }

    protected synchronized int getRequestCode() {
        return requestCode++;
    }

    protected CordovaPermissionRequestContext buildCallbackContext(String[] permissions, ICordovaPermissionRequestCallback callback) {
        return new CordovaPermissionRequestContext(this.getRequestCode(), permissions, callback);
    }

    public void requestPermissions(String[] permissions, ICordovaPermissionRequestCallback callback) {
        CordovaPermissionRequestContext context = this.buildCallbackContext(permissions, callback);

        this.contexts.put(context.getID(), context);

        ArrayList<String> needToRequest = new ArrayList<String>();

        for (String permission : permissions) {
            if (!this.hasPermission(permission)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this.activity, permission)) {
                    needToRequest.add(permission);
                }
                else {
                    // the user as previously rejected permission
                    context.setGrantResult(permission, PackageManager.PERMISSION_DENIED);
                }
            }
            else {
                context.setGrantResult(permission, PackageManager.PERMISSION_GRANTED);
            }
        }

        if (needToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this.activity, needToRequest.toArray(new String[needToRequest.size()]), context.getID());
        }
        else {
            // We already have permission for everything... so we can simply callback
            this.fireCallback(requestCode);
        }
    }

    protected void fireCallback(int requestCode) {
        CordovaPermissionRequestContext context = this.contexts.get(requestCode);
        if (context.hasCompleteGrantResult()) {
            context.fireCallback();
        }
    }

    protected synchronized void fireCallback(int requestCode, HashMap<String, Integer> grantResults) {
        CordovaPermissionRequestContext context = this.contexts.get(requestCode);
        this.contexts.remove(requestCode);

        Set<Map.Entry<String, Integer>> grantSet = grantResults.entrySet();
        Iterator<Map.Entry<String, Integer>> iterator = grantSet.iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Integer> item = iterator.next();
            context.setGrantResult(item.getKey(), item.getValue());
        }

        this.fireCallback(requestCode);
    }

    public boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(permission);
        }
        else {
            return true;
        }
    }

    protected HashMap<String, Integer> getGrantResultsMap(String[] permissions) {
        HashMap<String, Integer> grantResults = new HashMap();
        for (String permission : permissions) {
            grantResults.put(permission, PackageManager.PERMISSION_GRANTED);
        }
        return grantResults;
    }

    protected HashMap<String, Integer> getGrantResultsMap(String[] permissions, int[] grantResults) {
        HashMap<String, Integer> grantResultsMap = new HashMap();

        for (int i = 0; i < permissions.length; i++) {
            grantResultsMap.put(permissions[i], grantResults[i]);
        }

        return grantResultsMap;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        this.fireCallback(requestCode, this.getGrantResultsMap(permissions, grantResults));
    }
}
