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

package io.barracks.ota.client;

/**
 * Constants used by the SDK by default
 */
public class Defaults {
    /**
     * Defines the base URL for the Barracks platform
     */
    public static final String DEFAULT_BASE_URL = "https://app.barracks.io/";
    /**
     * Default temporary destination for the package download
     */
    public static final String DEFAULT_TMP_DL_DESTINATION = "update.tmp";
    /**
     * Default final destination for the package download
     */
    public static final String DEFAULT_FINAL_DL_DESTINATION = "update.final";
}
