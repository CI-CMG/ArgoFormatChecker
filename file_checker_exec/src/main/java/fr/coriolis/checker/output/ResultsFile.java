package fr.coriolis.checker.output;

import edu.colorado.cires.argonaut.xml.filecheck.Errors;
import edu.colorado.cires.argonaut.xml.filecheck.FileCheckResults;
import edu.colorado.cires.argonaut.xml.filecheck.Metadata;
import edu.colorado.cires.argonaut.xml.filecheck.Warnings;
import fr.coriolis.checker.validators.ArgoFileValidator;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import fr.coriolis.checker.core.ArgoDataFile;
import fr.coriolis.checker.core.ArgoDataFile.FileType;
import fr.coriolis.checker.core.ValidationResult;
import fr.coriolis.checker.specs.ArgoDate;
import javax.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultsFile implements Closeable {

	// ..object variables

	private final PrintWriter out;
	private final boolean doXml;
  private final FileCheckResults xml;

	// ArgoDataFile argo = null;

	// ..class variables
	private final static DecimalFormat cycleFmt = new DecimalFormat("000");
	private final static DecimalFormat dFmt = new DecimalFormat("####0.0000;-####0.0000");

	private static final Logger log = LoggerFactory.getLogger("ResultsFile");

	// .............................................................
	//
	// constructors
	//
	// .............................................................

	public ResultsFile(boolean doXml, Path resultsFileName, String fcVersion, String spVersion, Path inputFileName)
			throws IOException {
		out = new PrintWriter(Files.newBufferedWriter(resultsFileName));

		this.doXml = doXml;
    xml = new FileCheckResults();
    xml.setFilecheckerVersion(fcVersion);
    xml.setSpecVersion(spVersion);
    xml.setFile(inputFileName.toString());

		if (!doXml) {

			out.println("VERSION-INFO: FileChecker = '" + fcVersion + "' Specification = '" + spVersion + "'");
			out.println("FILE-NAME: " + inputFileName);
			log.debug("...ResultsFile: text file");
		}

	} // ..end contstructor

	// .....................................................................
	//
	// methods
	//
	// .....................................................................

  @Override
	public void close()
			throws IOException {
			if (doXml) {

        try {
          Marshaller marshaller = JAXBContext.newInstance(FileCheckResults.class).createMarshaller();
          marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
          marshaller.marshal(xml, out);
        } catch (JAXBException e) {
          throw new IOException("Unable to write XML", e);
        }

				log.debug("output xml");
			}
			out.close();
	} // ..end close

	public void openError(Exception e) {
    xml.setStatus("ERROR");
    xml.setPhase("OPEN-FILE");

    Errors errors = new Errors();
    xml.setErrors(errors);
    errors.setNumber(1);
    errors.getErrors().add(e.toString());

		if (!doXml) {
			out.println("ERROR: Open exception:\n" + e);
			out.println("PHASE: OPEN-FILE");
		}
	} // ..end openError

  public void oldDModeFile(String dacName, String version) {
    xml.setStatus("FILE-REJECTED");
    xml.setPhase("DMODE-VERSION-CHECK");

    Metadata metadata = new Metadata();
    xml.setMetadata(metadata);
    metadata.setDac(dacName);
    metadata.setDATATYPE("Argo profile");
    metadata.setFORMATVERSION(version);

    Errors errors = new Errors();
    xml.setErrors(errors);
    errors.setNumber(1);
    errors.getErrors().add("D-mode: Version prior to v3.1 is not allowed");

    Warnings warnings = new Warnings();
    xml.setWarnings(warnings);
    warnings.setNumber(0);

		if (!doXml) {
			out.println("STATUS: FILE-REJECTED");
			out.println("PHASE: DMODE-VERSION-CHECK");
			out.println("META-DATA: start");
			out.println("DAC: " + dacName);
			out.println("TYPE: Argo profile");
			out.println("FORMAT_VERSION: " + version);
			out.println("META-DATA: end");
			out.println("FORMAT-ERRORS: start");
			out.println("D-mode: Pre-v3.1 is not allowed");
			out.println("FORMAT-ERRORS: end");
			out.println("FORMAT-WARNINGS: start");
			out.println("FORMAT-WARNINGS: end");
		}
	} // ..end oldDModeFile

  public void notArgoFile(String dacName) {
    xml.setStatus("FILE-REJECTED");
    xml.setPhase("OPEN-FILE");

    Metadata metadata = new Metadata();
    xml.setMetadata(metadata);
    metadata.setDac(dacName);

    Errors errors = new Errors();
    xml.setErrors(errors);
    errors.setNumber(1);
    errors.getErrors().add(ValidationResult.getMessage());

		if (!doXml) {
			out.println("STATUS: FILE-REJECTED");
			out.println("PHASE: OPEN-FILE");
			out.println("META-DATA: start");
			out.println("DAC: " + dacName);
			out.println("META-DATA: end");
			out.println("FORMAT-ERRORS: start");
			out.println(ValidationResult.getMessage());
			out.println("FORMAT-ERRORS: end");
			out.println("FORMAT-WARNINGS: start");
			out.println("FORMAT-WARNINGS: end");
		}
	} // ..end notArgoFile

  public void formatErrorMessage(String phase) {
    xml.setStatus("ERROR");
    xml.setPhase(phase);

    Errors errors = new Errors();
    xml.setErrors(errors);
    errors.setNumber(1);
    errors.getErrors().add("Format check failed. " + ValidationResult.getMessage());

		if (!doXml) {
			out.println("ERROR: Format check failed." + ValidationResult.getMessage());
			out.println("PHASE: " + phase);
		}
	}

  public void dataErrorMessage(String type) {
    xml.setStatus("ERROR");
    xml.setPhase("DATA-VALIDATION");

    Errors errors = new Errors();
    xml.setErrors(errors);
    errors.setNumber(1);
    errors.getErrors().add(type + " validation failed. " + ValidationResult.getMessage());

		if (!doXml) {
			out.println("ERROR: " + type + " validation failed: " + ValidationResult.getMessage());
		}
	}

  public void statusAndPhase(boolean accepted, String phase) {
		final String acc = "FILE-ACCEPTED";
		final String rej = "FILE-REJECTED";

		String status;
		if (accepted) {
			status = acc;
		} else {
			status = rej;
		}

    xml.setStatus(status);
    xml.setPhase(phase);

		if (!doXml) {
			out.println("STATUS: " + status);
			out.println("PHASE: " + phase);
		}
	}

  public void metaData(String dacName, ArgoDataFile argo, boolean formatPassed, boolean doPsalStats) {
		// ...............report meta-data results...............
		// ..status is that open was successful
		// ..- that means identified as Argo netCDF file (DATA_TYPE and FORMAT_VERSION)
		// ..- format may or may not have passed
		// .. - if format did not pass, trying to retrieve the numeric meta-data
		// .. may cause aborts -- i think string types are safe
		// ..try to get as much of the meta-data as exists, but avoid aborts

		String str;
		int i;

    Metadata metadata = new Metadata();
    xml.setMetadata(metadata);
    metadata.setDac(dacName);

		if (!doXml) {
			out.println("META-DATA: start");
			out.println("DAC: " + dacName);
		}
		log.debug("meta-data: dac = '" + dacName + "'");

		// ..it is implied by the code that to get here
		// ..openSuccessful must be true
		switch (argo.fileType()) {
		case METADATA:
			str = "Argo meta-data";
			break;
		case BIO_PROFILE:
			str = "B-Argo profile";
			break;
		case BIO_TRAJECTORY:
			str = "B-Argo trajectory";
			break;
		case PROFILE:
			str = "Argo profile";
			break;
		case TECHNICAL:
			str = "Argo technical data";
			break;
		case TRAJECTORY:
			str = "Argo trajectory";
			break;
		default:
			str = "File type not determined";
			break;
		}

    metadata.setDATATYPE(str);

		if (!doXml) {
			out.println("TYPE: " + str);
		}
		log.debug("meta-data: type = '" + str + "'");

		metaStr(argo, "FORMAT_VERSION", metadata::setFORMATVERSION);
		metaStr(argo, "DATE_UPDATE", metadata::setDATEUPDATE);

		if (argo.fileType() == FileType.PROFILE || argo.fileType() == FileType.BIO_PROFILE) {
			metaStrArray(argo, "DATA_CENTRE", metadata::setDATACENTRE);
			metaStrArray(argo, "PLATFORM_NUMBER", metadata::setPLATFORMNUMBER);
			metaStrArray(argo, "PI_NAME", metadata::setPINAME);
			metaStrArray(argo, "WMO_INST_TYPE", metadata::setWMOINSTTYPE);
			metaStr(argo, "DATA_MODE", metadata::setDATAMODE);
			metaStr(argo, "DIRECTION", metadata::setDIRECTION);

			if (formatPassed) { // ..skip these if format was bad to prevent potential aborts
				i = argo.getDimensionLength("N_PROF");
        metadata.setNPROF(i);
				if (!doXml) {
					out.println("N_PROF: " + i);
				}
				log.debug("n_prof: {}", i);

				i = argo.getDimensionLength("N_LEVELS");
        metadata.setNLEVELS(i);
				if (!doXml) {
					out.println("N_LEVELS: " + i);
				}
				log.debug("n_levels: {}", i);

				metaIntArray(argo, "CYCLE_NUMBER", cycleFmt, metadata::setCYCLENUMBER);
				metaTimeArray(argo, "JULD", metadata::setJULDDtg);
				metaDoubleArray(argo, "LATITUDE", dFmt, metadata::setLATITUDE);
				metaDoubleArray(argo, "LONGITUDE", dFmt, metadata::setLONGITUDE);
				metaStr(argo, "JULD_QC", metadata::setJULDQC);
				metaStr(argo, "POSITION_QC", metadata::setPOSITIONQC);
			}

			metaStr(argo, "PROFILE_TEMP_QC", metadata::setPROFILETEMPQC);
			metaStr(argo, "PROFILE_PSAL_QC", metadata::setPROFILEPSALQC);
			metaStr(argo, "PROFILE_DOXY_QC", metadata::setPROFILEDOXYQC);
			metaStationParameters(argo, metadata);

			if (formatPassed && argo.fileType() == FileType.PROFILE) {
				addMetaPsalStats(argo, metadata);
			}

		} else if (argo.fileType() == FileType.TRAJECTORY || argo.fileType() == FileType.BIO_TRAJECTORY) {
			metaStr(argo, "DATA_CENTRE", metadata::setDATACENTRE);
			metaStr(argo, "PLATFORM_NUMBER", metadata::setPLATFORMNUMBER);
			metaStr(argo, "PI_NAME", metadata::setPINAME);
			metaStr(argo, "WMO_INST_TYPE", metadata::setWMOINSTTYPE);
			metaStr(argo, "DATA_MODE", metadata::setDATAMODE);

			if (formatPassed) { // ..skip these if format was bad to prevent potential aborts
				double l[], min, max;
				l = argo.readDoubleArr("LATITUDE");
				min = 99999.D;
				max = -99999.D;

				for (double dbl : l) {
					if (dbl < 99990.D) {
						if (dbl > max) {
							max = dbl;
						}
						if (dbl < min) {
							min = dbl;
						}
					}
				}
				if (max < -99990.D) {
					max = 99999.D;
				}

        metadata.setMinLatitude(dFmt.format(min));

				if (!doXml) {
					out.println("MIN-LATITUDE:" + dFmt.format(min));
				}
				log.debug("min-latitude: '" + min + "'");

        metadata.setMaxLatitude(dFmt.format(max));

				if (!doXml) {
					out.println("MAX-LATITUDE:" + dFmt.format(max));
				}
				log.debug("max-latitude: '" + max + "'");

				l = argo.readDoubleArr("LONGITUDE");
				min = 99999.D;
				max = -99999.D;

				for (double dbl : l) {
					if (dbl < 99990.D) {
						if (dbl > max) {
							max = dbl;
						}
						if (dbl < min) {
							min = dbl;
						}
					}
				}
				if (max < -99990.D) {
					max = 99999.D;
				}

        metadata.setMinLongitude(dFmt.format(min));

				if (!doXml) {
					out.println("MIN-LONGITUDE:" + dFmt.format(min));
				}
				log.debug("min-longitude: '" + min + "'");

        metadata.setMaxLongitude(dFmt.format(max));

				if (!doXml) {
					out.println("MAX-LONGITUDE:" + dFmt.format(max));
				}
				log.debug("max-longitude: '" + max + "'");

				if (argo.fileType() == FileType.BIO_TRAJECTORY) {
					metaTrajectoryParameters(argo, metadata);
				}
			}

		} else if (argo.fileType() == FileType.METADATA) {
			metaStr(argo, "DATA_CENTRE", metadata::setDATACENTRE);
			metaStr(argo, "PLATFORM_NUMBER", metadata::setPLATFORMNUMBER);
			metaStr(argo, "PI_NAME", metadata::setPINAME);
			metaStr(argo, "WMO_INST_TYPE", metadata::setWMOINSTTYPE);

		} else if (argo.fileType() == FileType.TECHNICAL) {
      metaStr(argo, "DATA_CENTRE", metadata::setDATACENTRE);
      metaStr(argo, "PLATFORM_NUMBER", metadata::setPLATFORMNUMBER);
		}

		if (!doXml) {
			out.println("META-DATA: end");
		}
	} // ..end metaData

  private void addMetaPsalStats(ArgoDataFile argo, Metadata metadata) {
		// ...............report PSAL adjustment statistics...............
		// ..assumes:
		// ..- Argo-open was successful
		// ..- this is a core-profile file (checked)
		// ..- format has been passed

		double[] stats = argo.computePsalAdjStats();

		double[] arr1 = { stats[0] };
		double[] arr2 = { stats[1] };

		metaDoubleValArray(arr1, "psal-adj-mean", dFmt, metadata::setPsalAdjMean);
		metaDoubleValArray(arr2, "psal-adj-sdev", dFmt,  metadata::setPsalAdjSdev);

	}// ..end addMetaPsalStats

	// ************************** errorsAndWarnings ************************

  public void errorsAndWarnings(ArgoFileValidator argoFileValidator) {

    Errors errors = new Errors();
    xml.setErrors(errors);
    errors.setNumber(argoFileValidator.getValidationResult().nFormatErrors());

		if (!doXml) {
			out.println("FORMAT-ERRORS: start");
		}
		log.debug("format errors:" + argoFileValidator.getValidationResult().nFormatErrors());

		for (String err : argoFileValidator.getValidationResult().getErrors()) {
      errors.getErrors().add(err);
			if (!doXml) {
				out.println(err + "\n");
			}
			log.debug(err);
		}

		if (!doXml) {
			out.println("FORMAT-ERRORS: end");
		}
		log.debug("...end errors");

		// ...............report warnings................

    Warnings warnings = new Warnings();
    xml.setWarnings(warnings);
    warnings.setNumber(argoFileValidator.getValidationResult().nFormatWarnings());

		if (!doXml) {
			out.println("FORMAT-WARNINGS: start");
		}
		log.debug("format warnings: " + argoFileValidator.getValidationResult().nFormatWarnings());

		for (String err : argoFileValidator.getValidationResult().getWarnings()) {
      warnings.getWarnings().add(err);
			if (!doXml) {
				out.println(err + "\n");
			}
			log.debug(err);
		}

		if (!doXml) {
			out.println("FORMAT-WARNINGS: end");
		}

		log.debug("...end warnings");
	}// ..end errorsAndWarnings

	// ************************** metaStr **************************

  private void metaStr(ArgoDataFile argo, String var, Consumer<String> setter) {
    metaStr(argo, var, null, setter);
  }

	private void metaStr(ArgoDataFile argo, String var, String fmt, Consumer<String> setter) {
		String str = argo.readString(var);
		if (str == null) {
			str = "null";
		}

		if (!doXml) {
			out.print(var + ": ");
		}

    String value = fmt == null ? str : String.format(fmt, str);
    setter.accept(value.trim());

    if (!doXml) {
      out.println(value);
    }

		log.debug("meta-data: '" + var + "' = '" + str + "' (single string)");
	}// ..end metaStr

	// ************************ metaStrArray ****************************

  private void metaStrArray(ArgoDataFile argo, String var, Consumer<String> setter) {
    metaStrArray(argo, var, null, setter);
  }

	private void metaStrArray(ArgoDataFile argo, String var, String fmt, Consumer<String> setter) {
		String arr[] = argo.readStringArr(var);

		if (!doXml) {
			out.print(var + ":");
		}

    String value = Arrays.stream(arr)
        .map(String::trim)
        .map(val -> fmt == null ? val : String.format(fmt, val))
        .collect(Collectors.joining(","));

    setter.accept(value);

    if (!doXml) {
      out.println(value);
    }

		log.debug("meta-data: '" + var + "' (string array)");
	}

	// ********************* metaIntArray *******************************

	private void metaIntArray(ArgoDataFile argo, String var, DecimalFormat fmt, Consumer<String> setter) {
		int[] arr = argo.readIntArr(var);

		if (!doXml) {
			out.print(var + ":");
		}

    String value = Arrays.stream(arr)
        .mapToObj(val -> fmt == null ? Integer.toString(val) : fmt.format(val))
        .collect(Collectors.joining(","));

    setter.accept(value);

    if (!doXml) {
      out.println(value);
    }

		log.debug("meta-data: '" + var + "'");
	}// ..end metaIntArray

	// ******************** metaDoubleArray ********************************

	private void metaDoubleArray(ArgoDataFile argo, String var, DecimalFormat fmt, Consumer<String> setter) {
    metaDoubleValArray(argo.readDoubleArr(var), var, fmt, setter);
	}

	// ******************** metaDoubleValArray ********************************

	private void metaDoubleValArray(double[] arr, String var, DecimalFormat fmt, Consumer<String> setter) {

    if (!doXml) {
      out.print(var + ":");
    }

    String value = Arrays.stream(arr)
        .mapToObj(val -> fmt == null ? Double.toString(val) : fmt.format(val))
        .collect(Collectors.joining(","));

    setter.accept(value);

    if (!doXml) {
      out.println(value);
    }

    log.debug("meta-data: '" + var + "'");
	}// ..end metaDoubleVal

	// ************************** metaTimeArray *************************

  private void metaTimeArray(ArgoDataFile argo, String var, Consumer<String> setter)  {
    metaTimeArray(argo, var, null, setter);
  }

	private void metaTimeArray(ArgoDataFile argo, String var, String fmt, Consumer<String> setter)  {
		double[] arr = argo.readDoubleArr(var);

		if (!doXml) {
			out.print(var + "-DTG:");
		}

    String value = Arrays.stream(arr)
        .mapToObj(val -> ArgoDate.format(ArgoDate.get(val)))
        .map(val -> fmt == null ? val : String.format(val, val))
        .collect(Collectors.joining(","));

    setter.accept(value);

    if (!doXml) {
      out.println(value);
    }

		log.debug("meta-data: '" + var + "'");
	}// ..end metaTimeArray

	// ************************** metaStationParameters *************************

	private void metaStationParameters(ArgoDataFile argo, Metadata metadata) {
		int n_prof = argo.getDimensionLength("N_PROF");
		int n_param = argo.getDimensionLength("N_PARAM");
		if (n_prof < 0 || n_param < 0) {
			return;
		}

		HashSet<String> set = new HashSet<String>();
		StringBuilder list = null;
		StringBuilder pMode = null;

		for (int i = 0; i < n_prof; i++) {
			String[] str = argo.readStringArr("STATION_PARAMETERS", i);
			String pdm = null;

			if (argo.fileType() == FileType.BIO_PROFILE) {
				pdm = argo.readString("PARAMETER_DATA_MODE", i);
				// log.debug ("*** prof {}: pdm = '{}'", i, pdm);
			}

			for (int j = 0; j < n_param; j++) {
				String s = str[j].trim();

				char p;
				if (pdm == null) {
					p = '-';
				} else {
					p = pdm.charAt(j);
				}

				if (!s.isEmpty()) {
					if (!set.contains(s)) {
						set.add(s);
						if (list == null) {
							list = new StringBuilder(s);
							pMode = new StringBuilder(String.valueOf(p));
							// log.debug("***** init: list = '{}'; pMode = '{}'", list, pMode);
						} else {
							list.append(" " + s);
							pMode.append(p);
							// log.debug("***** add: list = '{}'; pMode = '{}'", list, pMode);
						}
					}
				}
			}
		}

    metadata.setSTATIONPARAMETERS(list == null ? null : list.toString());

		if (!doXml) {
			out.print("STATION_PARAMETERS: ");
		}

		if (list != null && !doXml) {
      out.println(list);
		}

		if (pMode != null) {
      metadata.setPARAMETERDATAMODE(pMode.toString());

			if (!doXml) {
				out.print("PARAMETER_DATA_MODE: ");
        out.println(pMode);
			}
		}

		log.debug("meta-data: 'STATION_PARAMETER' = '" + list + "' (single string)");
	}// ..end metaStationParameters

	// ************************** metaTrajectoryParameters *************************

	private void metaTrajectoryParameters(ArgoDataFile argo, Metadata metadata) {
		int n_param = argo.getDimensionLength("N_PARAM");
		if (n_param < 0) {
			return;
		}

		HashSet<String> set = new HashSet<String>();
		StringBuilder list = null;
		StringBuilder pMode = null;

		String[] str = argo.readStringArr("TRAJECTORY_PARAMETERS");
		String pdm = null;

		for (int j = 0; j < n_param; j++) {
			String s = str[j].trim();

			char p;
			if (pdm == null) {
				p = '-';
			} else {
				p = pdm.charAt(j);
			}

			if (!s.isEmpty()) {
				if (!set.contains(s)) {
					set.add(s);
					if (list == null) {
						list = new StringBuilder(s);
						pMode = new StringBuilder(String.valueOf(p));
						// log.debug("***** init: list = '{}'; pMode = '{}'", list, pMode);
					} else {
						list.append(" " + s);
						pMode.append(p);
						// log.debug("***** add: list = '{}'; pMode = '{}'", list, pMode);
					}
				}
			}
		}

    metadata.setSTATIONPARAMETERS(list == null ? null : list.toString());

		if (!doXml) {
			out.print("STATION_PARAMETERS: ");
		}

		if (list != null && !doXml) {
      out.println(list);
		}

		if (pMode != null) {
      metadata.setPARAMETERDATAMODE(pMode.toString());
			if (!doXml) {
				out.print("PARAMETER_DATA_MODE: ");
        out.println(pMode);
			}
		}

		log.debug("meta-data: 'TRAJECTORY_PARAMETER' = '" + list + "' (single string)");
	}// ..end metaTrajectoryParameters

	// ************************** convenience methods ******************************
	private static String scrubInvalidXmlChar(String input) {
		if (input == null) {
			return null;
		}

		StringBuilder out = new StringBuilder();

		input.codePoints().forEach(cp -> {
			if ((cp == 0x9) || (cp == 0xA) || (cp == 0xD) || (cp >= 0x20 && cp <= 0xD7FF)
					|| (cp >= 0xE000 && cp <= 0xFFFD) || (cp >= 0x10000 && cp <= 0x10FFFF)) {
				out.appendCodePoint(cp);
			} else {
				out.append('-');
			}
		});

		return out.toString();

	}

} // ..end class ResultsFile
