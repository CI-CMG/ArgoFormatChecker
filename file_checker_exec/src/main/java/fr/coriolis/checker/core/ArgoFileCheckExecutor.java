package fr.coriolis.checker.core;

import fr.coriolis.checker.core.ArgoDataFile.FileType;
import fr.coriolis.checker.exceptions.NotAnArgoFileException;
import fr.coriolis.checker.exceptions.ValidateFileDataFailedException;
import fr.coriolis.checker.exceptions.VerifyFileFormatFailedException;
import fr.coriolis.checker.output.ResultsFile;
import fr.coriolis.checker.tables.ArgoNVSReferenceTable;
import fr.coriolis.checker.validators.ArgoFileValidator;
import fr.coriolis.checker.validators.ArgoMetadataFileValidator;
import fr.coriolis.checker.validators.ArgoProfileFileValidator;
import fr.coriolis.checker.validators.ArgoTechnicalFileValidator;
import fr.coriolis.checker.validators.ArgoTrajectoryFileValidator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class executes the Argo file validation.
 */
public class ArgoFileCheckExecutor {

  private static final Logger log = LoggerFactory.getLogger(ArgoFileCheckExecutor.class);


  private final ArgoFileCheckExecutorContext context;
  private final ArgoNVSReferenceTable argoNVSReferenceTable;

  public ArgoFileCheckExecutor(ArgoFileCheckExecutorContext context) {
    this.context = context;
    // initialize NVS tables :
    argoNVSReferenceTable = initializeNVSTables(context);
  }


  /**
   * Open the Argo data file.
   *
   * @param inFileName Name of the input file
   * @param dacName Name of the DAC
   * @return ArgoDataFile object if successfully opened and validated
   * @throws Exception if any issue occurs during processing
   */
  private static ArgoDataFile openArgoFile(ArgoNVSReferenceTable argoNVSReferenceTable, Path inFileName, boolean useInternalSpecs, Path specDirName, String dacName) throws NotAnArgoFileException, IOException {
    ArgoDataFile argo = ArgoDataFile.open(argoNVSReferenceTable, inFileName, useInternalSpecs, specDirName, true, dacName);

    if (argo == null) {
      // ..null file means it did not meet the min criteria to be an argo file
      throw new NotAnArgoFileException("ArgoDataFile.open failed: " + ValidationResult.getMessage());
    }

    return argo;
  }

  /**
   * Loop through files and check format and optionally data also
   */
  public void validateFiles(List<String> filesToProcess) {

    // Loop through files list
    for (String file : filesToProcess) {
      // .... get file informations from options :
      Path inFileName = context.getInDir().resolve(file);
      Path outFileName = context.getOutDir().resolve(file + ".filecheck");
      log.info("input file: '" + inFileName + "'");
      log.info("results file: '" + outFileName + "'");

      // .....open the output results file...
      try(ResultsFile out = new ResultsFile(context.isDoXml(), outFileName, context.getApplicationProperties().getFcVersion(), context.getVersionInfoProperties().getSpVersion(), inFileName)) {
        // ......open and process the input file.....
        // ..............open Argo file ....................
        try (ArgoDataFile argo = openArgoFile(argoNVSReferenceTable, inFileName, context.isUseInternalSpecs(), context.getSpecDirName(), context.getDacName())) {

          // ..............instanciate File validator ....................
          ArgoFileValidator argoFileValidator = instanciateSpecializedValidator(argo);

          // .................check the format................
          String phase = "FORMAT-VERIFICATION";
          boolean[] checkFormatResults = checkArgoFileFormat(argo, argoFileValidator, context.getDacName());
          boolean specialPreV31FormatCheckPassed = checkFormatResults[1];
          boolean formatPassed = checkFormatResults[0];

          // ..................check the data..................
          boolean rudimentaryDateCheckDone = rudimentaryDateCheck(formatPassed, argo, argoFileValidator); // true if a rudimentary
          // date check has be
          // done
          // Evaluate is full data check needs to be done
          boolean doDataCheck = isCheckDataToBeDone(formatPassed, context.isDoFormatOnly(),
              rudimentaryDateCheckDone);

          if (doDataCheck) { // Full data check needs to be done
            phase = "DATA-VALIDATION";
            checkArgoFileData(argo, argoFileValidator, context.getDacName(), context.isDoNulls());
          }

          // ..................check file Name...................
          if (context.isDoNameCheck() && formatPassed) {
            // .."name check" requested and no other errors
            phase = "FILE-NAME-CHECK";
            argoFileValidator.validateGdacFileName();
          }
          // ...............report status and meta-data results...............
          // ..status is that open was successful
          // ..- that means identified as Argo netCDF file (DATA_TYPE and FORMAT_VERSION)
          // ..- format may or may not have passed
          // .. - if format did not pass, trying to retrieve the numeric meta-data
          // .. may cause aborts -- i think string types are safe
          // ..try to get as much of the meta-data as exists, but avoid aborts

          if (!specialPreV31FormatCheckPassed) {
            out.oldDModeFile(context.getDacName(), argo.fileVersion());
          } else {
            out.statusAndPhase((argoFileValidator.getValidationResult().nFormatErrors() == 0), phase);
            out.metaData(context.getDacName(), argo, formatPassed, context.isDoPsalStats());
            out.errorsAndWarnings(argoFileValidator);
          }

          // .............................close Argo file......................
          argo.close();
          // .....................Exceptions handle......................

        } catch (NotAnArgoFileException e) {
          log.error(e.getMessage());
          out.notArgoFile(context.getDacName());
        } catch (VerifyFileFormatFailedException e) {
          log.error(e.getMessage());
          out.formatErrorMessage("FORMAT-VERIFICATION");
        } catch (ValidateFileDataFailedException e) {
          log.error(e.getMessage());
          out.dataErrorMessage(e.getMessage());
        } catch (Exception e) {
          log.error("Error processing file: " + file, e);
          out.openError(e);
        }
        log.debug("closing Results file");
      } catch (IOException e) {
        throw new RuntimeException("An error occurred opening output file", e);
      }
    }
  }

