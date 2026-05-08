package fr.coriolis.checker.core;

import java.nio.file.Path;

/**
 * Interface containing accessors for parameters needed when executing Argo file validation.
 */
public interface ArgoFileCheckExecutorContext {

  /**
   * Gets the DAC name being processed.
   *
   * @return the DAC name being processed
   */
  String getDacName();

  /**
   * Returns true if the online NERC Vocabulary Server should be used.
   *
   * @return true if the online NERC Vocabulary Server should be used, false to use local.
   */
  boolean isUseOnlineNVS();

  /**
   * Gets the directory to the file checker specifications.  May be null if {@link #isUseInternalSpecs()} returns false.
   * An exception will be thrown if {@link #isUseInternalSpecs()} returns true and this value is null.
   *
   * @return the directory to the file checker specifications
   */
  Path getSpecDirName();

  /**
   * Indicates to perform 'nulls-in-string' check
   *
   * @return true if 'nulls-in-string' check should be performed
   */
  boolean isDoNulls();

  /**
   * Indicates to only perform format checks to the files
   *
   * @return true if only format checks should be performed
   */
  boolean isDoFormatOnly();

  /**
   * Indicates to check the file name when processing.
   *
   * @return true to check the file name when processing
   */
  boolean isDoNameCheck();

  /**
   * Indicates to put PSAL adjustment statistics into results file
   *
   * @return true to put PSAL adjustment statistics into results file
   */
  boolean isDoPsalStats();

  /**
   * Indicates to only perform format checks on files format pre-3.1
   *
   * @return true to only perform format checks on files format pre-3.1
   */
  boolean isDoFormatOnlyPre31();

  /**
   * Returns the directory containing the Argo Files to validate.
   *
   * @return the directory containing the Argo Files to validate
   */
  Path getInDir();

  /**
   * Returns the output directory to save validation result files.
   *
   * @return the output directory to save validation result files
   */
  Path getOutDir();

  /**
   * Indicates that the output should be XML files.
   *
   * @return true if the output should be XML files
   */
  boolean isDoXml();

  /**
   * Indicates if file checker specifications should be used from the classpath or from an external directory.
   * If true, the {@link #getSpecDirName()} must return an appropriate path to the files.
   *
   * @return true if file checker specifications should be used from the classpath or false to read from the {@link #getSpecDirName()} directory
   */
  boolean isUseInternalSpecs();

  /**
   * Gets the {@link ApplicationProperties} containing the file checker version and other build information.
   *
   * @return the {@link ApplicationProperties} containing the file checker version and other build information
   */
  ApplicationProperties getApplicationProperties();

  /**
   * Gets the {@link VersionInfoProperties} containing specification version information.
   *
   * @return the {@link VersionInfoProperties} containing specification version information
   */
  VersionInfoProperties getVersionInfoProperties();

}
