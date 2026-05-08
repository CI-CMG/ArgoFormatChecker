package fr.coriolis.checker.core;

/**
 * Contains properties related to the file checker specifications.
 */
public interface VersionInfoProperties {

  String UNKNOWN_VERSION = "unknown";

  /**
   * Returns the version of the file checker specification suite.
   *
   * @return the version of the file checker specification suite
   */
  String getSpVersion();

}