  private static ArgoNVSReferenceTable initializeNVSTables(ArgoFileCheckExecutorContext context) {
    if (context.isUseOnlineNVS()) {
      return ArgoNVSReferenceTable.initializeFromInternet(context.getApplicationProperties().getNvsBaseUrl());
    }
    return ArgoNVSReferenceTable.initialize(context.isUseInternalSpecs(), context.getSpecDirName());
  }

  private boolean[] checkArgoFileFormat(ArgoDataFile argo, ArgoFileValidator argoFileValidator, String dacName) throws VerifyFileFormatFailedException {
    boolean[] results = new boolean[2];

    boolean isRegularFormatCheckPassed = regularCheckArgoFileFormat(dacName, argoFileValidator);
    boolean isSpecialPreV31FormatCheckPassed = checkArgoPreV31FileFormat(argo); // return true if not pre v3.1.
    // If pre v3.1, do the special
    // check and return true / false
    // if accepted/refused

    results[0] = isRegularFormatCheckPassed && isSpecialPreV31FormatCheckPassed; // will be false if
    // specialPreV31FormatCheckPassed
    // is false
    results[1] = isSpecialPreV31FormatCheckPassed;

    return results;
  }

  /**
   * Check the format with verifyFormat method from ArgoDataFile. Return true if format accepted, false otherwise. If the verifyFormat method fail,
   * an exception is raised.
   */
  private static boolean regularCheckArgoFileFormat(String dacName, ArgoFileValidator argoFileValidator) throws VerifyFileFormatFailedException {

    // check the format and return true if all process could be done
    boolean isVerifyFormatCompleted = argoFileValidator.validateFormat(dacName);

    if (!isVerifyFormatCompleted) {
      // ..verifyFormat *failed* -- not format errors - an actual failure
      throw new VerifyFileFormatFailedException("verifyFormat check failed: " + ValidationResult.getMessage());

    } else {
      // ..verifyFormat completed -- chech error/warning counts to determine status
      if (argoFileValidator.getValidationResult().nFormatErrors() == 0) {
        log.debug("format ACCEPTED");
        return true;

      } else {
        log.debug("format REJECTED");
        return false;
      }
    }
  }

