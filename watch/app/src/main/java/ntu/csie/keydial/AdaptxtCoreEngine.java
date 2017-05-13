/******************************************************************************
 *
 * Copyright 2013 KeyPoint Technologies (UK) Ltd.   
 * All rights reserved. This program and the accompanying materials   
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 *
 * Contributors: 
 * KeyPoint Technologies (UK) Ltd - Initial API and implementation
 *
 *****************************************************************************/
package ntu.csie.keydial;

import android.content.res.AssetManager;
import android.util.Log;

import com.kpt.adaptxt.core.coreapi.KPTCommands.KPTCmd;
import com.kpt.adaptxt.core.coreapi.KPTCore;
import com.kpt.adaptxt.core.coreapi.KPTFrameWork;
import com.kpt.adaptxt.core.coreapi.KPTParamInputInsertion;
import com.kpt.adaptxt.core.coreapi.KPTParamInputResetRemoveReplace;
import com.kpt.adaptxt.core.coreapi.KPTParamSuggestion;
import com.kpt.adaptxt.core.coreapi.KPTParamSuggestionConfig;
import com.kpt.adaptxt.core.coreapi.KPTParamValidSubjectDictWord;
import com.kpt.adaptxt.core.coreapi.KPTSuggEntry;
import com.kpt.adaptxt.core.coreapi.KPTTypes.KPTStatusCode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Handles the Adaptxt core engine calls. Also provides
 * singleton reference to core engine handle.
 *
 * @author KPT
 */
public class AdaptxtCoreEngine {

    /**
     * The JNI core engine handle using which all Core engine function calls are made
     */
    private KPTCore mAdaptxtCore;

    /**
     * Static reference to this class to provide single instance across binders.
     */
    private static AdaptxtCoreEngine sCoreEngine = null;

    /**
     * Holds the profile path for Adaptxt engine
     */
    private String mApkDirPath = null;


    /**
     * Global suggestion status/error code value
     */
    private KPTStatusCode mSuggStatusCode;

    /**
     * Log string
     */
    private static String KPTDebugString = "CoreEngine";


    /**
     * Default constructor
     */
    AdaptxtCoreEngine() {

    }

    /**
     * Static getter function to provide single engine instance to all callers.
     */
    public static AdaptxtCoreEngine getCoreEngineImpl() {
        if (sCoreEngine == null) {
            sCoreEngine = new AdaptxtCoreEngine();
        }
        return sCoreEngine;
    }

    /**
     * Initializes handle to the core.
     */
    public boolean initializeCore() {
        mAdaptxtCore = new KPTCore();
        // Creating framework
        KPTStatusCode statuscode = mAdaptxtCore.KPTFwkCreate(0, 1,
                mApkDirPath, true);
        if (statuscode == KPTStatusCode.KPT_SC_SUCCESS
                || statuscode == KPTStatusCode.KPT_SC_ALREADYEXISTS) {
            KPTFrameWork fwk = new KPTFrameWork();
            mAdaptxtCore.KPTFwkGetVersion(fwk);
            setErrorCorrection(true, 30);
        } else {
            Log.e(KPTDebugString, "===>> core create failed------>** " + statuscode);
        }
        return true;
    }

    /**
     * Copies required core files from assets to the profile folder.
     *
     * @param filePath
     * @param assetMgr
     */
    public void prepareCoreFiles(String filePath, AssetManager assetMgr) {
        mApkDirPath = filePath + "/Profile";
        atxAssestCopy(filePath, assetMgr);
        mApkDirPath = mApkDirPath + "/Profile";
    }

    /**
     * Clears existing text from engine
     *
     * @return
     */
    public boolean resetCoreString() {
        KPTParamInputResetRemoveReplace resetString = new KPTParamInputResetRemoveReplace(1);
        resetString.setResetInfo(0, "");
        KPTStatusCode statuscode = mAdaptxtCore.KPTFwkRunCmd(KPTCmd.KPTCMD_INPUTMGR_RESET, resetString);
        if (statuscode == KPTStatusCode.KPT_SC_SUCCESS) {
            return true;
        }

        return false;
    }

