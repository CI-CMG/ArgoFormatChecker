package fr.coriolis.checker.core;

/**
 * Contains build information and other properties related to the application.
 */
public interface ApplicationProperties {
  String UNKNOWN_VERSION = "unknown";
  String NVS_DEFAULT_BASE_URL = "https://vocab.nerc.ac.uk/collection/";

  /**
   * Returns the file checker application version.
   *
   * @return the file checker application version
   */
  String getFcVersion();

  /**
   * Returns the NERC Vocabulary Server URL
   *
   * @return the NERC Vocabulary Server URL
   */
  String getNvsBaseUrl();
}
