package fr.coriolis.checker.core;

import fr.coriolis.checker.specs.SpecIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * This implementation of {@link VersionInfoProperties} reads properties from an external directory.
 * This class is immutable and thread safe.
 */
public class ExternalSpecVersionInfoProperties implements VersionInfoProperties {

  private static final String SPEC_PROP_FILE_NAME = "VersionInfo.properties";

  /**
   * Reads the VersionInfo.properties file contained in the provided specification directory, and returns a new {@link VersionInfoProperties}
   * object that is immutable and thread safe.
   *
   * @param specDir the specification directory containing a file named VersionInfo.properties
   * @return a new {@link VersionInfoProperties} object
   */
  public static VersionInfoProperties loadVersionInfoPropertiesProperties(Path specDir) {
    try (InputStream in = SpecIO.openExternal(specDir.resolve(SPEC_PROP_FILE_NAME))) {
      Properties props = new Properties();
      props.load(in);
      return new ExternalSpecVersionInfoProperties(props.getProperty("Version", UNKNOWN_VERSION));
    } catch (IOException e) {
      throw new RuntimeException("could not read specProp file " + SPEC_PROP_FILE_NAME, e);
    }
  }

  private final String spVersion;

  private ExternalSpecVersionInfoProperties(String spVersion) {
    this.spVersion = spVersion;
  }

  @Override
  public String getSpVersion() {
    return spVersion;
  }
}
