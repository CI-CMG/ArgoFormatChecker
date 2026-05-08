package fr.coriolis.checker.core;

import fr.coriolis.checker.specs.SpecIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This implementation of {@link VersionInfoProperties} reads properties from the classpath.
 * This class is immutable and thread safe.
 */
public class ClasspathVersionInfoProperties implements VersionInfoProperties {


  private static final String SPEC_PROP_FILE_NAME = "VersionInfo.properties";
  private static final VersionInfoProperties theProperties = loadVersionInfoPropertiesProperties();


  /**
   * Returns the singleton, thread safe and immutable {@link VersionInfoProperties}
   *
   * @return the singleton, thread safe and immutable {@link VersionInfoProperties}
   */
  public static VersionInfoProperties getVersionInfoPropertiesProperties() {
    return theProperties;
  }

  private static VersionInfoProperties loadVersionInfoPropertiesProperties() {
    try (InputStream in = SpecIO.openInternal(SPEC_PROP_FILE_NAME)) {
      Properties props = new Properties();
      props.load(in);
      return new ClasspathVersionInfoProperties(props.getProperty("Version", UNKNOWN_VERSION));
    } catch (IOException e) {
      throw new RuntimeException("could not read specProp file " + SPEC_PROP_FILE_NAME, e);
    }
  }

  private final String spVersion;

  private ClasspathVersionInfoProperties(String spVersion) {
    this.spVersion = spVersion;
  }

  @Override
  public String getSpVersion() {
    return spVersion;
  }
}
