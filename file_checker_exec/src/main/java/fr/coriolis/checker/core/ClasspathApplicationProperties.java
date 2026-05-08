package fr.coriolis.checker.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This implementation of {@link ApplicationProperties} reads properties from the classpath that were set at build time.
 * This class is immutable and thread safe.
 */
public class ClasspathApplicationProperties implements ApplicationProperties {


  private static final String PROP_FILE_NAME = "Application.properties";
  private static final ApplicationProperties theProperties = loadApplicationProperties();


  /**
   * Returns the singleton, thread safe and immutable {@link ApplicationProperties}
   *
   * @return the singleton, thread safe and immutable {@link ApplicationProperties}
   */
  public static ApplicationProperties getApplicationProperties() {
    return theProperties;
  }

  private static ApplicationProperties loadApplicationProperties() {
    try (InputStream in = ClasspathApplicationProperties.class.getResourceAsStream(PROP_FILE_NAME)) {
      Properties codeProp = new Properties();
      codeProp.load(in);
      return new ClasspathApplicationProperties(
          codeProp.getProperty("Version", UNKNOWN_VERSION),
          System.getenv().getOrDefault("NVS_BASE_URL", codeProp.getProperty("nvs.baseurl.default", NVS_DEFAULT_BASE_URL))
      );
    } catch (IOException e) {
      throw new RuntimeException("could not read codeProp file " + PROP_FILE_NAME, e);
    }
  }

  private final String fcVersion;
  private final String nvsBaseUrl;

  private ClasspathApplicationProperties(String fcVersion, String nvsBaseUrl) {
    this.fcVersion = fcVersion;
    this.nvsBaseUrl = nvsBaseUrl;
  }

  @Override
  public String getFcVersion() {
    return fcVersion;
  }

  @Override
  public String getNvsBaseUrl() {
    return nvsBaseUrl;
  }
}
