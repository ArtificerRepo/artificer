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
package org.artificer.common.audit;

/**
 * Some basic built-in audit entry types.
 * @author eric.wittmann@redhat.com
 */
public final class AuditEntryTypes {

    public static final String ARTIFACT_ADD = "artifact:add"; //$NON-NLS-1$
    public static final String ARTIFACT_UPDATE = "artifact:update"; //$NON-NLS-1$
    public static final String ARTIFACT_DELETE = "artifact:delete"; //$NON-NLS-1$

}
