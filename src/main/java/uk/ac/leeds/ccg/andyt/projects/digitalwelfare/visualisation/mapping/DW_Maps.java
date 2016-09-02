/*
 * Copyright (C) 2014 geoagdt.
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
package uk.ac.leeds.ccg.andyt.projects.digitalwelfare.visualisation.mapping;

//import uk.ac.leeds.ccg.andyt.agdtgeotools.DW_Shapefile;
//import uk.ac.leeds.ccg.andyt.agdtgeotools.AGDT_Geotools;
//import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.mapping.DW_Shapefile;
//import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.mapping.AGDT_Geotools;
import uk.ac.leeds.ccg.andyt.agdtgeotools.AGDT_StyleParameters;
import uk.ac.leeds.ccg.andyt.agdtgeotools.AGDT_Point;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.geotools.data.collection.TreeSetFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import uk.ac.leeds.ccg.andyt.agdtgeotools.AGDT_Maps;
import uk.ac.leeds.ccg.andyt.agdtgeotools.AGDT_Shapefile;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_StaticIO;
import uk.ac.leeds.ccg.andyt.generic.utilities.Generic_Collections;
import uk.ac.leeds.ccg.andyt.agdtcensus.Deprivation_DataHandler;
import uk.ac.leeds.ccg.andyt.agdtcensus.Deprivation_DataRecord;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.core.DW_Environment;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.core.DW_Strings;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.data.postcode.DW_Postcode_Handler;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.io.DW_Files;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.process.DW_AbstractProcessor;

/**
 *
 * @author geoagdt
 */
public abstract class DW_Maps extends AGDT_Maps {

    protected transient DW_Environment env;
    protected DW_Files tDW_Files;
    protected DW_Strings tDW_Strings;
    protected DW_Postcode_Handler tDW_Postcode_Handler;

    //private TreeMap<String, AGDT_Point>[] ONSPDlookups;
    //private TreeMap<String, TreeMap<String, AGDT_Point>> tONSPDlookups;
    private static TreeMap<String, TreeMap<String, TreeMap<String, AGDT_Point>>> tONSPDlookups;

    /**
     * For storing level(s) (OA, LSOA, MSOA, PostcodeSector, PostcodeUnit, ...)
     */
    protected String level;
    protected ArrayList<String> levels;
    protected AGDT_StyleParameters styleParameters;

    protected boolean doDebug;

    public DW_Maps(DW_Environment env) {
        this.env = env;
        tDW_Files = env.getDW_Files();
        tDW_Strings = env.getDW_Strings();
        tDW_Postcode_Handler = env.getDW_Postcode_Handler();
    }

    //public TreeMap<String, AGDT_Point>[] getONSPDlookups() {
    public static TreeMap<String, TreeMap<String, TreeMap<String, AGDT_Point>>> getONSPDlookups(
            DW_Environment env) {
        if (tONSPDlookups == null) {
            initONSPDLookups(env);
        }
        return tONSPDlookups;
    }

    public static void initONSPDLookups(DW_Environment env) {
        tONSPDlookups = new TreeMap<String, TreeMap<String, TreeMap<String, AGDT_Point>>>();
        ArrayList<String> levels;
        levels = new ArrayList<String>();
        levels.add("Unit");
        //levels.add("Sector");
        //levels.add("Area");
        DW_Postcode_Handler tDW_Postcode_Handler;
        tDW_Postcode_Handler = env.getDW_Postcode_Handler();
        TreeMap<String, File> ONSPDFiles;
        ONSPDFiles = tDW_Postcode_Handler.getONSPDFiles();
        Iterator<String> ite2;
        ite2 = levels.iterator();
        while (ite2.hasNext()) {
            String level = ite2.next();
            TreeMap<String, TreeMap<String, AGDT_Point>> tONSPDlookup;
            tONSPDlookup = tDW_Postcode_Handler.getPostcodeUnitPointLookups(
                    true,
                    ONSPDFiles,
                    DW_Postcode_Handler.getDefaultLookupFilename());
            tONSPDlookups.put(level, tONSPDlookup);
        }
    }

    public static DW_Shapefile getPostcodeUnitPoly_DW_Shapefile(
            DW_Environment env,
            ShapefileDataStoreFactory sdsf) {
        DW_Shapefile result;
        // Code Point Boundaries
        TreeSet<String> postcodes;
        postcodes = new TreeSet<String>();
        postcodes.add("ls");
        postcodes.add("hg");
        postcodes.add("yo");
        postcodes.add("wf");
        postcodes.add("bd");
        postcodes.add("hd");
        postcodes.add("hx");
        String polyShapefilename;
        polyShapefilename = "LS_HG_YO_WF_BD_HD_HX_PostcodeUnit.shp";
        String year;
        year = "2012";
        File generatedCodePointDir;
        generatedCodePointDir = new File(
                env.getDW_Files().getGeneratedCodePointDir(),
                year);
        File polyShapefile = new File(
                generatedCodePointDir,
                polyShapefilename);
        polyShapefile.mkdirs();
        polyShapefile = new File(
                polyShapefile,
                polyShapefilename);
        result = getPostcodeUnitPoly_DW_Shapefile(
                sdsf,
                polyShapefile,
                generatedCodePointDir,
                postcodes);
        return result;
    }

