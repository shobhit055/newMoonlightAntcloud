package com.antcloud.app.computers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Random;

import com.antcloud.app.ui.LimeLog;

import android.content.Context;

public class IdentityManager {
    private static final String UNIQUE_ID_FILE_NAME = "uniqueid";
    private static final int UID_SIZE_IN_BYTES = 8;

    private String uniqueId;

    public IdentityManager(Context c) {
        uniqueId = loadUniqueId(c);
        if (uniqueId == null) {
            uniqueId = generateNewUniqueId(c);
        }

        LimeLog.info("UID is now: "+uniqueId);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    private static String loadUniqueId(Context c) {
        // 2 Hex digits per byte
        char[] uid = new char[UID_SIZE_IN_BYTES * 2];
        LimeLog.info("Reading UID from disk");
        try (final InputStreamReader reader =
                     new InputStreamReader(c.openFileInput(UNIQUE_ID_FILE_NAME))
        ) {
            if (reader.read(uid) != UID_SIZE_IN_BYTES * 2) {
                LimeLog.severe("UID file data is truncated");
                return null;
            }
            return new String(uid);
        } catch (FileNotFoundException e) {
            LimeLog.info("No UID file found");
            return null;
        } catch (IOException e) {
            LimeLog.severe("Error while reading UID file");
            e.printStackTrace();
            return null;
        }
    }

    private static String generateNewUniqueId(Context c) {
        // Generate a new UID hex string
        LimeLog.info("Generating new UID");
        String uidStr = String.format((Locale)null, "%016x", new Random().nextLong());

        try (final OutputStreamWriter writer =
                     new OutputStreamWriter(c.openFileOutput(UNIQUE_ID_FILE_NAME, 0))
        ) {
            writer.write(uidStr);
            LimeLog.info("UID written to disk");
        } catch (IOException e) {
            LimeLog.severe("Error while writing UID file");
            e.printStackTrace();
        }

        // We can return a UID even if I/O fails
        return uidStr;
    }
}
