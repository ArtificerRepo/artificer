/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.common.visitors;

import java.lang.reflect.Method;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.i18n.Messages;

/**
 * Helper class used to visit S-RAMP artifacts.  This should be replaced by "accept" methods implemented
 * on each of the generated S-RAMP artifact classes.  I think there might be a jax-b plugin that will do
 * that, but I haven't checked into yet.  Ex: accept(ArtifactVisitor visitor) { visitor.visit(this); }
 *
 * @author eric.wittmann@redhat.com
 */
public final class ArtifactVisitorHelper {

	/**
	 * Called to help the given visitor visit the provided artifact.
	 * @param visitor
	 * @param artifact
	 */
	public static void visitArtifact(ArtifactVisitor visitor, BaseArtifactType artifact) {
		try {
			Method method = visitor.getClass().getMethod("visit", artifact.getClass()); //$NON-NLS-1$
			method.invoke(visitor, artifact);
		} catch (Exception e) {
			// This shouldn't happen unless we've programmed something wrong in the visitor interface.
			throw new RuntimeException(
			        Messages.i18n.format("VISIT_METHOD_NOT_FOUND", visitor.getClass(), artifact.getClass()), //$NON-NLS-1$
					e);
		}
	}

}