    public static DW_Shapefile getPostcodeUnitPoly_DW_Shapefile(
            ShapefileDataStoreFactory sdsf,
            File file,
            File generatedCodePointDir,
            TreeSet<String> postcodes) {
        DW_Shapefile result;
        if (!file.exists()) {
            // Put it together from source...
            TreeSetFeatureCollection tsfc;
            tsfc = new TreeSetFeatureCollection();
            SimpleFeatureType sft = null;
            SimpleFeatureBuilder sfb = null;
            Iterator<String> ite;
            ite = postcodes.iterator();
            while (ite.hasNext()) {
                String postcode;
                postcode = ite.next();
                File aShapefile;
                aShapefile = new File(
                        generatedCodePointDir,
                        postcode + ".shp");
                aShapefile = new File(
                        aShapefile,
                        postcode + ".shp");
                DW_Shapefile aDW_Shapefile;
                aDW_Shapefile = new DW_Shapefile(aShapefile);
                FeatureCollection fc;
                fc = aDW_Shapefile.getFeatureCollection();
                FeatureIterator fi;
                fi = fc.features();
                if (sft == null) {
                    sft = (SimpleFeatureType) fc.getSchema();
                }
                if (sfb == null) {
                    sfb = new SimpleFeatureBuilder(sft);
                }
                while (fi.hasNext()) {
                    SimpleFeature sf;
                    sf = (SimpleFeature) fi.next();
                    tsfc.add(sf);
                }
                //fi.close();
            }
            DW_Shapefile.transact(file, sft, tsfc, sdsf);
        }
        result = new DW_Shapefile(
                file);
        return result;
    }

    /**
     * level = "District", "Sector" or "Area"
     *
     * @param level
     * @return
     */
    public static DW_Shapefile getPostcodePoly_DW_Shapefile(
            DW_Environment env,
            String level) {
        DW_Shapefile result;
        File dir;
        dir = new File(
                env.getDW_Files().getInputPostcodeDir(),
                "BoundaryData");
        dir = new File(
                dir,
                "GBPostcodeAreaSectorDistrict");
        dir = new File(
                dir,
                "GB_Postcodes");
        String name;
        name = "Postal" + level + ".shp";
        dir = new File(
                dir,
                name);
        File file = new File(
                dir,
                name);
        result = new DW_Shapefile(file);
        return result;
    }

    /*
     * Select and create a new shapefile.
     *
     * @param sdsf
     * @param fc
     * @param sft
     * @param codesToSelect
     * @param targetPropertyName
     * @param outputFile
     */
    public static void selectAndCreateNewShapefile(
            ShapefileDataStoreFactory sdsf,
            FeatureCollection fc,
            SimpleFeatureType sft,
            TreeSet<String> codesToSelect,
            //String attributeName, 
            String targetPropertyName,
            File outputFile) {
        // Initialise the collection of new Features
        TreeSetFeatureCollection tsfc;
        tsfc = new TreeSetFeatureCollection();
        // Create SimpleFeatureBuilder
        //FeatureFactory ff = FactoryFinder.getGeometryFactories();
        SimpleFeatureBuilder sfb;
        sfb = new SimpleFeatureBuilder(sft);
        FeatureIterator featureIterator;
        featureIterator = fc.features();
        int id_int = 0;
        while (featureIterator.hasNext()) {
            Feature inputFeature = featureIterator.next();
            Collection<Property> properties;
            properties = inputFeature.getProperties();
            Iterator<Property> itep = properties.iterator();
            while (itep.hasNext()) {
                Property p = itep.next();
                //System.out.println("Property " + p.toString());
                String propertyName = p.getName().toString();
                //System.out.println("PropertyName " + propertyName);
                if (propertyName.equalsIgnoreCase(targetPropertyName)) {
                    //PropertyType propertyType = p.getType();
                    //System.out.println("PropertyType " + propertyType);
                    Object value = p.getValue();
                    //System.out.println("PropertyValue " + value);
                    String valueString = value.toString();
                    if (codesToSelect.contains(valueString)) {
                        if (valueString.trim().equalsIgnoreCase("E02002337")) {
                            int debug = 1;
                        }
                        String id = "" + id_int;
                        addFeatureToFeatureCollection(
                                (SimpleFeature) inputFeature,
                                sfb,
                                tsfc,
                                id);
                        id_int++;
                    } else {
//                        System.out.println(valueString);
                    }
                }
            }
        }
        featureIterator.close();
        DW_Shapefile.transact(outputFile, sft, tsfc, sdsf);
    }

