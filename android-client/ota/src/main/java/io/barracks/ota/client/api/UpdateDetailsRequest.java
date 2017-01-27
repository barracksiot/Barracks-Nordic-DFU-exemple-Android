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
import android.text.TextUtils;

/**
 * This class encapsulates parameters to request {@link UpdateDetails} to the Barracks service.<br>
 * It comes with a convenient {@link Builder Builder} which creates and verifies the validity of the request<br>
 * It is a {@link Parcelable} which allows for it to be sent back and forth to the different
 * services with the use of an {@link android.content.Intent Intent}.
 */
public class UpdateDetailsRequest implements Parcelable {
    /**
     * @see Parcelable
     */
    public static final Creator<UpdateDetailsRequest> CREATOR = new Creator<UpdateDetailsRequest>() {
        @Override
        public UpdateDetailsRequest createFromParcel(Parcel in) {
            return new UpdateDetailsRequest(in);
        }

        @Override
        public UpdateDetailsRequest[] newArray(int size) {
            return new UpdateDetailsRequest[size];
        }
    };

    /**
     * The unique identifier for the unit which is requesting the details.
     */
    private final String unitId;
    /**
     * The current version of the package used by the unit.
     */
    private final String versionId;
    /**
     * A {@link Bundle} of user-defined customClientData.
     */
    private final Bundle customClientData;

    /**
     * Parcelable constructor
     *
     * @param in The parcel to read from.
     * @see Parcelable
     */
    protected UpdateDetailsRequest(Parcel in) {
        unitId = in.readString();
        versionId = in.readString();
        customClientData = in.readBundle(getClass().getClassLoader());
    }

    /**
     * @see Builder
     */
    private UpdateDetailsRequest(String unitId, String versionId, Bundle customClientData) {
        this.unitId = unitId;
        this.versionId = versionId;
        this.customClientData = customClientData;
    }

    /**
     * Get the unique identifier for the unit which is requesting the details.
     *
     * @return the unique identifier for the unit which is requesting the details.
     */
    public String getUnitId() {
        return unitId;
    }

    /**
     * Get the current version of the package used by the unit.
     *
     * @return the current version of the package used by the unit.
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * Get the {@link Bundle} of user-defined customClientData.
     *
     * @return the {@link Bundle} of user-defined customClientData.
     */
    public Bundle getCustomClientData() {
        return customClientData;
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
        dest.writeString(unitId);
        dest.writeString(versionId);
        dest.writeBundle(customClientData);
    }

    /**
     * A convenient builder to create an {@link UpdateDetailsRequest}.<br>
     * This class provides a verification mechanism when creating
     */
    public static final class Builder {
        private String unitId = null;
        private String versionId = null;
        private Bundle customClientData = null;

        /**
         * Builder constructor.
         */
        public Builder() {

        }

        /**
         * Define the unique identifier for the unit which is requesting the details.
         *
         * @param unitId the unique identifier for the unit which is requesting the details.
         * @return the same builder instance.
         */
        public Builder unitId(String unitId) {
            this.unitId = unitId;
            return this;
        }

        /**
         * Define the current version of the package used by the unit.
         *
         * @param versionId the current version of the package used by the unit.
         * @return the same builder instance.
         */
        public Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        /**
         * Define the {@link Bundle} of user-defined customClientData.
         *
         * @param customClientData the {@link Bundle} of user-defined customClientData.
         * @return the same builder instance.
         */
        public Builder customClientData(Bundle customClientData) {
            this.customClientData = customClientData;
            return this;
        }

        /**
         * Build the {@link UpdateDetailsRequest} of this builder.
         *
         * @return an {@link UpdateDetailsRequest}
         * @throws IllegalStateException when either a unit identifier or a version identifier is missing.
         */
        public UpdateDetailsRequest build() throws IllegalStateException {
            if (TextUtils.isEmpty(unitId)) {
                throw new IllegalStateException("Unit ID is required");
            }
            if (TextUtils.isEmpty(versionId)) {
                throw new IllegalStateException("Version ID is required");
            }
            return new UpdateDetailsRequest(unitId, versionId, customClientData);
        }
    }
}
