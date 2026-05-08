package fr.coriolis.checker.tables;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import fr.coriolis.checker.specs.SpecIO;
import fr.coriolis.checker.utils.NetUtils;

public final class ArgoNVSReferenceTable {

	public enum RELEVANT_TABLES {
		DATA_TYPE("DATA_TYPE", "R01"), DM_QC_FLAG("DM_QC_FLAG", "RD2"), PLATFORM_TYPE("PLATFORM_TYPE", "R23"),
		PLATFORM_MAKER("PLATFORM_MAKER", "R24"), PROF_QC_FLAG("PROF_QC_FLAG", "RP2"),
		POSITION_ACCURACY("POSITION_ACCURACY", "R05"), DATA_STATE_INDICATOR("DATA_STATE_INDICATOR", "R06"),
		ARGO_WMO_INST_TYPE("ARGO_WMO_INST_TYPE", "R08"), POSITIONING_SYSTEM("POSITIONING_SYSTEM", "R09"),
		TRANS_SYSTEM("TRANS_SYSTEM", "R10"), VERTICAL_SAMPLING_SCHEME("VERTICAL_SAMPLING_SCHEME", "R16"),
		STATUS("STATUS", "R19"), GROUNDED("GROUNDED", "R20"), PLATFORM_FAMILY("PLATFORM_FAMILY", "R22"),
		SENSOR("SENSOR", "R25"), SENSOR_MAKER("SENSOR_MAKER", "R26"), SENSOR_MODEL("SENSOR_MODEL", "R27"),
		MEASUREMENT_CODE_ID("MEASUREMENT_CODE_ID", "R15"), TECHNICAL_PARAMETER_NAME("TECHNICAL_PARAMETER_NAME", "R14"),
		CONFIG_PARAMETER_NAME("CONFIG_PARAMETER_NAME", "R18"), PARAMETER("PARAMETER", "R03"),
		PROGRAM_NAME("PROGRAM_NAME", "R41"), BATTERY_MAKER("BATTERY_MAKER", "R33"), BATTERY_TYPE("BATTERY_TYPE", "R34"),
		BATTERY_SIZE("BATTERY_SIZE", "R35"), PI_NAME("PI_NAME", "R40");

		public final String name;
		public final String code;

		RELEVANT_TABLES(String name, String code) {
			this.name = name;
			this.code = code;
		}

		public static RELEVANT_TABLES fromName(String name) {
			for (RELEVANT_TABLES t : RELEVANT_TABLES.values()) {
				if (t.name.equals(name)) {
					return t;
				}
			}
			return null;
		}

		public String getCode() {
			return code;
		}
	};

	// ==========
	// ALL TABLES
	// ==========
	private SkosCollection DATA_TYPE_TABLE;
	private SkosCollection DM_QC_FLAG_TABLE;
	private SkosCollection PLATFORM_TYPE_TABLE;
	private SkosCollection PLATFORM_MAKER_TABLE;
	private SkosCollection PROF_QC_FLAG_TABLE;
	private SkosCollection POSITION_ACCURACY_TABLE;
	private SkosCollection DATA_STATE_INDICATOR_TABLE;
	private SkosCollection ARGO_WMO_INST_TYPE_TABLE;
	private SkosCollection POSITIONING_SYSTEM_TABLE;
	private SkosCollection TRANS_SYSTEM_TABLE;
	private SkosCollection VERTICAL_SAMPLING_SCHEME_TABLE;
	private SkosCollection STATUS_TABLE;
	private SkosCollection GROUNDED_TABLE;
	private SkosCollection PLATFORM_FAMILY_TABLE;
	private SkosCollection SENSOR_TABLE;
	private SkosCollection SENSOR_MAKER_TABLE;
	private SkosCollection SENSOR_MODEL_TABLE;
	private SkosCollection MEASUREMENT_CODE_ID_TABLE;
	private SkosCollection TECHNICAL_PARAMETER_NAME_TABLE;
	private SkosCollection CONFIG_PARAMETER_NAME_TABLE;
	private SkosCollection PARAMETER_TABLE;
	private SkosCollection PROGRAM_NAME_TABLE;
	private SkosCollection BATTERY_MAKER_TABLE;
	private SkosCollection BATTERY_TYPE_TABLE;
	private SkosCollection BATTERY_SIZE_TABLE;
	private SkosCollection PI_NAME_TABLE;

