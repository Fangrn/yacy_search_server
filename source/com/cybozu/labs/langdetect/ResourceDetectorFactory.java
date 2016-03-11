/**
 *  ResourceDetectorFactory
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cybozu.labs.langdetect;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import com.cybozu.labs.langdetect.util.LangProfile;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;
import net.yacy.kelondro.util.ResourceUtils;

/**
 * Enable load of langdetect profile from classpath resources instead of files.
 * 
 * @author luc
 *
 */
public abstract class ResourceDetectorFactory {

	/**
	 * Load profiles from specified classpath directory. This method must be
	 * called once before language detection.
	 * 
	 * @param profileResourceDirectory
	 *            profile classpath resource directory path
	 * @throws LangDetectException
	 *             Can't open profiles(error code =
	 *             {@link ErrorCode#FileLoadError}) or profile's format is wrong
	 *             (error code = {@link ErrorCode#FormatError})
	 */
	public static void loadProfile(String profileResourceDirectory) throws LangDetectException {
		List<URL> resources = ResourceUtils.listFileResources(profileResourceDirectory);
		if (resources.isEmpty())
			throw new LangDetectException(ErrorCode.NeedLoadProfileError,
					"Not found profile: " + profileResourceDirectory);

		int langsize = resources.size(), index = 0;
		for (URL resource : resources) {
			InputStream resourceStream = null;
			try {
				resourceStream = resource.openStream();
				LangProfile profile = JSON.decode(resourceStream, LangProfile.class);
				DetectorFactory.addProfile(profile, index, langsize);
				++index;
			} catch (JSONException e) {
				throw new LangDetectException(ErrorCode.FormatError,
						"profile format error in '" + resource.getFile() + "'");
			} catch (IOException e) {
				throw new LangDetectException(ErrorCode.FileLoadError, "can't open '" + resource.getFile() + "'");
			} finally {
				try {
					if (resourceStream != null)
						resourceStream.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

}