  /**
   * If Argo Profile file has a version before 3.1, it cannot have a D-mode so the
   * format check don't pass and a special Result File will be issued. If arfo
   * Profile file version is 3.1 or after, return True.
   *
   * @param argo
   * @return true/false
   */
  private static boolean checkArgoPreV31FileFormat(ArgoDataFile argo) {
    // ......SPECIAL CHECK for pre-v3.1 D-mode Profile file......

    if (argo.fileType() == FileType.PROFILE) {
      String dMode = argo.readString("DATA_MODE", true); // ..true -> return NULLs if present
      if (dMode.charAt(0) == 'D') {
        String fv = argo.fileVersion();
        if (fv.compareTo("3.1") < 0) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Do the rudimentary date check if needed. Return true if the
   * rudimentaryDateCheck has be done, false otherwise
   *
   * @param formatPassed
   * @param argo
   * @param argoFileValidator
   * @return
   */
  private boolean rudimentaryDateCheck(boolean formatPassed, ArgoDataFile argo, ArgoFileValidator argoFileValidator) {
    boolean doRudimentaryDateCheck = isDoRudimentaryDateCheck(argo, context.isDoFormatOnlyPre31(),
        context.isDoFormatOnly(), formatPassed);
    if (doRudimentaryDateCheck) {
      // ..passed format checks, format accepted, full data checks not performed
      // because "early version" so do a couple of rudimentary date checks
      argoFileValidator.rudimentaryDateChecks();
    }
    return doRudimentaryDateCheck;
  }

  /**
   * Do the argo file data check by calling the method validate from ArgoDataFile
   * classes.
   *
   * @param argo
   * @param argoFileValidator
   * @param dacName
   * @param doNulls
   * @throws IOException
   * @throws ValidateFileDataFailedException
   */
  private void checkArgoFileData(ArgoDataFile argo, ArgoFileValidator argoFileValidator, String dacName, boolean doNulls)
      throws IOException, ValidateFileDataFailedException {

    if (argo.fileType() == FileType.METADATA) {

      // Do metadata file validate data
      boolean isValidateArgoMetadaFileDataCompleted = argoFileValidator.validateData(doNulls);
      if (!isValidateArgoMetadaFileDataCompleted) {
        // ..the validate process failed (not errors within the data)
        log.error("ArgoMetadataFile.validate failed: " + ValidationResult.getMessage());
        throw new ValidateFileDataFailedException("Meta-data");
      }

    } else if (argo.fileType() == FileType.PROFILE || argo.fileType() == FileType.BIO_PROFILE) {
      // Do profile file validate data
      boolean isValidateArgoProfileFileDataCompleted = argoFileValidator.validateData(false, dacName, doNulls);
      if (!isValidateArgoProfileFileDataCompleted) {
        // ..the validate process failed (not errors within the data)
        log.error("ArgoProfileFile.validate failed: " + ValidationResult.getMessage());
        throw new ValidateFileDataFailedException("Profile");
      }

    } else if (argo.fileType() == FileType.TECHNICAL) {
      // Do Technical file validate data
      boolean isValidateArgoTechnicalFileDataCompleted = argoFileValidator.validateData(dacName, doNulls);
      if (!isValidateArgoTechnicalFileDataCompleted) {
        // ..the validate process failed (not errors within the data)
        log.error("ArgoTechnicalFile.validate failed: " + ValidationResult.getMessage());
        throw new ValidateFileDataFailedException("Technical");
      }

    } else if (argo.fileType() == FileType.TRAJECTORY || argo.fileType() == FileType.BIO_TRAJECTORY) {
      // Do Trajectory file validate data
      boolean isValidateArgoTrajectoryFileDataCompleted = argoFileValidator.validateData(dacName, doNulls);
      if (!isValidateArgoTrajectoryFileDataCompleted) {
        // ..the validate process failed (not errors within the data)
        log.error("ArgoTrajectoryFile.validate failed: " + ValidationResult.getMessage());
        throw new ValidateFileDataFailedException("Trajectory");
      }
    }
  }

  private ArgoFileValidator instanciateSpecializedValidator(ArgoDataFile argo) throws IOException {

    if (argo.fileType() == FileType.METADATA) {
      return new ArgoMetadataFileValidator(argo, argoNVSReferenceTable);
    }
    if (argo.fileType() == FileType.PROFILE || argo.fileType() == FileType.BIO_PROFILE) {
      // Do profile file validate data
      return new ArgoProfileFileValidator(argo, argoNVSReferenceTable);
    }
    if (argo.fileType() == FileType.TECHNICAL) {
      // Do Technical file validate data
      return new ArgoTechnicalFileValidator(argo, argoNVSReferenceTable);

    }
    if (argo.fileType() == FileType.TRAJECTORY || argo.fileType() == FileType.BIO_TRAJECTORY) {
      return new ArgoTrajectoryFileValidator(argo, argoNVSReferenceTable);
    }

    throw new IllegalStateException("Unable to create file validator for type: " + argo.fileType());

  }

  /**
   * Evaluate if data check has to be done.
   *
   * @param formatPassed   (boolean)
   * @param isDoFormatOnly (boolean)
   * @return boolean doDataCheck
   */
  private boolean isCheckDataToBeDone(boolean formatPassed, boolean isDoFormatOnly,
      boolean rudimentaryDateCheckDone) {
    boolean doDataCheck = false;
    if (formatPassed && !rudimentaryDateCheckDone) {
      doDataCheck = true;
      if (isDoFormatOnly) {
        doDataCheck = false;
        log.debug("data check SKIPPED (-format-only)");
      }

    } else {
      doDataCheck = false;
      log.debug("data check SKIPPED (format rejected)");
    }

    return doDataCheck;
  }

  /**
   * Evalutate if a rudimentary DATE checks needs to be done.
   *
   * @param doFormatOnlyPre31
   * @param doFormatOnly
   * @param formatPassed
   * @return boolean doRudimentaryDateCheck
   */
  public boolean isDoRudimentaryDateCheck(ArgoDataFile argo, boolean doFormatOnlyPre31, boolean doFormatOnly,
      boolean formatPassed) {
    if (formatPassed && !doFormatOnly && doFormatOnlyPre31) {
      // ..have to evaluate the version #
      log.debug("argo.fileVersion() = '{}'", argo.fileVersion());
      if (argo.fileVersion().compareTo("3.1") < 0) {
        log.debug("data check SKIPPED");

        // ..format passed, NOT format-only
        // .. requested format-only for pre-3.1 -->
        // .. implies data-checks for v3.1 and beyond
        // .. need to do some rudimentary DATE checks on pre-3.1 files
        return true;
      }
    }
    return false;

  }

}
