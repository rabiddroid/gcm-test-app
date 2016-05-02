/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.rabiddroid.gcm.testharness;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class RefreshTokenService extends IntentService {

    private final DeviceToken deviceToken;

    public RefreshTokenService() {
        super(LoggingPreferences.TAG);
        deviceToken = new DeviceToken();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LoggingPreferences.TAG, "RefreshTokenService:onHandleIntent");
        try {

            //Delete token and request new one
            InstanceID instanceID = InstanceID.getInstance(this);
            instanceID.deleteToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE);

            /*String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(LoggingPreferences.TAG, "GCM Registration Token: " + token);

            String existingToken = deviceToken.get(this);
            if (!existingToken.isEmpty() && !existingToken.equals(token)) {
                Log.d(LoggingPreferences.TAG, "Token has changed: " + token);
                Toast tokenHasChangedToast = Toast.makeText(getApplicationContext(),
                        "New device token received.",
                        Toast.LENGTH_LONG);
                tokenHasChangedToast.show();


                deviceToken.save(this, token);
            }*/

            Intent registrationServiceIntent = new Intent(getApplicationContext(), RegistrationIntentService.class);
            startService(registrationServiceIntent);

            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(LoggingPreferences.TAG, "Failed to complete token refresh", e);
        }

    }

}