  public SkosCollection getDATA_TYPE_TABLE() {
    return DATA_TYPE_TABLE;
  }

  public SkosCollection getDM_QC_FLAG_TABLE() {
    return DM_QC_FLAG_TABLE;
  }

  public SkosCollection getPLATFORM_TYPE_TABLE() {
    return PLATFORM_TYPE_TABLE;
  }

  public SkosCollection getPLATFORM_MAKER_TABLE() {
    return PLATFORM_MAKER_TABLE;
  }

  public SkosCollection getPROF_QC_FLAG_TABLE() {
    return PROF_QC_FLAG_TABLE;
  }

  public SkosCollection getPOSITION_ACCURACY_TABLE() {
    return POSITION_ACCURACY_TABLE;
  }

  public SkosCollection getDATA_STATE_INDICATOR_TABLE() {
    return DATA_STATE_INDICATOR_TABLE;
  }

  public SkosCollection getARGO_WMO_INST_TYPE_TABLE() {
    return ARGO_WMO_INST_TYPE_TABLE;
  }

  public SkosCollection getPOSITIONING_SYSTEM_TABLE() {
    return POSITIONING_SYSTEM_TABLE;
  }

  public SkosCollection getTRANS_SYSTEM_TABLE() {
    return TRANS_SYSTEM_TABLE;
  }

  public SkosCollection getVERTICAL_SAMPLING_SCHEME_TABLE() {
    return VERTICAL_SAMPLING_SCHEME_TABLE;
  }

  public SkosCollection getSTATUS_TABLE() {
    return STATUS_TABLE;
  }

  public SkosCollection getGROUNDED_TABLE() {
    return GROUNDED_TABLE;
  }

  public SkosCollection getPLATFORM_FAMILY_TABLE() {
    return PLATFORM_FAMILY_TABLE;
  }

  public SkosCollection getSENSOR_TABLE() {
    return SENSOR_TABLE;
  }

  public SkosCollection getSENSOR_MAKER_TABLE() {
    return SENSOR_MAKER_TABLE;
  }

  public SkosCollection getSENSOR_MODEL_TABLE() {
    return SENSOR_MODEL_TABLE;
  }

  public SkosCollection getMEASUREMENT_CODE_ID_TABLE() {
    return MEASUREMENT_CODE_ID_TABLE;
  }

  public SkosCollection getTECHNICAL_PARAMETER_NAME_TABLE() {
    return TECHNICAL_PARAMETER_NAME_TABLE;
  }

  public SkosCollection getCONFIG_PARAMETER_NAME_TABLE() {
    return CONFIG_PARAMETER_NAME_TABLE;
  }

  public SkosCollection getPARAMETER_TABLE() {
    return PARAMETER_TABLE;
  }

  public SkosCollection getPROGRAM_NAME_TABLE() {
    return PROGRAM_NAME_TABLE;
  }

  public SkosCollection getBATTERY_MAKER_TABLE() {
    return BATTERY_MAKER_TABLE;
  }

  public SkosCollection getBATTERY_TYPE_TABLE() {
    return BATTERY_TYPE_TABLE;
  }

  public SkosCollection getBATTERY_SIZE_TABLE() {
    return BATTERY_SIZE_TABLE;
  }

  public SkosCollection getPI_NAME_TABLE() {
    return PI_NAME_TABLE;
  }

  // ====
	// INIT
	// ====
	/**
	 * Initialize NVS references tables (static variables) : loop over all files in
	 * the spec folder (from SpecIO) and instanciate a SkosCollection if file is a
	 * NVS jsonld table. Then populate all static variable of the Argo netcdf files
	 * checkers 's useful tables.
	 * 
	 *
	 */
	public static ArgoNVSReferenceTable initialize(boolean useInternalSpecs, Path specDir) {
		// MAp to store the tables
		Map<RELEVANT_TABLES, SkosCollection> nvsReferenceTables = new HashMap<>();

		// loop over relevant table list
		for (RELEVANT_TABLES t : RELEVANT_TABLES.values()) {
			String fileRableName = "NVS/" + t.getCode() + ".jsonld";
			try (InputStream tableInputStream = SpecIO.open(useInternalSpecs, specDir, fileRableName)) {
				processNVSTableFile(nvsReferenceTables, tableInputStream);
			} catch (FileNotFoundException e) {
        System.err.println("Table file not found : " + fileRableName + " (" + e.getMessage() + ")");
				break;
			} catch (IOException e) {
        System.err.println("Failed to parse table file: " + fileRableName + " (" + e.getMessage() + ")");
				break;
			}
		}

		return new ArgoNVSReferenceTable(nvsReferenceTables);
	}