    public static File getOutputImageFile(
            File outputFile,
            String outputType) {
        File result;
        String filename = outputFile.getName();
        String outputImageFilename;
        outputImageFilename = filename.substring(0, filename.length() - 4)
                + "." + outputType;
        result = new File(
                outputFile.getParent(),
                outputImageFilename);
        return result;
    }

//    This is now pulled directly from AGDT_Maps
//    /**
//     * @param dir
//     * @param filenames
//     * @return A Object[] result where: ----------------------------------------
//     * result[0] is an Object[] with the same length as filenames.length where
//     * each element i is the respective TreeMap<String, Integer> returned from
//     * getLevelData(digitalWelfareDir,filenames[i]);
//     * ---------------------------- result[1] is the max of all the maximum
//     * counts.
//     */
//    protected Object[] getLevelData(
//            File dir,
//            String[] filenames,
//            ArrayList<Integer> omit) {
//        Object[] result = new Object[2];
//        int length = filenames.length;
//        Object[] resultPart0 = new Object[length];
//        int max = Integer.MIN_VALUE;
//        for (int i = 0; i < length; i++) {
//            boolean doLevel;
//            doLevel = true;
//            if (omit != null) {
//                if (omit.contains(i)) {
//                    doLevel = false;
//                }
//            }
//            if (doLevel) {
//                Object[] levelData = getLevelData(
//                        dir,
//                        filenames[i]);
//                resultPart0[i] = levelData;
//                max = Math.max(max, (Integer) levelData[1]);
//            }
//        }
//        result[0] = resultPart0;
//        result[1] = max;
//        return result;
//    }
//
//    /**
//     * @param dir
//     * @param filename
//     * @return An Object[] result where: ---------------------------------------
//     * result[0] is a TreeMap<String, Integer> with keys which are DW_Census
//     * Codes and values that are counts;
//     * --------------------------------------------- result[1] is the max count.
//     */
//    protected Object[] getLevelData(
//            File dir,
//            String filename) {
//        Object[] result = new Object[2];
//        TreeMap<String, Integer> map = new TreeMap<String, Integer>();
//        result[0] = map;
//        File file = new File(
//                dir,
//                filename);
//
//        System.out.println("Reading data from file " + file);
//
//        BufferedReader br = Generic_StaticIO.getBufferedReader(file);
//        StreamTokenizer st = new StreamTokenizer(br);
//        Generic_StaticIO.setStreamTokenizerSyntax1(st);
//        try {
//            int token = st.nextToken();
//            //Need skip some header lines
//            st.nextToken();
//            st.nextToken();
////            st.nextToken();
////            st.nextToken();
//            int max = Integer.MIN_VALUE;
//            long RecordID = 0;
//            String line = "";
//            while (!(token == StreamTokenizer.TT_EOF)) {
//                switch (token) {
//                    case StreamTokenizer.TT_EOL:
//                        if (RecordID % 100 == 0) {
//                            System.out.println(line);
//                        }
//                        RecordID++;
//                        break;
//                    case StreamTokenizer.TT_WORD:
//                        line = st.sval;
//                        if (line != null) {
//                        String[] split = line.split(",");
//                        Integer value = new Integer(split[1].trim());
//                        map.put(split[0], value);
//                        max = Math.max(max, value);
//                        }
//                        break;
//                }
//                token = st.nextToken();
//            }
//            result[1] = max;
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//        }
//        return result;
//    }
    /**
     * @param level
     * @param area
     * @return
     */
    protected TreeMap<String, Integer> getPopData(
            String level,
            String area) {
        TreeMap<String, Integer> result;
        result = new TreeMap<String, Integer>();
        File dir = new File(
                env.getDW_Files().getInputCensus2011AttributeDataDir(level),
                area);
        File file = new File(
                dir,
                "pop.csv");
        try {
            BufferedReader br = Generic_StaticIO.getBufferedReader(file);
            StreamTokenizer st = new StreamTokenizer(br);
            Generic_StaticIO.setStreamTokenizerSyntax1(st);
            int token = st.nextToken();
//            //skip header (2 lines)
//            st.nextToken();
//            st.nextToken();
//            st.nextToken();
//            st.nextToken();
//            long RecordID = 0;
            String line = "";
            while (!(token == StreamTokenizer.TT_EOF)) {
                switch (token) {
                    case StreamTokenizer.TT_EOL:
//                        if (RecordID % 100 == 0) {
//                            System.out.println(line);
//                        }
//                        RecordID++;
                        break;
                    case StreamTokenizer.TT_WORD:
                        line = st.sval;
                        String[] split = line.split(",");
                        if (split.length == 2) {
//                            if (!(split[1].equalsIgnoreCase("MSOAIZ") || 
//                                    split[1].equalsIgnoreCase("Middle Super Output Areas and Intermediate Zones"))) {
                            try {
                                String censusArea = split[0];
                                Integer pop = new Integer(split[1]);
                                result.put(censusArea, pop);
//                            } else {
//                                int debug = 1; //Wierdness for Scotland!
//                            }
                            } catch (NumberFormatException e) {
                                // Carry on regardless
                            }
                        } else {
                            int debug = 1; //Sometimes Scotland or other data is missing!
                        }
                        break;
                }
                token = st.nextToken();
            }
            br.close();
        } catch (IOException e) {
            System.err.println(e.getMessage() + "in DW_Maps.getPopData(String=" + level + ",String=" + area + ")");
        }
        return result;
    }

