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
package org.overlord.sramp.integration.teiid;

import org.overlord.sramp.common.i18n.AbstractMessages;

/**
 * Internationalized messages for the Teiid S-RAMP integration module.
 */
public final class Messages extends AbstractMessages {

    /**
     * The shared instance of the localized messages.
     */
    public static final Messages I18N = new Messages();

    /**
     * Don't allow construct outside of this class.
     */
    private Messages() {
        super(Messages.class);
    }

}
