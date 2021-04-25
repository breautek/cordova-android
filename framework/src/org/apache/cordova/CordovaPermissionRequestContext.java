/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package org.apache.cordova;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class CordovaPermissionRequestContext {
    private int id;
    private ICordovaPermissionRequestCallback callback;
    private HashMap<String, Integer> grantResults;

    public CordovaPermissionRequestContext(int id, String[] permissions, ICordovaPermissionRequestCallback callback) {
        this.id = id;
        this.callback = callback;
        this.grantResults = new HashMap();

        for (String permission : permissions) {
            this.grantResults.put(permission, null);
        }
    }

    public synchronized void setGrantResult(String permission, int result) {
        this.grantResults.put(permission, result);
    }

    public HashMap<String, Integer> getGrantResults() {
        return this.grantResults;
    }

    public void fireCallback() {
        this.callback.run(this.grantResults);
    }

    public int getID() {
        return this.id;
    }

    public boolean hasCompleteGrantResult() {
        Set<Map.Entry<String, Integer>> grantSet = this.grantResults.entrySet();
        Iterator<Map.Entry<String, Integer>> iterator = grantSet.iterator();

        boolean retValue = true;

        while (iterator.hasNext() && retValue) {
            Map.Entry<String, Integer> item = iterator.next();
            if (item.getValue() == null) {
                retValue = false;
            }
        }

        return retValue;
    }
}