    /**
     * @param level
     * @return File
     */
    protected static File getAreaBoundaryShapefile(
            DW_Environment env,
            String level) {
        File result = null;
        String name;
        if (level.startsWith("Postcode")) {
            if (level.equalsIgnoreCase("PostcodeUnit")) {
                // Get Postcode Unit Boundaries
                DW_Shapefile postcodeUnitPoly_DW_Shapefile;
                postcodeUnitPoly_DW_Shapefile = getPostcodeUnitPoly_DW_Shapefile(
                        env,
                        new ShapefileDataStoreFactory());
                result = postcodeUnitPoly_DW_Shapefile.getFile();
            }
            if (level.equalsIgnoreCase("PostcodeSector")) {
                // Get PostcodeSector Boundaries
                DW_Shapefile postcodeSectorPoly_DW_Shapefile;
                postcodeSectorPoly_DW_Shapefile = getPostcodePoly_DW_Shapefile(
                        env,
                        "Sector");
                result = postcodeSectorPoly_DW_Shapefile.getFile();
            }
            if (level.equalsIgnoreCase("PostcodeDistrict")) {
                // Get PostcodeSector Boundaries
                DW_Shapefile postcodeSectorPoly_DW_Shapefile;
                postcodeSectorPoly_DW_Shapefile = getPostcodePoly_DW_Shapefile(
                        env,
                        "District");
                result = postcodeSectorPoly_DW_Shapefile.getFile();
            }
        } else {
            if (level.equalsIgnoreCase("LAD")) {
                name = "England_lad_2011_gen_clipped.shp";
            } else if (level.equalsIgnoreCase("OA")) {
                name = "England_oa_2011_gen_clipped.shp";
            } else {
                name = level + "_2011_EW_BGC.shp";
            }
            ///scratch02/DigitalWelfare/Input/Census/2011/LSOA/BoundaryData/
            File censusDirectory = env.getDW_Files().getInputCensus2011Dir(level);
            File boundaryDirectory = new File(
                    censusDirectory,
                    "/BoundaryData/");
            File shapefileDirectory = new File(
                    boundaryDirectory,
                    name);
//        File boundaryDirectory = new File(
//                digitalWelfareDir,
//                "/BoundaryData/" + level + "/" + name);
            result = new File(shapefileDirectory, name);
        }
        return result;
    }

    /**
     * @param area
     * @param level
     * @return File
     */
    protected static File getCensusBoundaryShapefile(
            DW_Environment env,
            String area,
            String level) {
        File result;
        String name = area + "_" + level + "_2011_EW_BGC.shp";
        File boundaryDirectory = new File(
                env.getDW_Files().getGeneratedCensus2011Dir(level),
                "/BoundaryData/" + name);
        if (!boundaryDirectory.exists()) {
            boolean dirCreated;
            dirCreated = boundaryDirectory.mkdirs();
            if (!dirCreated) {
                System.err.println("Warning directory " + boundaryDirectory
                        + " not created in " + DW_Maps.class
                        + ".getLeedsCensusBoundaryShapefile(File).");
            }
        }
        result = new File(boundaryDirectory, name);
        return result;
    }

