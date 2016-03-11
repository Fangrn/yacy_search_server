// Identificator.java
// -----------------------
// (C) by Marc Nause; marc.nause@audioattack.de
// first published on http://www.yacy.net
// Braunschweig, Germany, 2008
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

package net.yacy.document.language;

import java.util.ArrayList;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.cybozu.labs.langdetect.ResourceDetectorFactory;

import net.yacy.cora.util.ConcurrentLog;

/**
 * This class can try to identify the language a text is written in.
 */
public final class Identificator {

    private Detector detector;
    
    public Identificator() {
        try {
            if(DetectorFactory.getLangList().isEmpty()) {
            	/* Load profile from resource directory */
            	ResourceDetectorFactory.loadProfile("/langdetect");
            }
            this.detector = DetectorFactory.create();
        } catch (LangDetectException e) {
            ConcurrentLog.logException(e);
        }
    }

    public void add(final String word) {
        if (word != null && this.detector != null) {
        	this.detector.append(" " + word); // detector internally caches text up to maxtextlen = default = 10000 chars
        }
    }

    /**
     * Get the detected language with highest probability
     * if detection probability is above 0.3 (30%)
     * Underlaying detector differentiates zh-cn and zh-tw, these are returned as zh here.
     * @return 2 char language code (ISO 639-1)
     */
    public Language getLanguage() {
		if (this.detector != null) {
			Language language = null;
			try {
				ArrayList<Language> probabilities = this.detector.getProbabilities();
				if (probabilities.isEmpty())
					return null;
				language = this.detector.getProbabilities().get(0);
			} catch (LangDetectException e) {
				// this contains mostly the message "no features in text"
				// ConcurrentLog.logException(e);
				return null;
			}
			// Return language only if probability is higher than 30% to account
			// for missing language profiles
			if (language.prob > 0.3) {
				return language;
			}
		}

		return null;

    }
    
    /**
     * @param language detected language
     * @return 2 char language code (ISO 639-1) or null when language is not a valid language
     */
    public static String languageCode(Language language) {
    	String lang = null;
    	if(language != null && language.lang != null) {
    		if (language.lang.length() == 2) {
    			lang = language.lang;
    		} else if(language.lang.length() > 2){
    			lang =  language.lang.substring(0, 2);
    		}
    	}
    	return lang;
    	
    }


}