    public void selectFirst(KPTSuggestion sugg) {
        KPTParamInputInsertion inputInsertion = new KPTParamInputInsertion(1);
        inputInsertion.setInsertSuggestionRequest(0, sugg.mSuggestionId, true);
        KPTStatusCode statuscode = mAdaptxtCore.KPTFwkRunCmd(KPTCmd.KPTCMD_INPUTMGR_INSERTSUGG, inputInsertion);
        if (statuscode == KPTStatusCode.KPT_SC_SUCCESS) {
            Log.e(KPTDebugString, "KPTCMD_INPUTMGR_INSERTSUGG done :" + statuscode);
        } else {
            Log.e(KPTDebugString, "KPTCMD_INPUTMGR_INSERTSUGG failed :" + statuscode);
            return;
        }
    }

    /**
     * Inserts a single character into the Adaptxt engine and gets suggestions
     *
     * @param c Character to be inserted
     * @return List of suggestions after inserting a single character
     */
    public List<KPTSuggestion> insertChar(char chr) {
        resetCoreString();
        KPTParamInputInsertion insertChar = new KPTParamInputInsertion(1);
        insertChar.setInsertChar(chr, 0, 0);
        KPTStatusCode statuscode = mAdaptxtCore.KPTFwkRunCmd(KPTCmd.KPTCMD_INPUTMGR_INSERTCHAR, insertChar);

        if (statuscode == KPTStatusCode.KPT_SC_SUCCESS) {
            KPTParamSuggestion getSuggs = new KPTParamSuggestion(1, 0);
            mSuggStatusCode = mAdaptxtCore.KPTFwkRunCmd(KPTCmd.KPTCMD_SUGGS_GETSUGGESTIONS, getSuggs);

            List<KPTSuggestion> sugg = null;
            if (mSuggStatusCode == KPTStatusCode.KPT_SC_SUCCESS) {
                sugg = new ArrayList<KPTSuggestion>();
                int count = getSuggs.getSuggestionEntries().length;
                KPTSuggEntry[] entry = getSuggs.getSuggestionEntries();
                KPTSuggestion suggestion;
                for (int i = 0; i < count; i++) {
                    suggestion = new KPTSuggestion();
                    suggestion.setsuggestionString(entry[i].getSuggestionString());
                    suggestion.setsuggestionType(entry[i].getSuggestionType());
                    suggestion.setsuggestionId(entry[i].getSuggestionId());
                    sugg.add(suggestion);
                }

            } else {
                Log.e(KPTDebugString, "KPTCMD_SUGGS_GETSUGGESTIONS failed :" + statuscode);
            }
            return sugg;
        } else {
            Log.e(KPTDebugString, "KPTCMD_INPUTMGR_INSERTCHAR failed :" + statuscode);
            return null;
        }

    }

    /**
     * Inserts given text in the core engine and provides suggestions
     *
     * @param text Text to be inserted
     * @return List of suggestions
     */
    public List<KPTSuggestion> insertTextAndGetSuggestions(String text) {

        resetCoreString();
        KPTParamInputInsertion insertString = new KPTParamInputInsertion(1);
        insertString.setInsertString(text, text.length(), 0, 0, 0, null);
        KPTStatusCode statuscode = mAdaptxtCore.KPTFwkRunCmd(KPTCmd.KPTCMD_INPUTMGR_INSERTSTRING, insertString);

        if (statuscode == KPTStatusCode.KPT_SC_SUCCESS) {
            KPTParamSuggestion getSuggs = new KPTParamSuggestion(1, 0);
            mSuggStatusCode = mAdaptxtCore.KPTFwkRunCmd(KPTCmd.KPTCMD_SUGGS_GETSUGGESTIONS, getSuggs);

            List<KPTSuggestion> sugg = null;
            if (mSuggStatusCode == KPTStatusCode.KPT_SC_SUCCESS) {
                sugg = new ArrayList<KPTSuggestion>();
                int count = getSuggs.getSuggestionEntries().length;
                KPTSuggEntry[] entry = getSuggs.getSuggestionEntries();
                KPTSuggestion suggestion;
                for (int i = 0; i < count; i++) {
                    suggestion = new KPTSuggestion();
                    suggestion.setsuggestionString(entry[i].getSuggestionString());
                    suggestion.setsuggestionType(entry[i].getSuggestionType());
                    suggestion.setsuggestionId(entry[i].getSuggestionId());
                    sugg.add(suggestion);
                }
            } else {
                Log.e(KPTDebugString, "KPTCMD_SUGGS_GETSUGGESTIONS failed :" + statuscode);
            }
            return sugg;
        } else {
            Log.e(KPTDebugString, "KPTCMD_INPUTMGR_INSERTSTRING failed :" + statuscode);
            return null;
        }

    }

