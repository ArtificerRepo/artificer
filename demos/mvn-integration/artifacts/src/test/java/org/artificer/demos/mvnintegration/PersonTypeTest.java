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
package org.artificer.demos.mvnintegration;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for generated class {@link PersonTypeTest}.
 *
 * @author eric.wittmann@redhat.com
 */
public class PersonTypeTest {

	@Test
	public void test() {
		ObjectFactory factory = new ObjectFactory();
		PersonType person = factory.createPersonType();
		Assert.assertNotNull(person);
		person.setFirstName("Max");
		person.setLastName("Eisenhardt");

		String name = person.getFirstName() + " " + person.getLastName();
		Assert.assertEquals("Max Eisenhardt", name);
	}

}
