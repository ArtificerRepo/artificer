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
package org.overlord.sramp.repository.jcr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Extends the basic {@link FileInputStream}.  This extension deletes the 
 * file in the close method.
 *
 * @author eric.wittmann@redhat.com
 */
public class DeleteOnCloseFileInputStream extends FileInputStream {

	private File file;
	
	/**
	 * Constructor.
	 * @param file
	 * @throws FileNotFoundException
	 */
	public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
		super(file);
		this.file = file;
	}
	
	/**
	 * @see java.io.FileInputStream#close()
	 */
	@Override
	public void close() throws IOException {
		super.close();
		this.file.delete();
	}

}
