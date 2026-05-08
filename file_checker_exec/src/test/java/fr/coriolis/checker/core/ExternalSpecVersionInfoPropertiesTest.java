package fr.coriolis.checker.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class ExternalSpecVersionInfoPropertiesTest {

  @Test
  public void testSimple() throws Exception {
    VersionInfoProperties properties = ExternalSpecVersionInfoProperties.loadVersionInfoPropertiesProperties(Paths.get("../file_checker_spec"));
    assertNotNull(properties.getSpVersion());
    assertFalse(properties.getSpVersion().isEmpty());
  }

}