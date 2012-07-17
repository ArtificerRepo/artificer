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
package org.overlord.sramp.wagon;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Implements a wagon provider that uses the S-RAMP Atom API.
 * 
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("unchecked")
@Component(role=Wagon.class, hint="sramp", instantiationStrategy="per-lookup")
public class SrampWagon extends StreamWagon {

	/**
	 * Constructor.
	 */
	public SrampWagon() {
	}

	/**
	 * @see org.apache.maven.wagon.AbstractWagon#openConnectionInternal()
	 */
	@Override
	protected void openConnectionInternal() throws ConnectionException, AuthenticationException {
		// The S-RAMP Atom API is session-less, so no connections to open
		System.out.println("openConnectionInternal");
	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#closeConnection()
	 */
	@Override
	public void closeConnection() throws ConnectionException {
		// The S-RAMP Atom API is session-less, so no connections to close
		System.out.println("closeConnection");
	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#fillInputData(org.apache.maven.wagon.InputData)
	 */
	@Override
	public void fillInputData(InputData arg0) throws TransferFailedException, ResourceDoesNotExistException,
			AuthorizationException {
		// TODO Auto-generated method stub
		System.out.println("fillInputData");
	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#fillOutputData(org.apache.maven.wagon.OutputData)
	 */
	@Override
	public void fillOutputData(OutputData arg0) throws TransferFailedException {
		// TODO Auto-generated method stub
		System.out.println("fillOutputData");
	}

}