	/**
	 * Initialize the NVS tables from the nerc server on internet.
	 * 
	 */

	public static ArgoNVSReferenceTable initializeFromInternet(String baseUrl) {
		Map<RELEVANT_TABLES, SkosCollection> nvsReferenceTables = new HashMap<>();

		// Loop through relevant tables list :
		for (RELEVANT_TABLES t : RELEVANT_TABLES.values()) {
			String tableUrl = baseUrl + t.getCode() + "/current/?_profile=nvs&_mediatype=application/ld+json";
			try (InputStream tableInputStream = NetUtils.openInputStream(tableUrl)) {
				processNVSTableFile(nvsReferenceTables, tableInputStream);
			} catch (IOException e) {
        System.err.println("Table file not found on NVS : " + tableUrl + " (" + e.getMessage() + ")");
				break;
			}
		}

		return new ArgoNVSReferenceTable(nvsReferenceTables);

	}

	// ==================
	// CONVENIENT METHODS
	// ==================
	private static void processNVSTableFile(Map<RELEVANT_TABLES, SkosCollection> nvsReferenceTables,
			InputStream tableInput) throws IOException {
		// table parser :
		ArgoNVSReferenceTableParser nvsTablesParser = new ArgoNVSReferenceTableParser();

		SkosCollection table;

		// parse table :
		table = nvsTablesParser.getCollection(tableInput);

		// is it a relevant table ?
		RELEVANT_TABLES enumKey = RELEVANT_TABLES.fromName(table.getAltLabel());
		if (enumKey != null) {
			nvsReferenceTables.put(enumKey, table);
		}
	}

	private ArgoNVSReferenceTable(Map<RELEVANT_TABLES, SkosCollection> nvsReferenceTables) {
		DATA_TYPE_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.DATA_TYPE);
		DM_QC_FLAG_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.DM_QC_FLAG);
		PLATFORM_TYPE_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.PLATFORM_TYPE);
		PLATFORM_MAKER_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.PLATFORM_MAKER);
		PROF_QC_FLAG_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.PROF_QC_FLAG);
		POSITION_ACCURACY_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.POSITION_ACCURACY);
		DATA_STATE_INDICATOR_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.DATA_STATE_INDICATOR);
		ARGO_WMO_INST_TYPE_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.ARGO_WMO_INST_TYPE);
		POSITIONING_SYSTEM_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.POSITIONING_SYSTEM);
		TRANS_SYSTEM_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.TRANS_SYSTEM);
		VERTICAL_SAMPLING_SCHEME_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.VERTICAL_SAMPLING_SCHEME);
		STATUS_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.STATUS);
		GROUNDED_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.GROUNDED);
		PLATFORM_FAMILY_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.PLATFORM_FAMILY);
		SENSOR_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.SENSOR);
		SENSOR_MAKER_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.SENSOR_MAKER);
		SENSOR_MODEL_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.SENSOR_MODEL);
		MEASUREMENT_CODE_ID_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.MEASUREMENT_CODE_ID);
		TECHNICAL_PARAMETER_NAME_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.TECHNICAL_PARAMETER_NAME);
		CONFIG_PARAMETER_NAME_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.CONFIG_PARAMETER_NAME);
		PARAMETER_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.PARAMETER);
		PROGRAM_NAME_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.PROGRAM_NAME);
		BATTERY_MAKER_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.BATTERY_MAKER);
		BATTERY_TYPE_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.BATTERY_TYPE);
		BATTERY_SIZE_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.BATTERY_SIZE);
		PI_NAME_TABLE = nvsReferenceTables.get(RELEVANT_TABLES.PI_NAME);
	}

}
