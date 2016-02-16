/** 
 * UtilTest.java
 * 
 * Copyright 2016 President and Fellows of Harvard College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kurator.akka.data;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author mole
 *
 */
public class UtilTest {

	/**
	 * Test method for {@link org.kurator.akka.data.Util#isBlank(java.lang.String)}.
	 */
	@Test
	public void testIsBlank() {
		assertTrue(Util.isBlank(""));
		try { 
		   assertTrue(Util.isBlank(""));
		} catch (Exception e) { 
			fail("Threw unexpected exception " + e.getMessage());
		}
		try { 
			Util.isBlank(null);
			fail("Failed to throw expected exception");
		} catch (Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		assertTrue(Util.isBlank(""));
		assertTrue(Util.isBlank(" "));
		assertTrue(Util.isBlank(" " + '\n' + " "));
		assertTrue(Util.isBlank(" " + '\t' + " "));
		
		assertFalse(Util.isBlank("9"));
		assertFalse(Util.isBlank(" 9 "));
		assertFalse(Util.isBlank(" A "));
		assertFalse(Util.isBlank("A"));
		assertFalse(Util.isBlank(" ² "));
		assertFalse(Util.isBlank("♀"));
		assertFalse(Util.isBlank("♂"));
		
		// Trying funny things with control characters
		assertFalse(Util.isBlank("A"));
	}

	/**
	 * Test method for {@link org.kurator.akka.data.Util#hasContent(java.lang.String)}.
	 */
	@Test
	public void testHasContent() {
		assertFalse(Util.hasContent(""));
		assertFalse(Util.hasContent(null));
		assertFalse(Util.hasContent(" "));
		
		assertTrue(Util.hasContent("a"));
		assertTrue(Util.hasContent(" a "));
	}

}