    /**
     * @return File
     */
    protected File getLeedsPostcodeSectorShapefile() {
        File result;
        String fileAndDirName = "LSPostalSector.shp";
        File boundaryDirectory = new File(
                env.getDW_Files().getInputPostcodeDir(),
                "BoundaryData");
        File shapefileDir = new File(
                boundaryDirectory,
                fileAndDirName);
        result = new File(
                shapefileDir,
                fileAndDirName);
        return result;
    }

//    /*
//     * Select and create a new shapefile.
//     *
//     * @param fc
//     * @param sft
//     * @param tLSOACodes
//     * @param tLSOAData
//     * @param attributeName
//     * @param outputFile
//     * @param max
//     * @param filter If filter == true then result is clipped to the LSOA
//     * boundary.
//     */
//    public void selectAndCreateNewShapefile(
//            ShapefileDataStoreFactory sdsf,
//            FeatureCollection fc,
//            SimpleFeatureType sft,
//            TreeSet<String> censusCodes,
//            TreeMap<String, Integer> levelData,
//            //String attributeName, 
//            String targetPropertyName,
//            File outputFile,
//            int filter,
//            TreeMap<Integer, Integer> deprivationClasses,
//            TreeMap<String, Deprivation_DataRecord> deprivationDataRecords,
//            Integer deprivationClass) {
//        //        summariseAttributes(sft);
//        // Initialise the collection of new Features
//        TreeSetFeatureCollection tsfc;
//        tsfc = new TreeSetFeatureCollection();
//        // Create SimpleFeatureBuilder
//        //FeatureFactory ff = FactoryFinder.getGeometryFactories();
//        SimpleFeatureBuilder sfb;
//        sfb = new SimpleFeatureBuilder(sft);
//        Set<String> keySet = levelData.keySet();
//        FeatureIterator featureIterator;
//        featureIterator = fc.features();
//        int id_int = 0;
//        while (featureIterator.hasNext()) {
//            Feature inputFeature = featureIterator.next();
//            Collection<Property> properties;
//            properties = inputFeature.getProperties();
//            Iterator<Property> itep = properties.iterator();
//            while (itep.hasNext()) {
//                Property p = itep.next();
//                //System.out.println("Property " + p.toString());
//                String propertyName = p.getName().toString();
//                //System.out.println("PropertyName " + propertyName);
//                if (propertyName.equalsIgnoreCase(targetPropertyName)) {
//                    //PropertyType propertyType = p.getType();
//                    //System.out.println("PropertyType " + propertyType);
//                    Object value = p.getValue();
//                    //System.out.println("PropertyValue " + value);
//                    String valueString = value.toString();
//                    Deprivation_DataRecord aDeprivation_DataRecord;
//                    aDeprivation_DataRecord = deprivationDataRecords.get(valueString);
//                    // aDeprivation_DataRecord might be null as deprivation data comes from 2001 census...
//                    if (aDeprivation_DataRecord != null) {
//                        Integer thisDeprivationClass;
//                        thisDeprivationClass = Deprivation_DataHandler.getDeprivationClass(
//                                deprivationClasses,
//                                aDeprivation_DataRecord);
//                        if (thisDeprivationClass == deprivationClass.intValue()) {
//                            if (filter < 3) {
//                                if (censusCodes.contains(valueString)) {
//                                    Integer clientCount = levelData.get(valueString);
//                                    //                            if (clientCount == null) {
//                                    //                                clientCount = 0;
//                                    //                            }
//                                    if (clientCount != null) {
//                                        String id = "" + id_int;
//                                        addFeatureAttributeAndAddToFeatureCollection(
//                                                (SimpleFeature) inputFeature, sfb, clientCount, tsfc, id);
//                                        id_int++;
//                                    }
//                                }
//                            } else {
//                                if (keySet.contains(valueString) || censusCodes.contains(valueString)) {
//                                    Integer clientCount = levelData.get(valueString);
//                                    //                            if (clientCount == null) {
//                                    //                                clientCount = 0;
//                                    //                            }
//                                    if (clientCount != null) {
//                                        String id = "" + id_int;
//                                        addFeatureAttributeAndAddToFeatureCollection(
//                                                (SimpleFeature) inputFeature, sfb, clientCount, tsfc, id);
//                                        id_int++;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        featureIterator.close();
//        DW_Shapefile.transact(outputFile, sft, tsfc, sdsf);
//    }

