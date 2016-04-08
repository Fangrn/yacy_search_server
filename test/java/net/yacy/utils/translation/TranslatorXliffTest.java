package net.yacy.utils.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.yacy.data.Translator;
import net.yacy.kelondro.util.ResourceUtils;

public class TranslatorXliffTest {

    @Before
    public void setUp() throws Exception {
        // asure data directory exists for temp test files
        File testDataDir = new File("test/DATA");
        if (!testDataDir.exists()) {
            testDataDir.mkdir();
        }
    }

    /**
     * Test of loadTranslationsListsFromXliff method, of class TranslatorXliff.
     * load all translation lists from default locales directory writes temp
     * test files to test/DATA and compares translation text
     */
    @Test
    public void testLoadTranslationsListsFromXliff() {
        List<URL> lngFiles = Translator.langFiles(this.getClass().getResource("/locales/"));
        for (URL fileURL : lngFiles) {
        	String filename = ResourceUtils.getFileName(fileURL);
            // load translation list
            System.out.println("Test translation file " + filename);
            Map<String, Map<String, String>> origTrans =  Translator.loadTranslationsLists(fileURL);
            TranslatorXliff txlif = new TranslatorXliff();

            // save as xliff file
            File xlftmp = new File("test/DATA", filename + ".xlf");
            txlif.saveAsXliff(filename.substring(0, 2), xlftmp, origTrans);

            // load created xliff file
            Map<String, Map<String, String>> xliffTrans = TranslatorXliff.loadTranslationsListsFromXliff(xlftmp);

            // compare content
            assertEquals(origTrans.size(), xliffTrans.size());
            for (String s : origTrans.keySet()) { // get translation filename
                assertTrue(xliffTrans.containsKey(s));

                // compare translation list
                Map<String, String> origList = origTrans.get(s);
                Map<String, String> xliffList = xliffTrans.get(s);
                assertEquals(origList.size(), xliffList.size());
                for (String ss : origList.keySet()) {
                    assertTrue("translation key", xliffList.containsKey(ss));
                    String origVal = origList.get(ss);
                    // it is possible that intentionally empty translation is given
                    // in this case xliff target is missing (=null)
                    if (origVal != null && !origVal.isEmpty()) {
                        String xliffVal = xliffList.get(ss);
                        if (!origVal.equals(xliffVal)) {
                            assertEquals("translation value", origVal, xliffVal);
                        }
                    }
                }
            }
        }
    }
}
