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

import io.barracks.ota.client.Defaults;

/**
 * Convenient access to the various helpers.
 */
public class BarracksHelper {
    /**
     * The API key provided by the Barracks platform.
     */
    private final String apiKey;
    /**
     * The url used to call the Barracks platform.
     */
    private final String baseUrl;

    /**
     * Default constructor for the helper.<br>
     * The url is pointing to {@link Defaults#DEFAULT_BASE_URL}
     *
     * @param apiKey The API key provided by the Barracks platform.
     */
    public BarracksHelper(String apiKey) {
        this(apiKey, Defaults.DEFAULT_BASE_URL);
    }

    /**
     * Advanced constructor for the helper.
     *
     * @param apiKey  The API key provided by the Barracks platform.
     * @param baseUrl The url used to call the Barracks platform.
     */
    public BarracksHelper(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * Access to a helper for making update requests to the Barracks platform.
     *
     * @return A properly configured {@link UpdateCheckHelper}
     */
    public UpdateCheckHelper getUpdateCheckHelper() {
        return new UpdateCheckHelper(apiKey, baseUrl);
    }

    /**
     * Access to a helper for downloading packages.
     *
     * @return A properly configured {@link PackageDownloadHelper}
     */
    public PackageDownloadHelper getPackageDownloadHelper() {
        return new PackageDownloadHelper(apiKey);
    }
}
