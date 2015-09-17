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
package org.artificer.common.query.xpath;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for the {@link StaticNamespaceContext} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class StaticNamespaceContextTest {

	@Test
	public void testGetNamespaceURI() {
		StaticNamespaceContext ctx = new StaticNamespaceContext();
		ctx.addMapping("ns1", "urn:ns1");
		ctx.addMapping("ns1-1", "urn:ns1");
		ctx.addMapping("ns2", "urn:ns2");
		ctx.addMapping("ns3", "urn:ns3");

		Assert.assertEquals("urn:ns2", ctx.getNamespaceURI("ns2"));
		Assert.assertEquals("urn:ns3", ctx.getNamespaceURI("ns3"));
		Assert.assertNull(ctx.getNamespaceURI("ns4"));
	}

	@Test
	public void testGetPrefix() {
		StaticNamespaceContext ctx = new StaticNamespaceContext();
		ctx.addMapping("ns1", "urn:ns1");
		ctx.addMapping("ns1-1", "urn:ns1");
		ctx.addMapping("ns2", "urn:ns2");
		ctx.addMapping("ns3", "urn:ns3");

		Assert.assertEquals("ns2", ctx.getPrefix("urn:ns2"));
		Assert.assertEquals("ns3", ctx.getPrefix("urn:ns3"));
		Assert.assertNull(ctx.getPrefix("urn:ns4"));
	}

	@Test
	public void testGetPrefixes() {
		StaticNamespaceContext ctx = new StaticNamespaceContext();
		ctx.addMapping("ns1", "urn:ns1");
		ctx.addMapping("ns1-1", "urn:ns1");
		ctx.addMapping("ns2", "urn:ns2");
		ctx.addMapping("ns3", "urn:ns3");

		Assert.assertTrue(ctx.getPrefixes("urn:ns2").hasNext());
		Assert.assertTrue(ctx.getPrefixes("urn:ns3").hasNext());
		Assert.assertFalse(ctx.getPrefixes("urn:ns4").hasNext());

		Iterator<String> iter1 = ctx.getPrefixes("urn:ns1");
		Assert.assertTrue(iter1.hasNext());
		Assert.assertTrue(iter1.next().startsWith("ns1"));
		Assert.assertTrue(iter1.hasNext());
		Assert.assertTrue(iter1.next().startsWith("ns1"));
	}

	@Test
	public void testRemoveMapping() {
		StaticNamespaceContext ctx = new StaticNamespaceContext();
		ctx.addMapping("ns1", "urn:ns1");
		ctx.addMapping("ns1-1", "urn:ns1");
		ctx.addMapping("ns2", "urn:ns2");
		ctx.addMapping("ns3", "urn:ns3");

		Assert.assertEquals("urn:ns2", ctx.getNamespaceURI("ns2"));
		Assert.assertEquals("urn:ns3", ctx.getNamespaceURI("ns3"));
		Assert.assertNull(ctx.getNamespaceURI("ns4"));

		ctx.removeMapping("ns2");
		Assert.assertNull(ctx.getNamespaceURI("ns2"));
		Assert.assertEquals("urn:ns3", ctx.getNamespaceURI("ns3"));
		Assert.assertNull(ctx.getNamespaceURI("ns4"));
	}

}
