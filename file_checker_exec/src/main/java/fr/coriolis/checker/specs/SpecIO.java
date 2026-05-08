package fr.coriolis.checker.specs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SpecIO {

  private static final String RESOURCES_BASE_PATH = "/file_checker_spec";

  public static InputStream openInternal(String fileName) throws IOException {
    String resourcePath = RESOURCES_BASE_PATH + "/" + fileName; // ex: "/specs/spec.properties"
    InputStream in = SpecIO.class.getResourceAsStream(resourcePath);
    if (in == null) {
      throw new FileNotFoundException("File not found: " + resourcePath);
    }
    return in;
  }

  public static InputStream openExternal(Path file) throws IOException {
    if (!Files.exists(file)) {
      throw new FileNotFoundException("File not found: " + file);
    }
    return Files.newInputStream(file);
  }

  public static InputStream open(boolean useInternal, Path specDir, String fileName) throws IOException {
    return useInternal ?  openInternal(fileName) : openExternal(specDir.resolve(fileName));
  }

  // prevent instantiation of utility class
  private SpecIO() {

  }

}
