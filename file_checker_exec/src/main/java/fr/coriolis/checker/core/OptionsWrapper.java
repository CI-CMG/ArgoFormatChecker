package fr.coriolis.checker.core;

import fr.coriolis.checker.config.Options;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation of {@link ArgoFileCheckExecutorContext} that wraps a {@link Options} object used when running as a command line application.
 * This implementation of {@link ArgoFileCheckExecutorContext} caches the {@link VersionInfoProperties} when created.
 *
 */
public class OptionsWrapper implements ArgoFileCheckExecutorContext {

  private final Options options;
  // This implementation of ArgoFileCheckExecutorContext caches the VersionInfoProperties
  private final VersionInfoProperties versionInfoProperties;

  public OptionsWrapper(Options options) {
    this.options = options;
    versionInfoProperties = options.isUseInternalSpecs()
        ? ClasspathVersionInfoProperties.getVersionInfoPropertiesProperties()
        : ExternalSpecVersionInfoProperties.loadVersionInfoPropertiesProperties(Paths.get(options.getSpecDirName()));
  }

  @Override
  public String getDacName() {
    return options.getDacName();
  }

  @Override
  public boolean isUseOnlineNVS() {
    return options.isUseOnlineNVS();
  }

  @Override
  public Path getSpecDirName() {
    return Paths.get(options.getSpecDirName());
  }

  @Override
  public boolean isDoNulls() {
    return options.isDoNulls();
  }

  @Override
  public boolean isDoFormatOnly() {
    return options.isDoFormatOnly();
  }

  @Override
  public boolean isDoNameCheck() {
    return options.isDoNameCheck();
  }

  @Override
  public boolean isDoPsalStats() {
    return options.isDoPsalStats();
  }

  @Override
  public boolean isDoFormatOnlyPre31() {
    return options.isDoFormatOnlyPre31();
  }

  @Override
  public Path getInDir() {
    return Paths.get(options.getInDirName());
  }

  @Override
  public Path getOutDir() {
    return Paths.get(options.getOutDirName());
  }

  @Override
  public boolean isDoXml() {
    return options.isDoXml();
  }

  @Override
  public boolean isUseInternalSpecs() {
    return options.isUseInternalSpecs();
  }

  @Override
  public ApplicationProperties getApplicationProperties() {
    return ClasspathApplicationProperties.getApplicationProperties();
  }

  @Override
  public VersionInfoProperties getVersionInfoProperties() {
    return versionInfoProperties;
  }


}
