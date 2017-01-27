/*
 *    Copyright 2016 Barracks Solutions Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.barracks.ota.client.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import io.barracks.ota.client.UpdateCheckService;
import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDetailsRequest;

/**
 * A helper which makes it easier to use the {@link UpdateCheckService}.
 * <p>
 * Use {@link #bind(Context, UpdateCheckCallback)} before requesting for an udpate,
 * and {@link #unbind(Context)} when you are done using the helper.
 * </p>
 */
public class UpdateCheckHelper extends BroadcastReceiver {
    private static final String TAG = UpdateCheckHelper.class.getSimpleName();
    private final String apiKey;
    private final String baseUrl;
    private Context context;
    private UpdateCheckCallback callback;

    /**
     * Helper's contstructor.
     *
     * @param apiKey The API key provided by the Barracks platform.
     */
    public UpdateCheckHelper(String apiKey) {
        this(apiKey, null);
    }

    /**
     * Helper's contstructor.
     *
     * @param apiKey  The API key provided by the Barracks platform.
     * @param baseUrl The base URL for the Barracks platform.
     */
    public UpdateCheckHelper(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case UpdateCheckService.ACTION_CHECK:
                if (callback.hashCode() == intent.getIntExtra(UpdateCheckService.EXTRA_CALLBACK, 0)) {
                    if (intent.hasCategory(UpdateCheckService.UPDATE_REQUEST_ERROR)) {
                        callback.onUpdateRequestError(
                                (UpdateDetailsRequest) intent.getParcelableExtra(UpdateCheckService.EXTRA_REQUEST),
                                (Throwable) intent.getSerializableExtra(UpdateCheckService.EXTRA_EXCEPTION)
                        );
                    } else if (intent.hasCategory(UpdateCheckService.UPDATE_AVAILABLE)) {
                        callback.onUpdateAvailable(
                                (UpdateDetailsRequest) intent.getParcelableExtra(UpdateCheckService.EXTRA_REQUEST),
                                (UpdateDetails) intent.getParcelableExtra(UpdateCheckService.EXTRA_UPDATE_DETAILS)
                        );
                    } else if (intent.hasCategory(UpdateCheckService.UPDATE_UNAVAILABLE)) {
                        callback.onUpdateUnavailable(
                                (UpdateDetailsRequest) intent.getParcelableExtra(UpdateCheckService.EXTRA_REQUEST)
                        );
                    }
                }
                break;
        }
    }

    /**
     * Call this method to register your callback before checking for an update.
     *
     * @param context  The context.
     * @param callback The {@link UpdateCheckCallback} which will be called during the request.
     */
    public void bind(Context context, UpdateCheckCallback callback) {
        this.context = context;
        this.callback = callback;
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(this, UpdateCheckService.ACTION_CHECK_FILTER);
    }

    /**
     * Call this method to unregister your callback and free the resources when you are done with
     * this helper.
     *
     * @param context The context.
     */
    public void unbind(Context context) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.unregisterReceiver(this);
        this.context = null;
        this.callback = null;
    }

    /**
     * Call this method to request details about an update to the Barracks platform.
     *
     * @param request The request to be sent to the Barracks platform.
     */
    public void requestUpdate(UpdateDetailsRequest request) {
        Intent intent = new Intent(context, UpdateCheckService.class)
                .setAction(UpdateCheckService.ACTION_CHECK)
                .putExtra(UpdateCheckService.EXTRA_API_KEY, apiKey)
                .putExtra(UpdateCheckService.EXTRA_URL, baseUrl)
                .putExtra(UpdateCheckService.EXTRA_CALLBACK, callback.hashCode())
                .putExtra(UpdateCheckService.EXTRA_REQUEST, request);
        context.startService(intent);
    }
}
