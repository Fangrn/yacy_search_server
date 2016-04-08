// ListNonTranslatedFiles.java
// -------------------------------------
// part of YACY
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

package net.yacy.utils.translation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.yacy.cora.util.ConcurrentLog;
import net.yacy.data.ListManager;
import net.yacy.data.Translator;
import net.yacy.kelondro.util.ResourceUtils;

/**
 * Util to help identifying non translated files.
 * 
 * @author luc
 * 
 */
public class ListNonTranslatedFiles extends TranslatorUtil {
	
	/**
	 * Print on standard output result of search
	 * @param nonTranslatedFiles list of non translated files
	 */
	private static void printResults(List<URL> nonTranslatedFiles) {
		System.out.println(nonTranslatedFiles.size() + " files are not translated.");
		for(URL file : nonTranslatedFiles) {
			System.out.println(file);
		}
	}

	/**
	 * List all files from srcDir directory which are not translated using
	 * specified locale file with specified extensions. If no argument is set,
	 * default values are used.
	 * 
	 * @param args
	 *            runtime arguments<br/>
	 *            <ul>
	 *            <li>args[0] : source dir path</li>
	 *            <li>args[1] : translation file path</li>
	 *            <li>args[2] : extensions (separated by commas)</li>
	 *            </ul>
	 * @throws MalformedURLException when source dir path is not valid
	 */
	public static void main(String args[]) throws MalformedURLException {
		URL sourceDir = getSourceDirURL(args, 0);

		File translationFile = getTranslationFile(args, 1);

		List<String> extensions = ListManager
				.string2vector(getExtensions(args, 2));
		

		String excludedDir = "locale";

		ConcurrentLog.info("ListNonTranslatedFiles", "Listing non translated "
				+ extensions + " files from " + sourceDir + " using "
				+ translationFile);

		try {
			Set<String> translatedRelativePaths = Translator.loadTranslationsLists(translationFile.toURI().toURL()).keySet();

			List<URL> nonTranslatedFiles = new ArrayList<>();
			
	    	final List<URL> dirList = ResourceUtils.listRecursiveDirectories(sourceDir, excludedDir);
	        dirList.add(sourceDir);
	        int sourceDirURLLength = sourceDir.toExternalForm().length();
			for (final URL childSourceDir : dirList) {
				List<URL> srcFileURLs = ResourceUtils.listFileResources(childSourceDir);
				for(URL srcFileURL : srcFileURLs) {
					String relativePath = srcFileURL.toExternalForm().substring(sourceDirURLLength);					
					for(String ext: extensions) {
						if(relativePath.endsWith(ext)) {
		                    if (!translatedRelativePaths.contains(relativePath)) {
		                        nonTranslatedFiles.add(srcFileURL);
		                    }
		                    break;
						}
					}
				}
			}

			
			printResults(nonTranslatedFiles);


		} finally {
			ConcurrentLog.shutdown();
		}

	}

}
