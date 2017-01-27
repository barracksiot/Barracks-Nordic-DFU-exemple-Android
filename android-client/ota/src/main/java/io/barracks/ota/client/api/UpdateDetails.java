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

package io.barracks.ota.client.api;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class encapsulates the details about an update.<br>
 * It is a {@link Parcelable} which allows for it to be sent back and forth to the different
 * services with the use of an {@link android.content.Intent Intent}
 */
public class UpdateDetails implements Parcelable {
    /**
     * @see Parcelable
     */
    public static final Creator<UpdateDetails> CREATOR = new Creator<UpdateDetails>() {
        @Override
        public UpdateDetails createFromParcel(Parcel in) {
            return new UpdateDetails(in);
        }

        @Override
        public UpdateDetails[] newArray(int size) {
            return new UpdateDetails[size];
        }
    };

    /**
     * The version ID of the update.
     */
    private String versionId;
    /**
     * The {@link PackageInfo} describing the package to be downloaded.
     */
    private PackageInfo packageInfo;
    /**
     * A set of user-defined customUpdateData.
     */
    private Bundle customUpdateData = new Bundle();

    private UpdateDetails() {

    }

    /**
     * Parcelable constructor
     *
     * @param in The parcel to read from.
     * @see Parcelable
     */
    protected UpdateDetails(Parcel in) {
        versionId = in.readString();
        packageInfo = in.readParcelable(getClass().getClassLoader());
        customUpdateData = in.readBundle(getClass().getClassLoader());
    }

    /**
     * Get the version ID of the update.
     *
     * @return the version ID of the update.
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * Get the {@link PackageInfo} describing the package to be downloaded.
     *
     * @return the  {@link PackageInfo} describing the package to be downloaded.
     */
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    /**
     * Get the set of user-defined customUpdateData.
     *
     * @return the set of user-defined customUpdateData.
     */
    public Bundle getCustomUpdateData() {
        return customUpdateData;
    }

    /**
     * @see Parcelable
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @see Parcelable
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(versionId);
        dest.writeParcelable(packageInfo, 0);
        dest.writeBundle(customUpdateData);
    }

}
