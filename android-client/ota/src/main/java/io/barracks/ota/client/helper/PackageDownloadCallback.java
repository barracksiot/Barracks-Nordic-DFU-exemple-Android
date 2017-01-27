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

/**
 * This callback interface is used when downloading a package using the {@link PackageDownloadHelper}.
 */
public interface PackageDownloadCallback {
    /**
     * This method is called when the package has been successfully downloaded.
     *
     * @param details The details received from the Barracks platform.
     * @param path    The path of the downloaded file.
     */
    void onDownloadSuccess(UpdateDetails details, String path);

    /**
     * This method is called when the download fails.
     *
     * @param details   The details received from the Barracks platform.
     * @param throwable The exception caught during the download.
     */
    void onDownloadFailure(UpdateDetails details, Throwable throwable);

    /**
     * This method is called during the download to inform the application about its progress.
     *
     * @param details  The details received from the Barracks platform.
     * @param progress The percentage of progress.
     */
    void onDownloadProgress(UpdateDetails details, int progress);
}
