// TranslateAll.java
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
import java.util.Locale;

import net.yacy.yacy;
import net.yacy.data.Translator;
import net.yacy.search.SwitchboardConstants;

/**
 * Base class for launching a {@link Translator} method without full YaCy
 * application started.
 * 
 * @author luc
 * 
 */
public abstract class TranslatorUtil {

	/**
	 * @param args
	 *            main parameters
	 * @parm argIndex index of source dir parameter
	 * @return translation source dir from parameters or default
	 *         (workingDir/htroot)
	 * @throws IllegalArgumentException
	 *             when no parameters is set and default is not found
	 */
	protected static URL getSourceDirURL(String[] args, int argIndex) {
		URL sourceDir;
		if (args.length > argIndex && argIndex >= 0) {
			try {
				sourceDir = new File(args[argIndex]).getAbsoluteFile().toURI().toURL();
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(
						"Wrong path for sources directory : "
								+ args[argIndex]);
			}
		} else {
			sourceDir = TranslatorUtil.class.getResource(SwitchboardConstants.HTROOT_PATH_DEFAULT_RESOURCE);
			if (sourceDir == null) {
				throw new IllegalArgumentException(
						"No sources directory specified, and default not found : "
								+ sourceDir.getPath());
			}
		}
		return sourceDir;
	}

	/**
	 * @param args
	 *            main parameters
	 * @parm argIndex index of translation file parameter
	 * @return translation file from parameters or default (base on current
	 *         Locale)
	 * @throws IllegalArgumentException
	 *             when no parameters is set and default is not found
	 */
	protected static File getTranslationFile(String[] args, int argIndex) {
		File translationFile;
		if (args.length > argIndex && argIndex >= 0) {
			translationFile = new File(args[argIndex]);
		} else {
			String workingDir = System.getProperty("user.dir");
			if (workingDir == null) {
				throw new IllegalArgumentException(
						"No translation file specified, and default not found");
			}
			translationFile = new File(workingDir, "locales" + File.separator
					+ Locale.getDefault().getLanguage() + ".lng");
			if (!translationFile.exists()) {
				throw new IllegalArgumentException(
						"No translation file specified, and default not found : "
								+ translationFile.getPath());
			}
		}
		return translationFile;
	}

	/**
	 * @param args
	 *            main parameters
	 * @parm argIndex index of translation file parameter
	 * @return extensions list from parameters or default (same as used in
	 *         {@link yacy})
	 */
	protected static String getExtensions(String[] args, int argIndex) {
		String extensions;
		if (args.length > argIndex && argIndex >= 0) {
			extensions = args[argIndex];
		} else {
			extensions = "html,template,inc";
		}
		return extensions;
	}

}