    /*
     * Select and create a new shapefile.
     *
     * @param fc
     * @param sft
     * @param tLSOACodes
     * @param tLSOAData
     * @param attributeName
     * @param outputFile
     * @param max
     * @param filter If filter == true then result is clipped to the LSOA
     * boundary.
     */
    /**
     *
     * @param sdsf
     * @param fc
     * @param sft
     * @param areaCodes For filtering.
     * @param levelData Keys are areaCodes.
     * @param attributeName Expected to be either "Count", "Density" or "Rate"
     * @param targetPropertyName
     * @param population Can be null. Keys are areaCodes.
     * @param populationMultiplier For multiplying count by before dividing by
     * population
     * @param outputFile
     * @param filter If filter &lt; 3 only those areas in areaCodes are mapped.
     * Otherwise additional areas for areaCodes in levelData are also mapped.
     * @param deprivationClasses
     * @param deprivationDataRecords
     * @param deprivationClass
     * @return
     */
    public TreeMap<Integer, Integer> selectAndCreateNewShapefile(
            ShapefileDataStoreFactory sdsf,
            FeatureCollection fc,
            SimpleFeatureType sft,
            TreeSet<String> areaCodes,
            TreeMap<String, Integer> levelData,
            String attributeName,
            String targetPropertyName,
            TreeMap<String, Integer> population,
            double populationMultiplier,
            File outputFile,
            int filter,
            TreeMap<Integer, Integer> deprivationClasses,
            TreeMap<String, Deprivation_DataRecord> deprivationDataRecords,
            Integer deprivationClass,
            boolean countClientsInAndOutOfRegion) {
        TreeMap<Integer, Integer> inAndOutOfRegionCount = null;
        //        summariseAttributes(sft);
        // Initialise the collection of new Features
        TreeSetFeatureCollection tsfc;
        tsfc = new TreeSetFeatureCollection();
        // Create SimpleFeatureBuilder
        //FeatureFactory ff = FactoryFinder.getGeometryFactories();
        SimpleFeatureBuilder sfb;
        sfb = new SimpleFeatureBuilder(sft);
        if (countClientsInAndOutOfRegion) {
            inAndOutOfRegionCount = new TreeMap<Integer, Integer>();
            inAndOutOfRegionCount.put(0, 0);
            inAndOutOfRegionCount.put(1, 0);
        }
        Set<String> keySet = levelData.keySet();
        FeatureIterator featureIterator;
        featureIterator = fc.features();
        int id_int = 0;
        while (featureIterator.hasNext()) {
            Feature inputFeature = featureIterator.next();
            SimpleFeature sf;
            Collection<Property> properties;
            properties = inputFeature.getProperties();
            Iterator<Property> itep;
            // get Area
            double area = 0;
            itep = properties.iterator();
            while (itep.hasNext()) {
                Property p = itep.next();
                //System.out.println("Property " + p.toString());
                String propertyName = p.getName().toString();
                //System.out.println("PropertyName " + propertyName);
//                PropertyDescriptor pd;
//                pd = p.getDescriptor();
                if (propertyName.equalsIgnoreCase("the_geom")) {
                    Geometry g;
                    g = (Geometry) p.getValue();
                    area = g.getArea();
                    try {
                        Polygon poly;
                        poly = (Polygon) g;
                        area = poly.getArea();
                    } catch (ClassCastException e) {
                        int debug = 1;
                    }
                    try {
                        MultiPolygon multipoly;
                        multipoly = (MultiPolygon) g;
                        area = multipoly.getArea();
                    } catch (ClassCastException e) {
                        int debug = 1;
                    }
                }
            }
            itep = properties.iterator();
            while (itep.hasNext()) {
                Property p = itep.next();
                //System.out.println("Property " + p.toString());
                String propertyName = p.getName().toString();
                //System.out.println("PropertyName " + propertyName);
                if (propertyName.equalsIgnoreCase(targetPropertyName)) {
                    //PropertyType propertyType = p.getType();
                    //System.out.println("PropertyType " + propertyType);
                    Object value = p.getValue();
                    //System.out.println("PropertyValue " + value);
                    String valueString = value.toString();
                    if (deprivationDataRecords != null) {
                        Deprivation_DataRecord aDeprivation_DataRecord;
                        aDeprivation_DataRecord = deprivationDataRecords.get(valueString);
                        // aDeprivation_DataRecord might be null as deprivation data comes from 2001 census...
                        if (aDeprivation_DataRecord != null) {
                            Integer thisDeprivationClass;
                            thisDeprivationClass = Deprivation_DataHandler.getDeprivationClass(
                                    deprivationClasses,
                                    aDeprivation_DataRecord);
                            if (thisDeprivationClass == deprivationClass.intValue()) {
                                sf = (SimpleFeature) inputFeature;
                                id_int = calculate0(
                                        filter,
                                        areaCodes,
                                        levelData,
                                        keySet,
                                        attributeName,
                                        sf,
                                        sfb,
                                        tsfc,
                                        id_int,
                                        area,
                                        population,
                                        valueString,
                                        populationMultiplier,
                                        countClientsInAndOutOfRegion,
                                        inAndOutOfRegionCount);
                            }
                        } else {
                            int debug = 1;
                        }
                    } else {
                        sf = (SimpleFeature) inputFeature;
                        id_int = calculate0(
                                filter,
                                areaCodes,
                                levelData,
                                keySet,
                                attributeName,
                                sf,
                                sfb,
                                tsfc,
                                id_int,
                                area,
                                population,
                                valueString,
                                populationMultiplier,
                                countClientsInAndOutOfRegion,
                                inAndOutOfRegionCount);
                    }
                }
            }
        }
        featureIterator.close();
        DW_Shapefile.transact(outputFile, sft, tsfc, sdsf);
        return inAndOutOfRegionCount;
    }

    /**
     *
     * @param filter
     * @param areaCodes
     * @param levelData
     * @param levelDataKeySet
     * @param attributeName
     * @param sf
     * @param sfb
     * @param tsfc
     * @param id_int
     * @param area
     * @param population
     * @param areaCode
     * @param populationMultiplier
     * @param countClientsInAndOutOfRegion
     * @param inAndOutOfRegionCount
     */
    private int calculate0(
            int filter,
            TreeSet<String> areaCodes,
            TreeMap<String, Integer> levelData,
            Set<String> levelDataKeySet,
            String attributeName,
            SimpleFeature sf,
            SimpleFeatureBuilder sfb,
            TreeSetFeatureCollection tsfc,
            int id_int,
            double area,
            TreeMap<String, Integer> population,
            String areaCode,
            double populationMultiplier,
            boolean countClientsInAndOutOfRegion,
            TreeMap<Integer, Integer> inAndOutOfRegionCount) {
        Integer clientCount = levelData.get(areaCode);
        if (clientCount == null) {
            clientCount = 0;
        }

//        if (areaCode.equalsIgnoreCase("LS7")) {
//            if (doDebug) {
//                int debug = 1;
//            }
//        }
        //if (clientCount != null) {
        if (filter < 3) {
            if (areaCodes.contains(areaCode)) {
                id_int = calculate(
                        clientCount,
                        attributeName,
                        sf,
                        sfb,
                        tsfc,
                        id_int,
                        area,
                        population,
                        areaCode,
                        populationMultiplier,
                        countClientsInAndOutOfRegion,
                        inAndOutOfRegionCount);
            } else if (countClientsInAndOutOfRegion) {
                Generic_Collections.addToTreeMapIntegerInteger(
                        inAndOutOfRegionCount, 0, clientCount);
            } //DEBUG
        } else if (levelDataKeySet.contains(areaCode) || areaCodes.contains(areaCode)) {
            id_int = calculate(
                    clientCount,
                    attributeName,
                    sf,
                    sfb,
                    tsfc,
                    id_int,
                    area,
                    population,
                    areaCode,
                    populationMultiplier,
                    countClientsInAndOutOfRegion,
                    inAndOutOfRegionCount);
        } else if (countClientsInAndOutOfRegion) {
            Generic_Collections.addToTreeMapIntegerInteger(
                    inAndOutOfRegionCount, 0, clientCount);
        }
        //}
        return id_int;
    }

