package fr.coriolis.checker.core;

import fr.coriolis.checker.config.Options;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Argo FileChecker data file validation checking.
 * <p>
 * Separate documentation exists:
 * <ul>
 * <li>Details of the checks: Argo Data File Format and Consistency Checks
 * <li>Users Manual:
 * </ul>
 * <p>
 *
 * @author Mark Ignaszewski
 * @version $Id: ValidateSubmit.java 1319 2022-04-14 21:48:55Z ignaszewski $
 */
public class ValidateSubmit {

  // ......................Variable Declarations................
  private static final Logger log = LoggerFactory.getLogger(ValidateSubmit.class.getSimpleName());

  static {
    System.setProperty("logfile.name", ValidateSubmit.class.getSimpleName() + "_LOG");
  }

  // .................................................................
  //
  // main
  //
  // .................................................................

  public static void main(String args[]) throws IOException {

    log.info("{}:  START", ValidateSubmit.class.getSimpleName());
    // .....extract the options from command-line arguments....
    try {
      Options.init(args);
      Options options = Options.getInstance();

      File inDir = new File(options.getInDirName()); // already checked in Options that it is a directory.

      // is help is asked :
      displayHelpIfAsked(options);

      // is application version is asked :
      displayVersionIfAsked(options);

      // validate Mandatory arguments :
      options.validateMandatoryArguments(); // System exit with error if no validated

      // ....................get list of input files.................
      List<String> filesToProcess = getFilesToProcessList(options.getListFile(), options.getInFileList(), inDir);

      // ..................check format and data (optional) of all files in
      // list......................
      ArgoFileCheckExecutor validator = new ArgoFileCheckExecutor(new OptionsWrapper(options));
      validator.validateFiles(filesToProcess);

    } catch (Exception e) {
      System.err.println(e.getMessage());
      Help();
      System.exit(1);
    }

  }// ..end main

  // .................................................................
  //
  // Methods
  //
  // .................................................................

  private static void displayHelpIfAsked(Options options) {
    if (options.isHelp()) {
      ValidateSubmit.Help();
      System.exit(0);
    }

  }

  /**
   * display application version if asked in the command-line arguments. End program after.
   */
  private static void displayVersionIfAsked(Options options) {
    if (options.isVersion()) {
      System.out.println(" ");
      System.out.println("Code version: " + ClasspathApplicationProperties.getApplicationProperties().getFcVersion());
      System.exit(0);
    }
  }

	/**
	 * input files are chosen in the following priority order : 1) an
	 * input-file-list (overrides all other lists) 2) file name arguments (already
	 * parsed above, if specified) 3) all files in the input directory
	 *
	 * @param listFile   (String) path name of the file containing a list of files
	 *                   to process
	 * @param inFileList List<String> list of input files given in command-line
	 *                   arguments
	 * @param inDir      (String) Directory path where input files reside
	 * @return List<String> of input files (paths) to process
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static List<String> getFilesToProcessList(String listFile, List<String> inFileList, File inDir)
			throws FileNotFoundException, IOException {

		List<String> filesToProcess = new ArrayList<>(200);
		if (listFile != null) { // 1
			// ..a list file was specified - open and read it
			// ..this overrides all other "input lists"
			File f = new File(listFile);
			if (!f.isFile()) {
				log.error("-list-file does not exist: '" + listFile + "'");
        System.err.println("\nERROR: -list-file DOES NOT EXIST: '" + listFile + "'");
				System.exit(1);
			} else if (!f.canRead()) {
				log.error("-list-file cannot be read: '" + listFile + "'");
        System.err.println("\nERROR: -list-file CANNOT BE READ: '" + listFile + "'");
				System.exit(1);
			}
			// ..open and read the file
			BufferedReader file = new BufferedReader(new FileReader(listFile));
			// inFileList = new ArrayList<String>(200);
			String line;
			while ((line = file.readLine()) != null) {
				if (line.trim().length() > 0) {
					filesToProcess.add(line.trim());
				}
			}
			file.close();
			log.info("Read {} entries from -list-file '{}'", filesToProcess.size(), listFile);

		} else if (inFileList != null) { // 2
			filesToProcess = inFileList;

		} else if (inFileList == null) { // 3
			filesToProcess = Arrays.asList(inDir.list());
			log.debug("inFileList: all files in directory. size = {}", filesToProcess.size());
		}
		return filesToProcess;
	}

	public static void Help() {
		System.out.println("\n" + "Purpose: Validates the files in a directory\n" + "\n" + "Usage: java  " + ValidateSubmit.class.getSimpleName()
				+ " [options] dac-name [spec-dir] output-dir input-dir [file-names]\n" + "Options:\n"
				+ "   -help | -H | -U   Help -- this message\n" + "   -no-name-check Do not check the file name\n"
				+ "   -null-warn     Perform 'nulls-in-string' check (warning)\n"
				+ "                  default: do NOT check for nulls\n"
				+ "   -text-result   Text-formatted results files\n"
				+ "                  default: XML-formatted results files\n"
				+ "   -list-file <list-file-path>  File containing list of files to process\n"
				+ "                                default: no list-file (see Input Files below)\n"
				+ "   -format-only   Only perform format checks to the files -- no data checks\n"
				+ "                  default: perform format and data checks\n"
				+ "   -data-check-all      Format and data checks for all files\n"
				+ "                        default: Only perform format checks on pre-3.1 files\n"
				+ "   -psal-stats    Put PSAL adjustment statistics into results file\n"
				+ "                  default: don't compute this information\n" + "\n"
				+ "   -format-only-pre3.1  (default) Only perform format checks on files format pre-3.1\n"
				+ "      ***deprecated - now the default - retained for backwards compatibility***\n" + "\n"
				+ "   -internal-specs  Use specs files wich are included in the JAR archive.\n"
				+ "                     With this option, spec-dir argument should not be provided.\n "
				+ "   -online-nvs  Use directly up-to-date NVS from internet. NVS forlder in spec dir will therefore be ignored.\n"
				+ "Arguments:\n" + "   dac-name       Name of DAC that owns the input files\n"
				+ "   spec-dir       Directory path of specification files. Do not specify if -internal-specs is used\n"
				+ "   output-dir     Directory path where results files will be placed\n"
				+ "   input-dir      Directory path where input files reside\n"
				+ "   file-names     (Optional) List of files names to process (see below)\n" + "\n" + "Input Files:\n"
				+ "   Input files to process are determined in one of the following ways (priority order):\n"
				+ "   1) -list-file              List of names will be read from <list-file-path>\n"
				+ "   2) [file-names] argument   Files listed on command-line will be processed\n"
				+ "   3) All files in 'input-dir' will be processed\n" + "\n");
	}

}
