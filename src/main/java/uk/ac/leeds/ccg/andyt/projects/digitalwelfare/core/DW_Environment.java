/*
 * Copyright (C) 2016 geoagdt.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package uk.ac.leeds.ccg.andyt.projects.digitalwelfare.core;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_StaticIO;
import uk.ac.leeds.ccg.andyt.grids.core.Grids_Environment;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.core.log.DW_Log;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.data.postcode.DW_Postcode_Handler;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.data.shbe.DW_SHBE_CollectionHandler;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.data.shbe.DW_SHBE_Handler;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.data.shbe.DW_SHBE_TenancyType_Handler;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.data.underoccupied.DW_UO_Data;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.data.underoccupied.DW_UO_Handler;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.data.underoccupied.DW_UO_Set;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.io.DW_Files;

/**
 * This class is for an instance of the environment for the Digital Welfare
 * program. It contains holders for commonly referred to objects that might
 * otherwise be constructed multiple times. It is also for handling memory
 * although for the time being, there has not been a need for convoluted
 * swapping of data from memory to disk.
 *
 * @author agdturner
 */
public class DW_Environment extends DW_OutOfMemoryErrorHandler
        implements Serializable {

    public String sDigitalWelfareDir = "/scratch02/DigitalWelfare";
    //public String sDigitalWelfareDir = "C:/Users/geoagdt/projects/DigitalWelfare";

    /**
     * For storing an instance of DW_Strings for accessing Strings.
     */
    private DW_Strings DW_Strings;

    /**
     * For returning an instance of DW_Strings for convenience.
     *
     * @return
     */
    public DW_Strings getDW_Strings() {
        if (DW_Strings == null) {
            DW_Strings = new DW_Strings();
        }
        return DW_Strings;
    }

    /**
     * For storing an instance of DW_Files for accessing filenames and Files
     * therein.
     */
    private DW_Files DW_Files;

    /**
     * For returning an instance of DW_Files for convenience.
     *
     * @return
     */
    public DW_Files getDW_Files() {
        if (DW_Files == null) {
            DW_Files = new DW_Files(this);
        }
        return DW_Files;
    }

    /**
     * For storing an instance of Grids_Environment
     */
    private transient Grids_Environment Grids_Environment;

    /**
     * For returning an instance of Grids_Environment for convenience.
     *
     * @return
     */
    public Grids_Environment getGrids_Environment() {
        if (Grids_Environment == null) {
            Grids_Environment = new Grids_Environment();
        }
        return Grids_Environment;
    }

    /**
     * For storing an instance of HashMap<String, DW_SHBE_CollectionHandler>.
     */
    private HashMap<String, DW_SHBE_CollectionHandler> DW_SHBE_CollectionHandlers;

    /**
     * For returning an instance of DW_SHBE_CollectionHandler for convenience.
     *
     * @return
     */
    public HashMap<String, DW_SHBE_CollectionHandler> getDW_SHBE_CollectionHandlers() {
        if (DW_SHBE_CollectionHandlers == null) {
            DW_SHBE_CollectionHandlers = new HashMap<String, DW_SHBE_CollectionHandler>();
        }
        return DW_SHBE_CollectionHandlers;
    }

    /**
     * For returning an instance of DW_SHBE_CollectionHandler for convenience.
     *
     * @param paymentType
     * @return
     */
    public DW_SHBE_CollectionHandler getDW_SHBE_CollectionHandler(String paymentType) {
        DW_SHBE_CollectionHandler result;
        DW_SHBE_CollectionHandlers = getDW_SHBE_CollectionHandlers();
        if (DW_SHBE_CollectionHandlers.containsKey(paymentType)) {
            result = DW_SHBE_CollectionHandlers.get(paymentType);
        } else {
            File dir;
            dir = new File(
                    DW_Files.getSwapSHBEDir(),
                    paymentType);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            result = new DW_SHBE_CollectionHandler(
                    this,
                    paymentType);
            result.nextID = 0L;
            DW_SHBE_CollectionHandlers.put(paymentType, result);
        }
        return result;
    }

    /**
     * For storing an instance of DW_UO_Handler for convenience.
     */
    protected DW_UO_Handler DW_UO_Handler;

    /**
     * For returning an instance of DW_UO_Handler for convenience.
     *
     * @return
     */
    public DW_UO_Handler getDW_UO_Handler() {
        if (DW_UO_Handler == null) {
            DW_UO_Handler = new DW_UO_Handler(this);
        }
        return DW_UO_Handler;
    }

    /**
     * For storing an instance of DW_UO_Data.
     */
    protected DW_UO_Data DW_UO_Data;

    /**
     * For returning an instance of DW_UO_Handler for convenience.
     *
     * @return
     */
    public DW_UO_Data getDW_UO_Data() {
        if (DW_UO_Data == null) {
            DW_UO_Handler = getDW_UO_Handler();
            File f;
            f = new File(
                    DW_Files.getGeneratedUnderOccupiedDir(),
                    "DW_UO_Data.thisFile");
            if (f.exists()) {
                DW_UO_Data = (DW_UO_Data) Generic_StaticIO.readObject(f);
                // For debugging/testing load
                TreeMap<String, DW_UO_Set> tCouncilSets;
                tCouncilSets = DW_UO_Data.getCouncilSets();
                TreeMap<String, DW_UO_Set> tRSLSets;
                tRSLSets = DW_UO_Data.getRSLSets();
                int totalSets;
                totalSets = tCouncilSets.size() + tRSLSets.size();
                System.out.println("totalSets loaded " + totalSets);
                int numberOfInputFiles;
                numberOfInputFiles = DW_UO_Handler.getNumberOfInputFiles();
                System.out.println("numberOfInputFiles " + numberOfInputFiles);
                //if ()
            } else {
                boolean reload;
                reload = false;
                DW_UO_Data = DW_UO_Handler.loadUnderOccupiedReportData(reload);
                Generic_StaticIO.writeObject(DW_UO_Data, f);
            }
        }
        return DW_UO_Data;
    }

    /**
     * For storing an instance of DW_Postcode_Handler for convenience.
     */
    private DW_Postcode_Handler DW_Postcode_Handler;

    /**
     * For returning an instance of DW_Postcode_Handler for convenience.
     *
     * @return
     */
    public DW_Postcode_Handler getDW_Postcode_Handler() {
        if (DW_Postcode_Handler == null) {
            DW_Postcode_Handler = new DW_Postcode_Handler(this);
        }
        return DW_Postcode_Handler;
    }

    /**
     * For storing an instance of DW_SHBE_Handler for convenience.
     */
    private DW_SHBE_Handler DW_SHBE_Handler;

    /**
     * For returning an instance of DW_SHBE_Handler for convenience.
     *
     * @return
     */
    public DW_SHBE_Handler getDW_SHBE_Handler() {
        if (DW_SHBE_Handler == null) {
            DW_SHBE_Handler = new DW_SHBE_Handler(this);
        }
        return DW_SHBE_Handler;
    }

    /**
     * For storing an instance of DW_SHBE_TenancyType_Handler for convenience.
     */
    private DW_SHBE_TenancyType_Handler DW_SHBE_TenancyType_Handler;

    /**
     * For returning an instance of DW_SHBE_TenancyType_Handler for convenience.
     *
     * @return
     */
    public DW_SHBE_TenancyType_Handler getDW_SHBE_TenancyType_Handler() {
        if (DW_SHBE_TenancyType_Handler == null) {
            DW_SHBE_TenancyType_Handler = new DW_SHBE_TenancyType_Handler(this);
        }
        return DW_SHBE_TenancyType_Handler;
    }

    public DW_Environment(String sDigitalWelfareDir) {
        init_DW_Environment(sDigitalWelfareDir);
    }

    private void init_DW_Environment(String sDigitalWelfareDir) {
        this.sDigitalWelfareDir = sDigitalWelfareDir;
        this.DW_Files = new DW_Files(this);
        this.DW_Strings = new DW_Strings();
    }

    @Override
    public void init_MemoryReserve(
            boolean handleOutOfMemoryError) {
        try {
            init_MemoryReserve();
            tryToEnsureThereIsEnoughMemoryToContinue(handleOutOfMemoryError);
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                clear_MemoryReserve();
                if (!swapToFile_DataAny()) {
                    throw a_OutOfMemoryError;
                }
                init_MemoryReserve(handleOutOfMemoryError);
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    public int getDefaultMaximumNumberOfObjectsPerDirectory() {
        return 100;
    }

    @Override
    public boolean swapToFile_DataAny(boolean handleOutOfMemoryError) {
        try {
            boolean result = swapToFile_DataAny();
            tryToEnsureThereIsEnoughMemoryToContinue(
                    HandleOutOfMemoryErrorFalse);
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                clear_MemoryReserve();
                boolean result = swapToFile_DataAny(
                        HandleOutOfMemoryErrorFalse);
                init_MemoryReserve(HandleOutOfMemoryErrorFalse);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * A method to ensure there is enough memory to continue.
     *
     * @param handleOutOfMemoryError
     * @return
     */
    @Override
    public boolean tryToEnsureThereIsEnoughMemoryToContinue(
            boolean handleOutOfMemoryError) {
        try {
            if (tryToEnsureThereIsEnoughMemoryToContinue()) {
                return true;
            } else {
                String message
                        = "Warning! Not enough data to swap in "
                        + this.getClass().getName()
                        + ".tryToEnsureThereIsEnoughMemoryToContinue(boolean)";
                log(message);
                // Set to exit method with OutOfMemoryError
                handleOutOfMemoryError = false;
                throw new OutOfMemoryError();
            }
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                clear_MemoryReserve();
                boolean createdRoom = false;
                while (!createdRoom) {
                    if (!swapToFile_DataAny()) {
                        String message = "Warning! Not enough data to swap in "
                                + this.getClass().getName()
                                + ".tryToEnsureThereIsEnoughMemoryToContinue(boolean)";
                        log(message);
                        throw a_OutOfMemoryError;
                    }
                    try {
                        init_MemoryReserve(
                                HandleOutOfMemoryErrorFalse);
                        createdRoom = true;
                    } catch (OutOfMemoryError b_OutOfMemoryError) {
                        log(
                                "Struggling to ensure there is enough memory in "
                                + this.getClass().getName()
                                + ".tryToEnsureThereIsEnoughMemoryToContinue(boolean)");
                    }
                }
                return tryToEnsureThereIsEnoughMemoryToContinue(
                        handleOutOfMemoryError);
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * A method to try to ensure there is enough memory to continue.
     *
     * @return
     */
    @Override
    protected boolean tryToEnsureThereIsEnoughMemoryToContinue() {
        while (getTotalFreeMemory() < Memory_Threshold) {
            if (!swapToFile_DataAny()) {
                return false;
            }
        }
        return true;
    }

    private static void log(
            String message) {
        log(DW_Log.DW_DefaultLogLevel, message);
    }

    private static void log(
            Level level,
            String message) {
        Logger.getLogger(DW_Log.DW_DefaultLoggerName).log(level, message);
    }
}
