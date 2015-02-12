/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.artificer.ui.client.shared.beans;

/**
 * The origin of the artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public enum ArtifactOriginEnum {

    any, primary, derived;

    /**
     * Creates an origin enum given three booleans (any, primary, derived).  The first of these
     * that is true will win out.  This method is useful in the UI, where the value is displayed
     * as three radio buttons.
     * @param any
     * @param primary
     * @param derived
     */
    public static ArtifactOriginEnum valueOf(Boolean any, Boolean primary, Boolean derived) {
        if (any)
            return ArtifactOriginEnum.any;
        if (primary)
            return ArtifactOriginEnum.primary;
        if (derived)
            return ArtifactOriginEnum.derived;
        // Return null here - unspecified behavior.
        return null;
    }

}