    /**
     * Checks if a given word has a valid spelling
     *
     * @param text String whose spelling needs to be validated
     * @return return true if spelling is valid
     */
    public boolean isValidWord(String text) {
        KPTParamValidSubjectDictWord word = new KPTParamValidSubjectDictWord(1);
        word.mStringToValidate = text;
        word.mIsStringValid = false;
        mSuggStatusCode = mAdaptxtCore.KPTFwkRunCmd(KPTCmd.KPTCMD_INPUTMGR_ISVALIDWORD, word);

        if (mSuggStatusCode == KPTStatusCode.KPT_SC_SUCCESS) {
            return word.mIsStringValid;
        } else {
            Log.e(KPTDebugString, "KPTCMD_INPUTMGR_ISVALIDWORD failed : " + mSuggStatusCode);
            return false;
        }
    }

    /**
     * Inserts a given suggestion into Adaptxt core engine
     *
     * @param aSuggSet      suggestion set identifier
     * @param aSuggestionId suggestion identifier
     * @param autoSpace     Should space be appended after suggestion
     * @return If suggestion insertion successful
     */
    public boolean insertSuggestion(int aSuggSet, int aSuggestionId, boolean autoSpace) {
        KPTParamInputInsertion insertSugg = new KPTParamInputInsertion(1);

        insertSugg.setInsertSuggestionRequest(aSuggSet, aSuggestionId, autoSpace);

        KPTStatusCode statusCode = mAdaptxtCore.KPTFwkRunCmd(KPTCmd.KPTCMD_INPUTMGR_INSERTSUGG, insertSugg);

        int charBeforeCursor = insertSugg.getCharactersRemovedBeforeCursor();
        int charAfterCursor = insertSugg.getCharactersRemovedAfterCursor();
        String modString = insertSugg.getModificationString();

        Log.i(KPTDebugString, "InsertSuggestion " + charBeforeCursor + " " + charAfterCursor + " " + modString);

        if (statusCode != KPTStatusCode.KPT_SC_SUCCESS) {
            Log.e(KPTDebugString, "InsertSuggestion failed statuscode= " + statusCode);
            return false;
        }
        return true;
    }

    /**
     * Sets the error correction settings in the core engine
     *
     * @param errorCorrect
     * @return True if setting in core engine is successful
     */
    public boolean setErrorCorrection(boolean errorCorrect, int maxNumSuggestions) {
        int maskEC = KPTParamSuggestionConfig.KPT_SUGGS_CONFIG_ERROR_CORRECTION | KPTParamSuggestionConfig.KPT_SUGGS_CONFIG_MAX_SUGGESTIONS | KPTParamSuggestionConfig.KPT_SUGGS_CONFIG_COMPLETION | KPTParamSuggestionConfig.KPT_SUGGS_CONFIG_SENTENCE_CASE;
        KPTParamSuggestionConfig config = new KPTParamSuggestionConfig(1);
        config.setFieldMasktemp(maskEC);
        config.setErrorCorrectionOn(true);
        config.setMaxNumSuggestions(maxNumSuggestions);
        config.setCompletionOn(true);
        config.setUseSentenceCase(true);
        //config.setproximitySuggestionOn(true);
        mSuggStatusCode = mAdaptxtCore.KPTFwkRunCmd(KPTCmd.KPTCMD_SUGGS_SETCONFIG, config);
        if (mSuggStatusCode == KPTStatusCode.KPT_SC_SUCCESS) {
            return true;
        } else {
            Log.e(KPTDebugString, "KPTCMD_SUGGS_SETCONFIG failed " + mSuggStatusCode);
            return false;
        }
    }

