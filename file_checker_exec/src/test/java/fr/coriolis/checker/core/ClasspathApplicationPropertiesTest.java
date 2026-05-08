package fr.coriolis.checker.core;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ClasspathApplicationPropertiesTest {

  @Test
  public void testSimple() throws Exception {
    ApplicationProperties properties = ClasspathApplicationProperties.getApplicationProperties();
    assertNotNull(properties.getNvsBaseUrl());
    assertFalse(properties.getNvsBaseUrl().isEmpty());
    assertNotNull(properties.getFcVersion());
    assertFalse(properties.getFcVersion().isEmpty());
    assertNotEquals("@project.version@", properties.getFcVersion());
  }

}