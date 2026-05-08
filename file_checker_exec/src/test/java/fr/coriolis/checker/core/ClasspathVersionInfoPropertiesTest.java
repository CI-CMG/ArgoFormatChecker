package fr.coriolis.checker.core;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ClasspathVersionInfoPropertiesTest {

  @Test
  public void testSimple() throws Exception {
    VersionInfoProperties properties = ClasspathVersionInfoProperties.getVersionInfoPropertiesProperties();
    assertNotNull(properties.getSpVersion());
    assertFalse(properties.getSpVersion().isEmpty());
  }
}