    /**
     * Destroys the core engine instance
     */
    public void destroyCore() {

        KPTStatusCode statuscode = mAdaptxtCore.KPTFwkDestroy();
        if (statuscode != KPTStatusCode.KPT_SC_SUCCESS) {
            Log.e(KPTDebugString, "core not destroyed properly " + statuscode);
        }
        sCoreEngine = null;
    }

    /**
     * Initiates copy of core files from assets folder
     *
     * @param filePath
     * @param assetMgr
     */
    public static void atxAssestCopy(String filePath, AssetManager assetMgr) {

        String path = filePath + "/Profile/Profile";
        File atxFile = new File(path);
        if (atxFile.exists()) {
            return;
        }

        AssetManager am = assetMgr;
        try {
            InputStream isd = am.open("Profile.zip");
            OutputStream os = new FileOutputStream(filePath + "/Profile.zip");
            byte[] b = new byte[627572];
            int length;
            while ((length = isd.read(b)) > 0) {
                os.write(b, 0, length);
            }
            isd.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String zipPath = filePath + "/Profile.zip";
        File atxZipFile = new File(zipPath);
        if (atxZipFile.exists()) {
            unzip(zipPath);
        }

        atxZipFile.delete();
    }

    /**
     * Reads and unzips a zipped file
     *
     * @param zipFileName
     */
    private static void unzip(String zipFileName) {
        try {
            File file = new File(zipFileName);
            ZipFile zipFile = new ZipFile(file);

            // create a directory named the same as the zip file in the
            // same directory as the zip file.
            File zipDir = new File(file.getParentFile(), "Profile");
            zipDir.mkdir();

            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                String nme = entry.getName();
                // File for current file or directory
                File entryDestination = new File(zipDir, nme);

                // This file may be in a subfolder in the Zip bundle
                // This line ensures the parent folders are all
                // created.
                entryDestination.getParentFile().mkdirs();

                // Directories are included as seperate entries
                // in the zip file.
                if (!entry.isDirectory()) {
                    generateFile(entryDestination, entry, zipFile);
                } else {
                    entryDestination.mkdirs();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new file
     */
    private static void generateFile(File destination, ZipEntry entry, ZipFile owner) {
        InputStream in = null;
        OutputStream out = null;

        InputStream rawIn;
        try {
            rawIn = owner.getInputStream(entry);
            in = new BufferedInputStream(rawIn, 1024);
            FileOutputStream rawOut = new FileOutputStream(destination);
            out = new BufferedOutputStream(rawOut, 1024);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper class to hold the suggestions returned from Adaptxt core engine
     *
     * @author KPT
     */
    public class KPTSuggestion {

        /**
         * Empty Suggestion.
         * This value is used for initialization only.
         *
         * @see KPT_SUGGS_TYPE_EMPTY::suggestionType
         */
        public static final int KPT_SUGGS_TYPE_EMPTY = 0;

        /**
         * Error Correction.
         * The suggestion is generated by the error correction algorithms.
         */
        public static final int KPT_SUGGS_TYPE_ERROR_CORRECTION = 10;

        /**
         * Unique suggestion Id
         */
        private int mSuggestionId;

        /**
         * Suggestion type, if regular or error correction
         */
        private int mSsuggestionType;

        /**
         * Suggestion string
         */
        private String mSuggestionString;

        @Override
        public String toString() {
            return String.format("%s %s", mSsuggestionType, mSuggestionString);
        }

        /**
         * Sets the suggestion Id
         *
         * @param suggestionId
         */
        public void setsuggestionId(int suggestionId) {
            mSuggestionId = suggestionId;
        }

        /**
         * Sets the suggestion type
         *
         * @param suggestionType
         */
        public void setsuggestionType(int suggestionType) {
            mSsuggestionType = suggestionType;
        }

        /**
         * Sets the suggestion string
         *
         * @param suggestionString
         */
        public void setsuggestionString(String suggestionString) {
            mSuggestionString = suggestionString;
        }

        /**
         * Gets the suggestion Id
         *
         * @return
         */
        public int getSuggestionId() {
            return mSuggestionId;
        }

        /**
         * Gets the suggestion type
         *
         * @return
         */
        public int getSuggestionType() {
            return mSsuggestionType;
        }

        /**
         * Gets the suggestion string
         *
         * @return
         */
        public String getSuggestionString() {
            return mSuggestionString;
        }


    }
}

//End of file
