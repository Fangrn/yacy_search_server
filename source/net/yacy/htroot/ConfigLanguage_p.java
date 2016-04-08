package net.yacy.htroot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.yacy.cora.document.id.DigestURL;
import net.yacy.cora.protocol.ClientIdentification;
import net.yacy.cora.protocol.RequestHeader;
import net.yacy.data.Translator;
import net.yacy.data.WorkTables;
import net.yacy.kelondro.util.FileUtils;
import net.yacy.kelondro.util.ResourceUtils;
import net.yacy.search.Switchboard;
import net.yacy.server.serverObjects;
import net.yacy.server.serverSwitch;


public class ConfigLanguage_p {

    public static serverObjects respond(@SuppressWarnings("unused") final RequestHeader header, final serverObjects post, final serverSwitch env) {

        final serverObjects prop = new serverObjects();
        Switchboard sb = (Switchboard) env;
        final URL langPathURL = sb.getAppFileOrDefaultResource("locale.source", "/locales/");

        //Fallback
        //prop.put("currentlang", ""); //is done by Translationtemplate
        prop.put("status", "0");//nothing

        List<URL> langFiles = Translator.langFiles(langPathURL);
        if (langFiles == null) {
            return prop;
        }

        if (post != null) {
            final String selectedLanguage = post.get("language");

            // store this call as api call
            ((Switchboard) env).tables.recordAPICall(post, "ConfigLanguage_p.html", WorkTables.TABLE_API_TYPE_CONFIGURATION, "language settings: " + selectedLanguage);

            //change language
            if (post.containsKey("use_button") && selectedLanguage != null){
                /* Only change language if filename is contained in list of filesnames
                 * read from the language directory. This is very important to prevent
                 * directory traversal attacks!
                 */
                if (langFiles.contains(selectedLanguage) || selectedLanguage.startsWith("default")) {
                    Translator.changeLang(env, langPathURL, selectedLanguage);
                }

                //delete language file
            } else if (post.containsKey("delete")) {

                /* Only delete file if filename is contained in list of filesnames
                 * read from the language directory. This is very important to prevent
                 * directory traversal attacks!
                 */
            	File langDir = null;
            	try {
					langDir = new File(langPathURL.toURI());
				} catch (URISyntaxException ignored) {
				}
                if (langFiles.contains(selectedLanguage) && langDir != null) {
                    final File langfile= new File(langDir, selectedLanguage);
                    FileUtils.deletedelete(langfile);
                }

                //load language file from URL
            } else if (post.containsKey("url")){
                final String url = post.get("url");
                Iterator<String> it;
                try {
                    final DigestURL u = new DigestURL(url);
                    it = FileUtils.strings(u.get(ClientIdentification.yacyInternetCrawlerAgent, null, null));
                	try {
                    	final File langFile = new File(new File(langPathURL.toURI()), url.substring(url.lastIndexOf('/'), url.length()));
                    	final OutputStreamWriter bw = new OutputStreamWriter(new FileOutputStream(langFile), StandardCharsets.UTF_8.name());

                    	while (it.hasNext()) {
                        	bw.write(it.next() + "\n");
                    	}
                    	bw.close();
                		if (post.containsKey("use_lang") && "on".equals(post.get("use_lang"))) {
                    		Translator.changeLang(env, langPathURL, url.substring(url.lastIndexOf('/'), url.length()));
                		}
                	} catch(final IOException e) {
                    	prop.put("status", "2");//error saving the language file
                    	return prop;
                	} catch (URISyntaxException e) {
                    	prop.put("status", "2");//error saving the language file
                    	return prop;
					}
                } catch(final IOException e) {
                    prop.put("status", "1");//unable to get url
                    prop.put("status_url", url);
                }
            }
        }

        //re-read language files
        langFiles = Translator.langFiles(langPathURL);
        Collections.sort(langFiles, new Comparator<URL>() {

			@Override
			public int compare(URL o1, URL o2) {
				int res = 0;
				if(o1 != null && o2 != null) {
					res = o1.toExternalForm().compareTo(o2.toExternalForm());
				}
				return 0;
			}
		});
        final Map<String, String> langNames = Translator.langMap(env);

        //virtual entry
        prop.put("langlist_0_file", "default");
        prop.put("langlist_0_name", ((langNames.get("default") == null) ? "default" : langNames.get("default")));
        prop.put("langlist_0_selected", "selected=\"selected\"");

        int count = 0;
        for (final URL langFileURL : langFiles) {
            //+1 because of the virtual entry "default" at top
        	String langFile = ResourceUtils.getFileName(langFileURL);
            final String langKey = langFile.substring(0, langFile.length() -4);
            final String langName = langNames.get(langKey);
            prop.put("langlist_" + (count + 1) + "_file", langFile);
            prop.put("langlist_" + (count + 1) + "_name", ((langName == null) ? langKey : langName));

            if(env.getConfig("locale.language", "default").equals(langKey)) {
                prop.put("langlist_" + (count + 1) + "_selected", "selected=\"selected\"");
                prop.put("langlist_0_selected", " "); // reset Default
            } else {
                prop.put("langlist_" + (count + 1) + "_selected", " ");
            }
            count++;
        }
        prop.put("langlist", (count + 1));

        //is done by Translationtemplate
        //langName = (String) langNames.get(env.getConfig("locale.language", "default"));
        //prop.put("currentlang", ((langName == null) ? "default" : langName));
        return prop;
    }
}
