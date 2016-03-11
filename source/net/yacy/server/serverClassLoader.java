// serverClassLoader.java
// -----------------------
// (C) by Michael Peter Christen; mc@yacy.net
// first published on http://www.anomic.de
// Frankfurt, Germany, 2004
// last major change: 11.07.2004
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

package net.yacy.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;

import net.yacy.cora.util.ConcurrentLog;
import net.yacy.kelondro.util.FileUtils;

/**
 * Class loader for servlet classes
 * (findClass looking in default htroot directory)
 */
public final class serverClassLoader extends ClassLoader {
	
//	/** htroot directory resource */
//	private Resource htroot;

	/**
	 * Create a loader instance
	 * @param htRoot htroot directory (classpath or regular resource)
	 */
    public serverClassLoader(Resource htRootResource) {
    	super(Thread.currentThread().getContextClassLoader());
//        if(htRootResource == null) {
//        	throw new IllegalArgumentException("htrootResource must not be null");
//        }
//    	this.htroot = htRootResource;
        if (!registerAsParallelCapable()) { // avoid blocking
            ConcurrentLog.warn("serverClassLoader", "registerAsParallelCapable failed");
        }
    }
    
//	/**
//	 * Create a loader instance
//	 * @param htRoot htroot directory (classpath or regular resource)
//	 */
//    public serverClassLoader(final ClassLoader parent) {
//        super(parent);
//        if (!registerAsParallelCapable()) {
//            ConcurrentLog.warn("serverClassLoader", "registerAsParallelCapable failed");
//        }
//    }

//    /**
//     * Find servlet class in htroot directory
//     * but use the internal loadClass(Resource) method to load the class same way
//     * (e.g. caching) as direct call to loadClass(File)
//     * This method is mainly to avoid classpath conflicts for servlet to servlet calls
//     * making inclusion of htroot in system classpath not crucial
//     * 
//     *
//     * @param binary class name (e.g. java.lang.String is a binary class name)
//     * @return loaded class
//     * @throws ClassNotFoundException
//     */
//    @Override
//    protected Class<?> findClass(String classname) throws ClassNotFoundException {
//    	/* First try to load already loaded class */
//    	Class<?> c = findLoadedClass(classname);
//    	if (c != null) {
//    		return c;
//    	}
//    	if(this.htroot == null) {
//    		throw new ClassNotFoundException(classname);
//    	}
//        // construct path to htroot for a servletname
//        Resource classResource;
//		try {
//			classResource = this.htroot.addPath(classname.replace('.', '/') + ".class");
//		} catch (MalformedURLException e) {
//			throw new ClassNotFoundException("linkageError, " + e.getMessage() + ":" + classname);
//		} catch (IOException e) {
//			throw new ClassNotFoundException(e.getMessage() + ":" + classname);
//		}
//		if(classResource == null) {
//			throw new ClassNotFoundException(classname + " not found in htroot base resource.");
//		}
//        return loadClassResource(classResource);
//    }

	/**
	 * A special loadClass using file as argument to find and load a class. This
	 * method is directly called by the application and not part of the normal
	 * loadClass chain (= never called by JVM)
	 * 
	 * @param classfile
	 *            class file resource. Must not be null.
	 * @return loaded an resolved class
	 * @throws ClassNotFoundException when an error occured or when classfile is not a valid class resource
	 */
    public Class<?> loadClassResource(final Resource classfile) throws ClassNotFoundException {

    	if(classfile == null) {
    		throw new ClassNotFoundException("null class resource");
    	}
        Class<?> c;
        String filePath = classfile.getName();
        final int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex < 0) {
        	throw new ClassNotFoundException("wrong class name: " + filePath);
        }
        int startIndex = filePath.lastIndexOf(URIUtil.SLASH);
        if(startIndex < 0) {
        	startIndex = 0;
        } else {
        	startIndex++;
        }
        final String classname = filePath.substring(startIndex, dotIndex);

//        /* First try to find already loaded class */
//    	c = findLoadedClass(classname);
//    	if (c != null) {
//    		return c;
//    	}
        // load the file from the file system
        byte[] b;
        try {
        	InputStream classStream = classfile.getInputStream();
            b = FileUtils.read(classStream);
            // make a class out of the stream
            c = this.defineClass(classname, b, 0, b.length);
            resolveClass(c);
        } catch (final LinkageError ee) {
        	/* This error may occur when two threads try to define concurrently the same class. Here findLoadedClass should yet return something. */
        	c = findLoadedClass(classname);
        	if (c != null) {
        		return c;
        	}
            throw new ClassNotFoundException("linkageError, " + ee.getMessage() + ":" + classfile.toString());
        } catch (final IOException ee) {
            throw new ClassNotFoundException(ee.getMessage() + ":" + classfile.toString());
        }
        return c;
    }

}