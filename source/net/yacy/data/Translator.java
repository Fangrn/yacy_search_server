// translator.java
// -------------------------------------
// part of YACY
// (C) by Michael Peter Christen; mc@yacy.net
// first published on http://www.anomic.de
// Frankfurt, Germany, 2004
//
// This file ist contributed by Alexander Schier
// last major change: 25.05.2005
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

package net.yacy.data;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.yacy.cora.util.CommonPattern;
import net.yacy.cora.util.ConcurrentLog;
import net.yacy.kelondro.util.FileUtils;
import net.yacy.kelondro.util.Formatter;
import net.yacy.kelondro.util.ResourceUtils;
import net.yacy.search.SwitchboardConstants;
import net.yacy.server.serverSwitch;

/**
 * Wordlist based translator
 *
 * Uses a Property like file with phrases or single words to translate a string or a file
 * */
public class Translator {

    public final static String LANG_FILENAME_FILTER = "^.*\\.lng$";

    /**
     * Translate source using entries in translationTable
     * @param source text to translate. Mus be non null.
     * @param translationTable translation entries : text to translate -> translation
     * @return source translated
     */
	public static String translate(final String source,
			final Map<String, String> translationTable) {
		final Set<Map.Entry<String, String>> entries = translationTable.entrySet();
		StringBuilder builder = new StringBuilder(source);
		for (final Entry<String, String> entry: entries) {
			String key = entry.getKey();
			/* We have to check key is not empty or indexOf would always return a positive value */
			if (key != null && !key.isEmpty()) {
				String translation = entry.getValue();
				int index = builder.indexOf(key);
				if (index < 0) {
					// Filename not available, but it will be printed in Log
					// after all untranslated Strings as "Translated file: "
					if (ConcurrentLog.isFine("TRANSLATOR"))
						ConcurrentLog.fine("TRANSLATOR", "Unused String: "
								+ key);
				} else {
					while (index >= 0) {
						builder.replace(index, index + key.length(),
								translation);
						index = builder.indexOf(key,
								index + translation.length());
					}
				}
			}
		}
		return builder.toString();
	}

    /**
     * Load multiple translationLists from one File. Each List starts with #File: relative/path/to/file
     * (within each file section translation is done in order of the language file entries, on conflicts
     * put the shorter key to the end of the list)
     * @param translationFile the File, which contains the Lists
     * @return a HashMap, which contains for each File a HashMap with translations.
     */
    public static Map<String, Map<String, String>> loadTranslationsLists(final URL translationFile) {
        final Map<String, Map<String, String>> lists = new HashMap<String, Map<String, String>>(); //list of translationLists for different files.
        Map<String, String> translationList = new LinkedHashMap<String, String>(); //current Translation Table (maintaining input order)

        final List<String> list = FileUtils.getListArray(translationFile);
        String forFile = "";

        for (final String line : list) {
            if (!line.isEmpty()) {
                if (line.charAt(0) != '#') {
                    final String[] split = line.split("==", 2);
                    if (split.length == 2) {
                        translationList.put(split[0], split[1]);
                        //}else{ //Invalid line
                    }
                } else if (line.startsWith("#File:")) {
                    if (!forFile.isEmpty()) {
                        lists.put(forFile, translationList);
                    }
                    forFile = line.substring(6).trim(); //skip "#File:"
                    if (lists.containsKey(forFile)) {
                        translationList = lists.get(forFile);
                    } else {
                        translationList = new LinkedHashMap<String, String>();
                    }
                }
            }
        }
        lists.put(forFile, translationList);
        return lists;
    }

//    public static boolean translateFile(final URL sourceFile, final File destFile, final URL translationFile){
//        return translateFile(sourceFile, destFile, loadTranslationsLists(translationFile).get(sourceFile.getName()));
//    }

    /**
     * Translate sourceFile to destFile using translationList.
     * @param sourceFile file to translate
     * @param destFile file to write
     * @param translationList map of translations
     * @return true when destFile was sucessfully written, false otherwise
     */
    public static boolean translateFile(final URL sourceFile, final File destFile, final Map<String, String> translationList){

		StringBuilder content = new StringBuilder();
		BufferedReader br = null;
		try {
			InputStream inStream = sourceFile.openStream();
			if (inStream == null) {
				return false;
			}
			br = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
			String line = null;
			while ((line = br.readLine()) != null) {
				content.append(line).append(net.yacy.server.serverCore.CRLF_STRING);
			}
		} catch (final IOException e) {
			return false;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (final Exception e) {
				}
			}
		}

