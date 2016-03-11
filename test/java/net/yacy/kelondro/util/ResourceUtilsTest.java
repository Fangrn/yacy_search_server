// ResourceUtilsTest.java
// -------------------------------------------
// (C) by Michael Peter Christen; mc@yacy.net
// first published on http://www.anomic.de
// Frankfurt, Germany, 2004
//
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package net.yacy.kelondro.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for ResourceUtils class. Jar resources listing is tested on JUnit
 * jar.
 * 
 * @author luc
 *
 */
public class ResourceUtilsTest {

	/**
	 * Test listFileResources from jar
	 */
	@Test
	public final void testListJarFileResources() throws IOException {
		/* From directory name */
		List<URL> resources = ResourceUtils.listFileResources("/org/junit/");
		Assert.assertTrue("Should find resources", resources.size() > 0);
		/* Check resources are valid and can be read */
		for (URL resource : resources) {
			System.out.println("check resource : " + resource);
			InputStream stream = resource.openStream();
			stream.read();
			stream.close();
		}
		
		/* From directory URL */
		resources = ResourceUtils.listFileResources(this.getClass().getResource("/org/junit/"));
		Assert.assertTrue("Should find resources", resources.size() > 0);
		/* Check resources are valid and can be read */
		for (URL resource : resources) {
			System.out.println("check resource : " + resource);
			InputStream stream = resource.openStream();
			stream.read();
			stream.close();
		}

		/* Ending '/' is missing */
		resources = ResourceUtils.listFileResources("/org/junit");
		Assert.assertTrue("Should find resources", resources.size() > 0);
		/* Check resources are valid and can be read */
		for (URL resource : resources) {
			System.out.println("check resource : " + resource);
			InputStream stream = resource.openStream();
			stream.read();
			stream.close();
		}
	}

	/**
	 * Test listFileResources from jar returning empty result
	 */
	@Test
	public final void testListJarFileResourcesFileParam() {
		/* Parameter is an existing resource, but not a directory */
		List<URL> resources = ResourceUtils.listFileResources("/org/junit/Assert.class");
		Assert.assertTrue("Should not find resources", resources.isEmpty());

		/* Parameter is not an existing resource */
		resources = ResourceUtils.listFileResources("/org/junit/dfkjdfjdkf.txt");
		Assert.assertTrue("Should not find resources", resources.isEmpty());

		/* Parameter is null directory name */
		String directory = null;
		resources = ResourceUtils.listFileResources(directory);
		Assert.assertTrue("Should not find resources", resources.isEmpty());
		
		/* Parameter is null URL */
		URL directoryURL = null;
		resources = ResourceUtils.listFileResources(directoryURL);
		Assert.assertTrue("Should not find resources", resources.isEmpty());
	}

	/**
	 * Test listFileResources from classpath directory (relevant only if current
	 * class is not in a jar)
	 */
	@Test
	public final void testListFileResources() throws IOException {
		List<URL> resources = ResourceUtils
				.listFileResources('/' + this.getClass().getPackage().getName().replace('.', '/') + '/');
		Assert.assertTrue("Should find resources", resources.size() > 0);
		/* Check resources are valid and can be read */
		for (URL resource : resources) {
			System.out.println("check resource : " + resource);
			InputStream stream = resource.openStream();
			stream.read();
			stream.close();
		}

		/* Ending '/' is missing */
		resources = ResourceUtils.listFileResources('/' + this.getClass().getPackage().getName().replace('.', '/'));
		Assert.assertTrue("Should find resources", resources.size() > 0);
		/* Check resources are valid and can be read */
		for (URL resource : resources) {
			System.out.println("check resource : " + resource);
			InputStream stream = resource.openStream();
			stream.read();
			stream.close();
		}
	}

	/**
	 * Test listFileResources from classpath directory returning empty result
	 */
	@Test
	public final void testListFileResourcesFileParam() {
		/*
		 * Parameter is an existing classpath directory, but ending "/" has been
		 * forgotten
		 */
		String packageName = '/' + this.getClass().getPackage().getName().replace('.', '/');

		/* Parameter is an existing resource, but not a directory */
		List<URL> resources = ResourceUtils
				.listFileResources('/' + this.getClass().getCanonicalName().replace('.', '/') + ".class");
		Assert.assertTrue("Should not find resources", resources.isEmpty());

		/* Parameter is not an existing resource */
		resources = ResourceUtils.listFileResources(packageName + "/dfkjdfjdkf.txt");
		Assert.assertTrue("Should not find resources", resources.isEmpty());
	}

	/**
	 * Test getFileName
	 */
	@Test
	public final void testGetFileName() {
		/* Nominal file system classpath case */
		URL resource = this.getClass()
				.getResource('/' + this.getClass().getCanonicalName().replace('.', '/') + ".class");
		Assert.assertEquals(this.getClass().getSimpleName() + ".class", ResourceUtils.getFileName(resource));

		/* Nominal jar classpath case */
		resource = this.getClass().getResource('/' + Assert.class.getCanonicalName().replace('.', '/') + ".class");
		Assert.assertEquals(Assert.class.getSimpleName() + ".class", ResourceUtils.getFileName(resource));

		/* Parameter is a directory */
		resource = this.getClass().getResource('/' + this.getClass().getPackage().getName().replace('.', '/') + '/');
		Assert.assertTrue(ResourceUtils.getFileName(resource).isEmpty());

		/* Parameter is null */
		resource = null;
		Assert.assertTrue(ResourceUtils.getFileName(resource).isEmpty());
	}
	
	/**
	 * Test getParentDir
	 */
	@Test
	public final void testGetParentDir() {
		/* Nominal file system classpath case */
		URL resource = this.getClass()
				.getResource('/' + this.getClass().getCanonicalName().replace('.', '/') + ".class");
		String parentDir = ResourceUtils.getParentDir(resource);
		Assert.assertTrue(parentDir.contains(this.getClass().getPackage().getName().replace('.', '/')));
		Assert.assertFalse(parentDir.contains(this.getClass().getSimpleName()));

		/* Nominal jar classpath case */
		resource = this.getClass().getResource('/' + Assert.class.getCanonicalName().replace('.', '/') + ".class");
		parentDir = ResourceUtils.getParentDir(resource);
		Assert.assertTrue(parentDir.contains(Assert.class.getPackage().getName().replace('.', '/')));

		/* Parameter is a directory */
		resource = this.getClass().getResource('/' + this.getClass().getPackage().getName().replace('.', '/') + '/');
		parentDir = ResourceUtils.getParentDir(resource);
		Assert.assertTrue(parentDir.contains("/net/yacy/kelondro/"));

		/* Parameter is null */
		resource = null;
		Assert.assertTrue(ResourceUtils.getParentDir(resource).isEmpty());
	}

}
