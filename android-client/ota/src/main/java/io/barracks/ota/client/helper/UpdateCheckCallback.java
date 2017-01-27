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

import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDetailsRequest;

/**
 * This callback interface is used when requesting to the Barracks platform using the {@link UpdateCheckHelper}.
 */
public interface UpdateCheckCallback {
    /**
     * This method is called when update details are available.
     *
     * @param request The request sent to the Barracks platform.
     * @param details The details of the available update.
     */
    void onUpdateAvailable(UpdateDetailsRequest request, UpdateDetails details);

    /**
     * This method is called when there is no update available.
     *
     * @param request The request sent to the Barracks platform.
     */
    void onUpdateUnavailable(UpdateDetailsRequest request);

    /**
     * This method is called when an error occured during the request.
     *
     * @param request The request sent to the Barracks platform.
     * @param t       The exception caught during the request.
     */
    void onUpdateRequestError(UpdateDetailsRequest request, Throwable t);
}