        String processedContent = translate(content.toString(), translationList);
        BufferedWriter bw = null;
        try{
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), StandardCharsets.UTF_8));
            bw.write(processedContent);
            bw.close();
        }catch(final IOException e){
            return false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (final Exception e) {}
            }
        }

	return true;
    }

    public static boolean translateFiles(final URL sourceDir, final File destDir, final URL baseDir, final URL translationFile, final String extensions){
        return translateFiles(sourceDir, destDir, baseDir, loadTranslationsLists(translationFile), extensions);
    }

    public static boolean translateFiles(final URL sourceDir, final File destDir, final URL baseDir, final Map<String, Map<String, String>> translationLists, final String extensions){
        destDir.mkdirs();
        final List<String> exts = ListManager.string2vector(extensions);
        List<URL> sourceFiles = ResourceUtils.listFileResources(sourceDir);
        String relativePath;
        int baseDirURLLength = baseDir.toExternalForm().length();
        for (final URL sourceFile : sourceFiles) {
        	String sourceURLString = sourceFile.toExternalForm();
        	boolean extOK = false;
        	for(String ext : exts) {
        		if(sourceURLString.endsWith(ext)) {
        			extOK = true;
        			break;
        		}
        	}
        	if(!extOK) {
        		continue;
        	}
            try {
                relativePath = sourceFile.toExternalForm().substring(baseDirURLLength);
                if(relativePath.startsWith("/")) {
                	relativePath = relativePath.substring(1);
                }
            } catch (final IndexOutOfBoundsException e) {
                 ConcurrentLog.severe("TRANSLATOR", "Error creating relative Path for " + sourceFile.toExternalForm());
                 relativePath = "wrong path"; //not in translationLists
            }
            if (translationLists.containsKey(relativePath)) {
                ConcurrentLog.info("TRANSLATOR", "Translating file: "+ relativePath);
                if(!translateFile(
                                  sourceFile,
                                  new File(destDir, ResourceUtils.getFileName(sourceFile)),
                                  translationLists.get(relativePath)))
                {
                    ConcurrentLog.severe("TRANSLATOR", "File error while translating file " + relativePath);
                }
                //}else{
                    //serverLog.logInfo("TRANSLATOR", "No translation for file: "+relativePath);
            }
        }
        return true;
    }

    public static boolean translateFilesRecursive(final URL sourceDir, final File destDir, final URL translationFile, final String extensions, final String notdir){
    	final List<URL> dirList = ResourceUtils.listRecursiveDirectories(sourceDir, notdir);
        dirList.add(sourceDir);
        int sourceDirURLLength = sourceDir.toExternalForm().length();
		for (final URL childSourceDir : dirList) {
			// cuts the sourcePath and prepends the destPath
			String childDestPath = childSourceDir.toExternalForm().substring(sourceDirURLLength);
			File childDestDir = new File(destDir, childDestPath);
			translateFiles(childSourceDir, childDestDir, sourceDir, translationFile, extensions);
		}
        return true;
    }

    public static Map<String, String> langMap(@SuppressWarnings("unused") final serverSwitch env) {
        final String[] ms = CommonPattern.COMMA.split(
            "default/English,de/Deutsch,fr/Fran&ccedil;ais,nl/Nederlands,it/Italiano,es/Espa&ntilde;ol,pt/Portug&ecirc;s,fi/Suomi,se/Svenska,dk/Dansk," +
            "gr/E&lambda;&lambda;&eta;v&iota;&kappa;&alpha;,sk/Slovensky,cn/&#27721;&#35821;/&#28450;&#35486;," +
            "ru/&#1056;&#1091;&#1089;&#1089;&#1082;&#1080;&#1081;,uk/&#1059;&#1082;&#1088;&#1072;&#1111;&#1085;&#1089;&#1100;&#1082;&#1072;," + 
            "hi/&#2361;&#2367;&#2344;&#2381;&#2342;&#2368;"
            );
        final Map<String, String> map = new HashMap<String, String>();
        for (final String element : ms) {
            int p = element.indexOf('/');
            if (p > 0)
                map.put(element.substring(0, p), element.substring(p + 1));
        }
        return map;
    }

    /**
     * Change interface language to specified lang code
     * @param env server environnment. Must not be null.
     * @param translationFile translation file url. May be null when lang is "default"
     * @param lang target lang code (2 characters)
     * @return true when language was changed and no error occured
     */
    public static boolean changeLang(final serverSwitch env, final URL translationFile, final String lang) {
        boolean ret = false;

        if ("default".equals(lang)) {
            env.setConfig("locale.language", "default");
            ret = true;
        } else {
            URL htrootURL = env.getAppFileOrDefaultResource(SwitchboardConstants.HTROOT_PATH, SwitchboardConstants.HTROOT_PATH_DEFAULT_RESOURCE);
            final File destDir = new File(env.getDataPath("locale.translated_html", "DATA/LOCALE/htroot"), lang);// cut
            
			if(htrootURL != null) {
				if (Translator.translateFilesRecursive(htrootURL, destDir, translationFile, "html,template,inc", "locale")) {
					env.setConfig("locale.language", lang);
					Formatter.setLocale(env.getConfig("locale.language", "en"));
					BufferedWriter bw = null;
					try {
						bw = new BufferedWriter(new PrintWriter(new FileWriter(new File(destDir, "version"))));
						bw.write(env.getConfig("svnRevision", "Error getting Version"));
						bw.close();
					} catch (final IOException e) {
						ConcurrentLog.warn("TRANSLATOR", "Could write svnRevision");
					} finally {
						if(bw != null) {
							try {
								bw.close();
							} catch (IOException ignored) {
							}
						}
					}
					ret = true;
				}
			}
        }
        return ret;
    }

    /**
     * List language files under langPath classpath directory
     * @param langPath languages classpath directory 
     * @return a list of language files URLs eventually empty
     */
    public static List<URL> langFiles(URL langPath) {
    	List<URL> resources = ResourceUtils.listFileResources(langPath);
    	List<URL> langFiles = new ArrayList<>();
    	for(URL resource : resources) {
    		String fileName = ResourceUtils.getFileName(resource);
            if (fileName.matches(Translator.LANG_FILENAME_FILTER) ) {
            	langFiles.add(resource);
            }
    	}
        return langFiles;
    }
}