    /**
     *
     * @param clientCount
     * @param attributeName
     * @param inputFeature
     * @param sfb
     * @param tsfc
     * @param id_int
     * @param area
     * @param population
     * @param areaCode
     * @param populationMultiplier
     * @param countClientsInAndOutOfRegion
     * @param inAndOutOfRegionCount
     * @return
     */
    private int calculate(
            Integer clientCount,
            String attributeName,
            SimpleFeature inputFeature,
            SimpleFeatureBuilder sfb,
            TreeSetFeatureCollection tsfc,
            int id_int,
            double area,
            TreeMap<String, Integer> population,
            String areaCode,
            double populationMultiplier,
            boolean countClientsInAndOutOfRegion,
            TreeMap<Integer, Integer> inAndOutOfRegionCount) {
        String id = "" + id_int;
        if (attributeName.equalsIgnoreCase("Count")) {
//            if (id_int == 0) {
//                System.out.println("id, areaCode, clientCount");
//            }
            addFeatureAttributeAndAddToFeatureCollection(
                    (SimpleFeature) inputFeature, sfb, clientCount, tsfc, id);
//            System.out.println("" + id_int + ", " + areaCode + ", " + clientCount);
        }
        if (attributeName.equalsIgnoreCase("Density")) {
//            if (id_int == 0) {
//                System.out.println("id, areaCode, clientCount, area, density");
//            }
            double densityValue;
            densityValue = ((double) clientCount / area) * 1000000.0D; // * 1000000 for 100 hectares
            addFeatureAttributeAndAddToFeatureCollection(
                    (SimpleFeature) inputFeature, sfb, densityValue, tsfc, id);
//            if (densityValue < 0) {
//                int debug = 1;
//            }
//            System.out.println("" + id_int + ", " + areaCode + ", " + clientCount + ", " + area + ", " + densityValue);
        }
        if (attributeName.equalsIgnoreCase("Rate")) {
//            if (id_int == 0) {
//                System.out.println("id, areaCode, clientCount, area, pop");
//            }
            double rate;
            Integer pop = population.get(areaCode);
            rate = ((double) clientCount / (double) pop) * populationMultiplier;
            addFeatureAttributeAndAddToFeatureCollection(
                    (SimpleFeature) inputFeature, sfb, rate, tsfc, id);
//            System.out.println("" + id_int + ", " + areaCode + ", " + clientCount + ", " + area + ", " + pop);
        }
        if (countClientsInAndOutOfRegion) {
            Generic_Collections.addToTreeMapIntegerInteger(
                    inAndOutOfRegionCount,
                    1,
                    clientCount);
        }
        id_int++;
        return id_int;
    }

    @Override
    public String getOutName(String filename, String attributeName, int filter) {
        String result = filename.substring(0, filename.length() - 4);
        result += attributeName;
        if (filter == 0) {
            result += "ClippedToLeedsLAD";
        }
        if (filter == 1) {
            result += "ClippedToLeedsAndNeighbouringLADs";
        }
        if (filter == 2) {
            result += "ClippedToLeedsAndNearNeighbouringLADs";
        }
        if (filter == 3) {
            result += "";
        }
        return result;
    }

    public TreeSetFeatureCollection getAdviceLeedsPointFeatureCollection(
            SimpleFeatureType sft) {
        TreeSetFeatureCollection result;
        TreeMap<String, AGDT_Point> tAdviceLeedsNamesAndPoints;
        tAdviceLeedsNamesAndPoints = DW_AbstractProcessor.getAdviceLeedsNamesAndPoints(env);
        TreeMap<String, AGDT_Point> map = tAdviceLeedsNamesAndPoints;
        /*
         * GeometryFactory will be used to create the geometry attribute of each feature,
         * using a Point object for the location.
         */
        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory();
        SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(sft);
        result = new TreeSetFeatureCollection();
        Iterator<String> ite = map.keySet().iterator();
        while (ite.hasNext()) {
            String outlet = ite.next();
            String name = outlet;
            AGDT_Point p = map.get(outlet);
            addPointFeature(p, gf, sfb, name, result);
        }
        return result;
    }

