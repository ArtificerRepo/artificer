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
package org.overlord.sramp.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * An implementation of the {@link ShellCommandReader} that reads data from
 * a file.
 *
 * @author eric.wittmann@redhat.com
 */
public class FileShellCommandReader extends AbstractShellCommandReader {

	private String filePath;
	private BufferedReader fileReader;

	/**
	 * Constructor.
	 * @param factory
	 * @param context
	 * @param filePath
	 */
	public FileShellCommandReader(ShellCommandFactory factory, ShellContextImpl context, String filePath) {
		super(factory, context);
		this.filePath = filePath;
	}

	/**
	 * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#open()
	 */
	@Override
	public void open() throws IOException {
		File file = new File(filePath);
		if (!file.isFile()) {
			throw new FileNotFoundException(filePath);
		}
		this.fileReader = new BufferedReader(new InputStreamReader(FileUtils.openInputStream(file)));
	}

	/**
	 * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#readLine()
	 */
	@Override
	protected String readLine() throws IOException {
	    String line = null;
	    do {
            line = fileReader.readLine();
            if (line == null)
                break;
	    } while (line.startsWith("#") || line.trim().length() == 0); //$NON-NLS-1$
	    return line;
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommandReader#close()
	 */
	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(fileReader);
	}

}
