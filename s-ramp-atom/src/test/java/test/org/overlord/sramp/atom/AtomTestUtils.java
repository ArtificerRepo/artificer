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
package test.org.overlord.sramp.atom;

import java.io.File;
import java.lang.reflect.Field;

import org.overlord.sramp.atom.archive.SrampArchive;

/**
 * Some utility methods helpful when testing.
 *
 * @author eric.wittmann@redhat.com
 */
public class AtomTestUtils {

	/**
	 * Gets the working directory from the archive.  The working directory
	 * is a private field, but we can get it anyway (during a unit test).
	 * @param archive the sramp archive
	 * @return the archive's private working directory
	 * @throws Exception
	 */
	public static File getArchiveWorkDir(SrampArchive archive) throws Exception {
		Field field = archive.getClass().getDeclaredField("workDir"); //$NON-NLS-1$
		boolean oldAccessible = field.isAccessible();
		field.setAccessible(true);
		File workDir = (File) field.get(archive);
		field.setAccessible(oldAccessible);
		return workDir;
	}

}