    public TreeSetFeatureCollection getAdviceLeedsPointFeatureCollection(
            SimpleFeatureType sft,
            String outletTarget) {
        TreeSetFeatureCollection result;
        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory();
        SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(sft);
        result = getAdviceLeedsPointFeatureCollection(
                sft,
                outletTarget,
                gf,
                sfb);
        return result;
    }

    public TreeSetFeatureCollection getAdviceLeedsPointFeatureCollection(
            SimpleFeatureType sft,
            String outletTarget,
            GeometryFactory gf,
            SimpleFeatureBuilder sfb) {
        TreeSetFeatureCollection result;
        result = new TreeSetFeatureCollection();
        TreeMap<String, AGDT_Point> tAdviceLeedsNamesAndPoints;
        tAdviceLeedsNamesAndPoints = DW_AbstractProcessor.getAdviceLeedsNamesAndPoints(env);
        TreeMap<String, AGDT_Point> map = tAdviceLeedsNamesAndPoints;
        Iterator<String> ite = map.keySet().iterator();
        while (ite.hasNext()) {
            String outlet = ite.next();
            if (outlet.equalsIgnoreCase(outletTarget)) {
                AGDT_Point p = map.get(outlet);
                String name = outlet;
                addPointFeature(p, gf, sfb, name, result);
            }
        }
        return result;
    }

    public File createAdviceLeedsPointShapefileIfItDoesNotExist(
            File dir,
            String shapefileFilename) {
        File result;
        SimpleFeatureType sft;
        sft = getPointSimpleFeatureType(getDefaultSRID());
        TreeSetFeatureCollection fc;
        fc = getAdviceLeedsPointFeatureCollection(sft);
        result = createShapefileIfItDoesNotExist(
                dir,
                shapefileFilename,
                fc,
                sft);
        return result;
    }

    /**
     * @param dir
     * @param shapefileFilename
     * @param outletTarget
     * @return
     */
    public File createAdviceLeedsPointShapefileIfItDoesNotExist(
            File dir,
            String shapefileFilename,
            String outletTarget) {
        File result;
        SimpleFeatureType sft;
        sft = getPointSimpleFeatureType(getDefaultSRID());
        TreeSetFeatureCollection fc;
        fc = getAdviceLeedsPointFeatureCollection(
                sft, outletTarget);
        result = createShapefileIfItDoesNotExist(
                dir,
                shapefileFilename,
                fc,
                sft);
        return result;
    }

    /**
     * @return
     */
    public DW_Shapefile getAdviceLeedsPointDW_Shapefile() {
        String tAdviceLeedsPointName = "AllAdviceLeedsPoints";
        File tAdviceLeedsPointShapefileDir;
        tAdviceLeedsPointShapefileDir = new File(
                env.getDW_Files().getGeneratedAdviceLeedsDir(),
                tAdviceLeedsPointName);
        String tAdviceLeedsPointShapefileFilename;
        tAdviceLeedsPointShapefileFilename = tAdviceLeedsPointName + ".shp";
        File tAdviceLeedsPointShapefile;
        tAdviceLeedsPointShapefile = createAdviceLeedsPointShapefileIfItDoesNotExist(
                tAdviceLeedsPointShapefileDir,
                tAdviceLeedsPointShapefileFilename);
        return new DW_Shapefile(tAdviceLeedsPointShapefile);
    }

    /**
     * @return
     */
    public ArrayList<AGDT_Shapefile> getAdviceLeedsPointDW_Shapefiles() {
        ArrayList<AGDT_Shapefile> result;
        result = new ArrayList<AGDT_Shapefile>();
        ArrayList<String> tAdviceLeedsServiceNames;
        tAdviceLeedsServiceNames = DW_AbstractProcessor.getAdviceLeedsServiceNames();
        DW_Files tDW_Files;
        tDW_Files = env.getDW_Files();
        Iterator<String> ite;
        ite = tAdviceLeedsServiceNames.iterator();
        while (ite.hasNext()) {
            String tAdviceLeedsPointName = ite.next();
            File tAdviceLeedsPointShapefileDir;
            tAdviceLeedsPointShapefileDir = new File(
                    tDW_Files.getGeneratedAdviceLeedsDir(),
                    tAdviceLeedsPointName);
            String tAdviceLeedsPointShapefileFilename;
            tAdviceLeedsPointShapefileFilename = tAdviceLeedsPointName + ".shp";
            File tAdviceLeedsPointShapefile;
            tAdviceLeedsPointShapefile = createAdviceLeedsPointShapefileIfItDoesNotExist(
                    tAdviceLeedsPointShapefileDir,
                    tAdviceLeedsPointShapefileFilename,
                    tAdviceLeedsPointName);
            result.add(new AGDT_Shapefile(tAdviceLeedsPointShapefile));
        }
        return result;
    }

    public DW_Shapefile getCommunityAreasDW_Shapefile() {
        DW_Shapefile result;
        String name = "communityareas_region.shp";
        File dir = new File(
                env.getDW_Files().getInputDir(),
                "CommunityAreas");
        dir = new File(dir, name);
        File f;
        f = new File(
                dir,
                name);
        result = new DW_Shapefile(f);
        return result;
    }
}
