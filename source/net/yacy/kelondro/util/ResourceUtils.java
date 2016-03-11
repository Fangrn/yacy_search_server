// ResourceUtils.java
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

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.yacy.cora.util.ConcurrentLog;

/**
 * Utils for classpath resources
 * 
 * @author luc
 *
 */
public class ResourceUtils {

	/**
	 * List resources under a directory in classpath. Only return direct file
	 * resources (no directories and no files under subdirectories).
	 * 
	 * @param resourceFolder
	 *            directory name. When empty string, all resources with no
	 *            parent will be listed.
	 * @return a list of resources with directory as direct parent
	 */
	public static List<URL> listFileResources(String directory) {
		List<URL> resources = new ArrayList<>();
		if (directory != null) {
			/* Ensure directory name ends with "/" */
			if (!directory.endsWith("/")) {
				directory += "/";
			}
			URL directoryURL = ResourceUtils.class.getResource(directory);
			if (directoryURL != null) {
				resources = listFileResources(directoryURL);
			}
		}
		return resources;
	}

	/**
	 * List resources under a directory url. Only return file resources (no
	 * directories and no files under subdirectories).
	 * 
	 * @param directoryURL
	 *            url to a file directory or a directory entry in a jar
	 * @return a list of resources with directory as direct parent
	 */
	public static List<URL> listFileResources(URL directoryURL) {
		List<URL> resources = new ArrayList<>();
		if (directoryURL != null) {
			try {
				URLConnection urlConn = directoryURL.openConnection();
				if (directoryURL.getProtocol().equals("file")) {
					/* filesystem classpath resource */
					File directory = null;
					try {
						directory = new File(directoryURL.toURI());
					} catch (URISyntaxException e) {
						ConcurrentLog.fine("ResourceUtils",
								"directoryURL syntax error : " + directoryURL.toExternalForm());
					}
					if (directory != null) {
						File[] files = directory.listFiles();
						/* files is null when url is not a directory */
						if (files != null) {
							for (File file : files) {
								if (file.isFile()) {
									resources.add(file.toURI().toURL());
								}
							}
						}
					}
				} else if (urlConn instanceof JarURLConnection) {
					/* jar classpath resource */
					JarURLConnection jarConn = (JarURLConnection) urlConn;
					JarFile jarFile = jarConn.getJarFile();
					Enumeration<JarEntry> items = jarFile.entries();
					String urlString = directoryURL.toExternalForm();
					int jarSeparatorChar = urlString.indexOf("!");
					if (jarSeparatorChar >= 0 && urlString.length() > jarSeparatorChar + 2) {
						String prefix = urlString.substring(jarSeparatorChar + 2);
						while (items.hasMoreElements()) {
							JarEntry item = items.nextElement();
							if (item.getName().startsWith(prefix)) {
								String resourceName = item.getName().substring(prefix.length());
								/* Do not add folder or sub-folders entries */
								if (!resourceName.contains("/") && !resourceName.isEmpty()) {
									resources.add(new URL(urlString + resourceName));
								}
							}
						}
					}
				}
			} catch (IOException e) {
				ConcurrentLog.severe("ResourceUtils",
						"Error when listing directory resource files : " + e.getMessage());
			} finally {
			}
		}
		return resources;
	}

	/**
	 * List recursively directories under a directory url.
	 * 
	 * @param directoryURL
	 *            url to a filesystem directory or a directory entry in a jar
	 * @param excludeDir
	 *            directory name to exclude
	 * @return a list of children directories
	 */
	public static List<URL> listRecursiveDirectories(URL directoryURL, String excludeDir) {
		List<URL> resources = new ArrayList<>();
		if (directoryURL != null) {
			try {
				URLConnection urlConn = directoryURL.openConnection();
				if (directoryURL.getProtocol().equals("file")) {
					/* filesystem classpath resource */
					File directory = null;
					try {
						directory = new File(directoryURL.toURI());
					} catch (URISyntaxException e) {
						ConcurrentLog.fine("ResourceUtils",
								"directoryURL syntax error : " + directoryURL.toExternalForm());
					}
					if (directory != null) {
						List<File> directories = FileUtils.getDirsRecursive(directory, excludeDir);
						for (File childDir : directories) {
							resources.add(childDir.toURI().toURL());
						}
					}
				} else if (urlConn instanceof JarURLConnection) {
					/* jar classpath resource */
					JarURLConnection jarConn = (JarURLConnection) urlConn;
					JarFile jarFile = jarConn.getJarFile();
					Enumeration<JarEntry> items = jarFile.entries();
					String urlString = directoryURL.toExternalForm();
					int jarSeparatorChar = urlString.indexOf("!");
					if (jarSeparatorChar >= 0 && urlString.length() > jarSeparatorChar + 2) {
						String prefix = urlString.substring(jarSeparatorChar + 2);
						while (items.hasMoreElements()) {
							JarEntry item = items.nextElement();
							if (item.getName().startsWith(prefix)) {
								if(item.isDirectory()) {
									String resourceName = item.getName().substring(prefix.length());
									if(!resourceName.equals(excludeDir)) {
										resources.add(new URL(urlString + resourceName));
									}
								}
							}
						}
					}
				}
			} catch (IOException e) {
				ConcurrentLog.severe("ResourceUtils",
						"Error when listing directory resource files : " + e.getMessage());
			} finally {
			}
		}
		return resources;
	}

	/**
	 * @param classpath
	 *            resource name
	 * @return true when resource exists and is a directory
	 */
	public static boolean isResourceDir(String resource) {
		boolean isDirectory = false;
		if (resource != null) {
			/* Ensure name ends with "/" */
			if (!resource.endsWith("/")) {
				resource += "/";
			}
			URL url = ResourceUtils.class.getResource(resource);
			if (url != null) {
				try {
					URLConnection urlConn = url.openConnection();
					if (url.getProtocol().equals("file")) {
						/* filesystem classpath resource */
						try {
							isDirectory = new File(url.toURI()).isDirectory();
						} catch (URISyntaxException ignored) {
						}
					} else if (urlConn instanceof JarURLConnection) {
						/* jar classpath resource */
						JarURLConnection jarConn = (JarURLConnection) urlConn;
						JarEntry entry = jarConn.getJarEntry();

						isDirectory = entry != null && entry.isDirectory();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
				}
			}
		}
		return isDirectory;
	}

	/**
	 * Extract file name part of an url.
	 * 
	 * @param url
	 * @return file name, eventually empty.
	 */
	public static String getFileName(URL url) {
		String fileName = "";
		if (url != null) {
			String file = url.getFile();
			int index = file.lastIndexOf("/");
			if (index >= 0 && (index + 1) < file.length()) {
				fileName = file.substring(index + 1);
			}
		}
		return fileName;
	}

	/**
	 * Extract parent directory url part of an url.
	 * 
	 * @param url
	 * @return parent directory path, eventually empty.
	 */
	public static String getParentDir(URL url) {
		String parentPath = "";
		if (url != null) {
			String file = url.toExternalForm();
			int index = file.lastIndexOf("/");
			if (index >= 0) {
				parentPath = file.substring(0, index + 1);
			}
		}
		return parentPath;
	}

}
