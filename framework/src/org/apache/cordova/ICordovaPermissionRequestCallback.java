package org.apache.cordova;

import java.util.HashMap;
interface ICordovaPermissionRequestCallback {
    void run(HashMap<String, Integer> grantResults);
}
