package fr.coriolis.checker.core;


import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.colorado.cires.argonaut.xml.filecheck.FileCheckResults;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArgoFileCheckExecutorTest {

  private static final Path outputDir = Paths.get("target/test-output");
  private static final Path testFiles = Paths.get("src/test/netcdf-test-files");

  @BeforeEach
  public void setup() throws Exception {
    FileUtils.deleteQuietly(outputDir.toFile());
    Files.createDirectories(outputDir);
  }

  @AfterEach
  public void tearDown() throws IOException {
    FileUtils.deleteQuietly(outputDir.toFile());
  }

  private static final List<Runnable> RUNNERS = Arrays.asList(
      () -> new ArgoFileCheckExecutor(TestContext.builder()
          .withDacName("aoml")
          .withInDir(testFiles.resolve("TEST_ALL_0003"))
          .withDoNameCheck(false)
          .build())
          .validateFiles(Arrays.asList(
              "BD4900476_032_DOXY2.nc",
              "BD4900476_032_DOXY_2.nc"
          )),
      () -> new ArgoFileCheckExecutor(TestContext.builder()
          .withDacName("coriolis")
          .withInDir(testFiles.resolve("TEST_ALL_0001"))
          .withDoFormatOnly(true)
          .build())
          .validateFiles(Arrays.asList(
              "6903283_badName.nc",
              "6903283_tech.nc")),
      () -> new ArgoFileCheckExecutor(TestContext.builder()
          .withDacName("aoml")
          .withInDir(testFiles.resolve("TEST_ALL_0002"))
          .withDoNameCheck(false)
          .build())
          .validateFiles(Arrays.asList(
              "D4900757_024_CNDC_with_ADJUSTED.nc",
              "D4900757_024_CNDC_without_ADJUSTED.nc"
          )),
      () -> new ArgoFileCheckExecutor(TestContext.builder()
          .withDacName("coriolis")
          .withInDir(testFiles.resolve("TEST_ALL_0006"))
          .withDoNameCheck(false)
          .build())
          .validateFiles(Arrays.asList(
              "5907141_meta_apostrophe.nc",
              "5907141_meta_Bad_PIName.nc",
              "5907141_meta_BadSpaceCaractere.nc",
              "5907141_meta_multipleName.nc"
          )),
      () -> new ArgoFileCheckExecutor(TestContext.builder()
          .withDacName("bodc")
          .withInDir(testFiles.resolve("TEST_ALL_0005"))
          .withDoNameCheck(false)
          .build())
          .validateFiles(Arrays.asList(
              "D4900757_024_CNDC_without_ADJUSTED_version-3.0_DMode-A.nc",
              "D4900757_024_CNDC_with_ADJUSTED_version-3.0_DMode-A.nc"
          ))
  );

  @Test
  public void testSingleThread() throws Exception {
    RUNNERS.forEach(Runnable::run);
    assertSavedFiles();
  }

  @Test
  public void testMultiThread() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(RUNNERS.size());
    List<Future<?>> futures = new ArrayList<>();
    for (Runnable runner : RUNNERS) {
      futures.add(pool.submit(runner));
    }
    for (Future<?> future : futures) {
      future.get(10, TimeUnit.SECONDS);
    }
    assertSavedFiles();
  }




  private static void assertSavedFiles() throws JAXBException, IOException {
    assertEqualsBD4900476_032_DOXY2();
    assertEqualsBD4900476_032_DOXY_2();
    assertEquals6903283_badName();
    assertEquals6903283_tech();
    assertEqualsD4900757_024_CNDC_with_ADJUSTED();
    assertEqualsD4900757_024_CNDC_without_ADJUSTED();
    assertEquals5907141_meta_apostrophe();
    assertEquals5907141_meta_Bad_PIName();
    assertEquals5907141_meta_BadSpaceCaractere();
    assertEquals5907141_meta_multipleName();
    assertEqualsD4900757_024_CNDC_with_ADJUSTED_version_3();
    assertEqualsD4900757_024_CNDC_without_ADJUSTED_version_3();
  }


  private static void assertEqualsBD4900476_032_DOXY2() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("BD4900476_032_DOXY2.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0003/BD4900476_032_DOXY2.nc", saved.getFile());
    assertEquals("FILE-REJECTED", saved.getStatus());
    assertEquals("FORMAT-VERIFICATION", saved.getPhase());
    assertEquals("aoml", saved.getMetadata().getDac());
    assertEquals("B-Argo profile", saved.getMetadata().getDATATYPE());
    assertEquals("3.1", saved.getMetadata().getFORMATVERSION());
    assertEquals("20200310105513", saved.getMetadata().getDATEUPDATE());
    assertEquals("AO", saved.getMetadata().getDATACENTRE());
    assertEquals("4900476", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals("STEPHEN RISER", saved.getMetadata().getPINAME());
    assertEquals("846", saved.getMetadata().getWMOINSTTYPE());
    assertEquals("D", saved.getMetadata().getDATAMODE());
    assertEquals("A", saved.getMetadata().getDIRECTION());
    assertEquals("null", saved.getMetadata().getPROFILETEMPQC());
    assertEquals("null", saved.getMetadata().getPROFILEPSALQC());
    assertEquals("A", saved.getMetadata().getPROFILEDOXYQC());
    assertEquals("PRES FREQUENCY_DOXY DOXY TEMP_DOXY2 BPHASE_DOXY2 DOXY2", saved.getMetadata().getSTATIONPARAMETERS());
    assertEquals("RRDRRD", saved.getMetadata().getPARAMETERDATAMODE());
    assertEquals(18, saved.getErrors().getNumber());
    assertEquals(Arrays.asList(
        "variable: PROFILE_TEMP_DOXY2_QC: not defined in specification 'b_profile:v3.1'",
        "variable: PROFILE_BPHASE_DOXY2_QC: not defined in specification 'b_profile:v3.1'",
        "variable: PROFILE_DOXY2_QC: not defined in specification 'b_profile:v3.1'",
        "variable: TEMP_DOXY2: not defined in specification 'b_profile:v3.1'",
        "variable: TEMP_DOXY2_QC: not defined in specification 'b_profile:v3.1'",
        "variable: TEMP_DOXY2_ADJUSTED: not defined in specification 'b_profile:v3.1'",
        "variable: TEMP_DOXY2_ADJUSTED_QC: not defined in specification 'b_profile:v3.1'",
        "variable: TEMP_DOXY2_ADJUSTED_ERROR: not defined in specification 'b_profile:v3.1'",
        "variable: BPHASE_DOXY2: not defined in specification 'b_profile:v3.1'",
        "variable: BPHASE_DOXY2_QC: not defined in specification 'b_profile:v3.1'",
        "variable: BPHASE_DOXY2_ADJUSTED: not defined in specification 'b_profile:v3.1'",
        "variable: BPHASE_DOXY2_ADJUSTED_QC: not defined in specification 'b_profile:v3.1'",
        "variable: BPHASE_DOXY2_ADJUSTED_ERROR: not defined in specification 'b_profile:v3.1'",
        "variable: DOXY2: not defined in specification 'b_profile:v3.1'",
        "variable: DOXY2_QC: not defined in specification 'b_profile:v3.1'",
        "variable: DOXY2_ADJUSTED: not defined in specification 'b_profile:v3.1'",
        "variable: DOXY2_ADJUSTED_QC: not defined in specification 'b_profile:v3.1'",
        "variable: DOXY2_ADJUSTED_ERROR: not defined in specification 'b_profile:v3.1'"
    ), saved.getErrors().getErrors());
    assertEquals(0, saved.getWarnings().getNumber());
    assertEquals(Collections.emptyList(), saved.getWarnings().getWarnings());
  }

  private static void assertEqualsBD4900476_032_DOXY_2() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("BD4900476_032_DOXY_2.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0003/BD4900476_032_DOXY_2.nc", saved.getFile());
    assertEquals("FILE-ACCEPTED", saved.getStatus());
    assertEquals("DATA-VALIDATION", saved.getPhase());
    assertEquals("aoml", saved.getMetadata().getDac());
    assertEquals("B-Argo profile", saved.getMetadata().getDATATYPE());
    assertEquals("3.1", saved.getMetadata().getFORMATVERSION());
    assertEquals("20200310105513", saved.getMetadata().getDATEUPDATE());
    assertEquals("AO", saved.getMetadata().getDATACENTRE());
    assertEquals("4900476", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals("STEPHEN RISER", saved.getMetadata().getPINAME());
    assertEquals("846", saved.getMetadata().getWMOINSTTYPE());
    assertEquals("D", saved.getMetadata().getDATAMODE());
    assertEquals("A", saved.getMetadata().getDIRECTION());
    assertEquals(1, saved.getMetadata().getNPROF());
    assertEquals(69, saved.getMetadata().getNLEVELS());
    assertEquals("032", saved.getMetadata().getCYCLENUMBER());
    assertEquals("-45.7600", saved.getMetadata().getLATITUDE());
    assertEquals("103.0320", saved.getMetadata().getLONGITUDE());
    assertEquals("20080110191412", saved.getMetadata().getJULDDtg());
    assertEquals("1", saved.getMetadata().getJULDQC());
    assertEquals("1", saved.getMetadata().getPOSITIONQC());
    assertEquals("null", saved.getMetadata().getPROFILETEMPQC());
    assertEquals("null", saved.getMetadata().getPROFILEPSALQC());
    assertEquals("A", saved.getMetadata().getPROFILEDOXYQC());
    assertEquals("PRES FREQUENCY_DOXY DOXY TEMP_DOXY_2 BPHASE_DOXY_2 DOXY_2", saved.getMetadata().getSTATIONPARAMETERS());
    assertEquals("RRDRRD", saved.getMetadata().getPARAMETERDATAMODE());
    assertEquals(0, saved.getErrors().getNumber());
    assertEquals(Collections.emptyList(), saved.getErrors().getErrors());
    assertEquals(0, saved.getWarnings().getNumber());
    assertEquals(Collections.emptyList(), saved.getWarnings().getWarnings());
  }

  private static void assertEquals6903283_badName() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("6903283_badName.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0001/6903283_badName.nc", saved.getFile());
    assertEquals("FILE-REJECTED", saved.getStatus());
    assertEquals("FILE-NAME-CHECK", saved.getPhase());
    assertEquals("coriolis", saved.getMetadata().getDac());
    assertEquals("Argo technical data", saved.getMetadata().getDATATYPE());
    assertEquals("3.1", saved.getMetadata().getFORMATVERSION());
    assertEquals("20180627112958", saved.getMetadata().getDATEUPDATE());
    assertEquals("IF", saved.getMetadata().getDATACENTRE());
    assertEquals("6903283", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals(1, saved.getErrors().getNumber());
    assertEquals(Collections.singletonList("Inconsistent file name\n"
            + "\tDAC file name       '6903283_badName.nc'\n"
            + "\tExpected file name according to file type, DIRECTION, DATA_MODE, CYCLE_NUMBER and PLATFORM_NUMBER : '6903283_tech.nc'"),
        saved.getErrors().getErrors());
  }

  private static void assertEquals6903283_tech() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("6903283_tech.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0001/6903283_tech.nc", saved.getFile());
    assertEquals("FILE-ACCEPTED", saved.getStatus());
    assertEquals("FILE-NAME-CHECK", saved.getPhase());
    assertEquals("coriolis", saved.getMetadata().getDac());
    assertEquals("Argo technical data", saved.getMetadata().getDATATYPE());
    assertEquals("3.1", saved.getMetadata().getFORMATVERSION());
    assertEquals("20180627112958", saved.getMetadata().getDATEUPDATE());
    assertEquals("IF", saved.getMetadata().getDATACENTRE());
    assertEquals("6903283", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals(0, saved.getErrors().getNumber());
    assertEquals(Collections.emptyList(), saved.getErrors().getErrors());
    assertEquals(0, saved.getWarnings().getNumber());
    assertEquals(Collections.emptyList(), saved.getWarnings().getWarnings());
  }

  private static void assertEqualsD4900757_024_CNDC_with_ADJUSTED() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("D4900757_024_CNDC_with_ADJUSTED.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0002/D4900757_024_CNDC_with_ADJUSTED.nc", saved.getFile());
    assertEquals("FILE-ACCEPTED", saved.getStatus());
    assertEquals("DATA-VALIDATION", saved.getPhase());
    assertEquals("aoml", saved.getMetadata().getDac());
    assertEquals("Argo profile", saved.getMetadata().getDATATYPE());
    assertEquals("3.1", saved.getMetadata().getFORMATVERSION());
    assertEquals("20191028121515", saved.getMetadata().getDATEUPDATE());
    assertEquals("AO", saved.getMetadata().getDATACENTRE());
    assertEquals("4900757", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals("BRECK OWENS", saved.getMetadata().getPINAME());
    assertEquals("852", saved.getMetadata().getWMOINSTTYPE());
    assertEquals("D", saved.getMetadata().getDATAMODE());
    assertEquals("A", saved.getMetadata().getDIRECTION());
    assertEquals(1, saved.getMetadata().getNPROF());
    assertEquals(76, saved.getMetadata().getNLEVELS());
    assertEquals("024", saved.getMetadata().getCYCLENUMBER());
    assertEquals("19.6310", saved.getMetadata().getLATITUDE());
    assertEquals("-43.7750", saved.getMetadata().getLONGITUDE());
    assertEquals("20070126124001", saved.getMetadata().getJULDDtg());
    assertEquals("1", saved.getMetadata().getJULDQC());
    assertEquals("1", saved.getMetadata().getPOSITIONQC());
    assertEquals("F", saved.getMetadata().getPROFILETEMPQC());
    assertEquals("F", saved.getMetadata().getPROFILEPSALQC());
    assertEquals("null", saved.getMetadata().getPROFILEDOXYQC());
    assertEquals("PRES TEMP PSAL CNDC", saved.getMetadata().getSTATIONPARAMETERS());
    assertEquals("----", saved.getMetadata().getPARAMETERDATAMODE());
    assertEquals("99999.0000", saved.getMetadata().getPsalAdjMean());
    assertEquals("99999.0000", saved.getMetadata().getPsalAdjSdev());
    assertEquals(0, saved.getErrors().getNumber());
    assertEquals(Collections.emptyList(), saved.getErrors().getErrors());
    assertEquals(1, saved.getWarnings().getNumber());
    assertEquals(Collections.singletonList(
            "VERTICAL_SAMPLING_SCHEME[1]: Invalid: 'Primary sampling: averaged [data averaged with equal weights into irregular pressure bins'   *** WILL BECOME AN ERROR ***"),
        saved.getWarnings().getWarnings());
  }

  private static void assertEqualsD4900757_024_CNDC_without_ADJUSTED() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("D4900757_024_CNDC_without_ADJUSTED.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0002/D4900757_024_CNDC_without_ADJUSTED.nc", saved.getFile());
    assertEquals("FILE-ACCEPTED", saved.getStatus());
    assertEquals("DATA-VALIDATION", saved.getPhase());
    assertEquals("aoml", saved.getMetadata().getDac());
    assertEquals("Argo profile", saved.getMetadata().getDATATYPE());
    assertEquals("3.1", saved.getMetadata().getFORMATVERSION());
    assertEquals("20191028121515", saved.getMetadata().getDATEUPDATE());
    assertEquals("AO", saved.getMetadata().getDATACENTRE());
    assertEquals("4900757", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals("BRECK OWENS", saved.getMetadata().getPINAME());
    assertEquals("852", saved.getMetadata().getWMOINSTTYPE());
    assertEquals("D", saved.getMetadata().getDATAMODE());
    assertEquals("A", saved.getMetadata().getDIRECTION());
    assertEquals(1, saved.getMetadata().getNPROF());
    assertEquals(76, saved.getMetadata().getNLEVELS());
    assertEquals("024", saved.getMetadata().getCYCLENUMBER());
    assertEquals("19.6310", saved.getMetadata().getLATITUDE());
    assertEquals("-43.7750", saved.getMetadata().getLONGITUDE());
    assertEquals("20070126124001", saved.getMetadata().getJULDDtg());
    assertEquals("1", saved.getMetadata().getJULDQC());
    assertEquals("1", saved.getMetadata().getPOSITIONQC());
    assertEquals("F", saved.getMetadata().getPROFILETEMPQC());
    assertEquals("F", saved.getMetadata().getPROFILEPSALQC());
    assertEquals("null", saved.getMetadata().getPROFILEDOXYQC());
    assertEquals("PRES TEMP PSAL CNDC", saved.getMetadata().getSTATIONPARAMETERS());
    assertEquals("----", saved.getMetadata().getPARAMETERDATAMODE());
    assertEquals("99999.0000", saved.getMetadata().getPsalAdjMean());
    assertEquals("99999.0000", saved.getMetadata().getPsalAdjSdev());
    assertEquals(0, saved.getErrors().getNumber());
    assertEquals(Collections.emptyList(), saved.getErrors().getErrors());
    assertEquals(1, saved.getWarnings().getNumber());
    assertEquals(Collections.singletonList(
            "VERTICAL_SAMPLING_SCHEME[1]: Invalid: 'Primary sampling: averaged [data averaged with equal weights into irregular pressure bins'   *** WILL BECOME AN ERROR ***"),
        saved.getWarnings().getWarnings());
  }

  private static void assertEquals5907141_meta_apostrophe() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("5907141_meta_apostrophe.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0006/5907141_meta_apostrophe.nc", saved.getFile());
    assertEquals("FILE-ACCEPTED", saved.getStatus());
    assertEquals("DATA-VALIDATION", saved.getPhase());
    assertEquals("coriolis", saved.getMetadata().getDac());
    assertEquals("Argo meta-data", saved.getMetadata().getDATATYPE());
    assertEquals("3.1", saved.getMetadata().getFORMATVERSION());
    assertEquals("20251216052621", saved.getMetadata().getDATEUPDATE());
    assertEquals("IF", saved.getMetadata().getDATACENTRE());
    assertEquals("5907141", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals("Fabrizio D'ORTENZIO", saved.getMetadata().getPINAME());
    assertEquals("844", saved.getMetadata().getWMOINSTTYPE());
    assertEquals(0, saved.getErrors().getNumber());
    assertEquals(Collections.emptyList(), saved.getErrors().getErrors());
    assertEquals(0, saved.getWarnings().getNumber());
    assertEquals(Collections.emptyList(), saved.getWarnings().getWarnings());
  }

  private static void assertEquals5907141_meta_Bad_PIName() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("5907141_meta_Bad_PIName.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0006/5907141_meta_Bad_PIName.nc", saved.getFile());
    assertEquals("FILE-ACCEPTED", saved.getStatus());
    assertEquals("DATA-VALIDATION", saved.getPhase());
    assertEquals("coriolis", saved.getMetadata().getDac());
    assertEquals("Argo meta-data", saved.getMetadata().getDATATYPE());
    assertEquals("3.1", saved.getMetadata().getFORMATVERSION());
    assertEquals("20251216052621", saved.getMetadata().getDATEUPDATE());
    assertEquals("IF", saved.getMetadata().getDATACENTRE());
    assertEquals("5907141", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals("BAD", saved.getMetadata().getPINAME());
    assertEquals("844", saved.getMetadata().getWMOINSTTYPE());
    assertEquals(0, saved.getErrors().getNumber());
    assertEquals(Collections.emptyList(), saved.getErrors().getErrors());
    assertEquals(1, saved.getWarnings().getNumber());
    assertEquals(Collections.singletonList("PI_NAME : 'BAD' Status: Invalid (not in NVS R40 table)"), saved.getWarnings().getWarnings());
  }

  private static void assertEquals5907141_meta_BadSpaceCaractere() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("5907141_meta_BadSpaceCaractere.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0006/5907141_meta_BadSpaceCaractere.nc", saved.getFile());
    assertEquals("FILE-ACCEPTED", saved.getStatus());
    assertEquals("DATA-VALIDATION", saved.getPhase());
    assertEquals("coriolis", saved.getMetadata().getDac());
    assertEquals("Argo meta-data", saved.getMetadata().getDATATYPE());
    assertEquals("3.1", saved.getMetadata().getFORMATVERSION());
    assertEquals("20251216052621", saved.getMetadata().getDATEUPDATE());
    assertEquals("IF", saved.getMetadata().getDATACENTRE());
    assertEquals("5907141", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals("Fabrizio D'ORTENZIO", saved.getMetadata().getPINAME());
    assertEquals("844", saved.getMetadata().getWMOINSTTYPE());
    assertEquals(0, saved.getErrors().getNumber());
    assertEquals(Collections.emptyList(), saved.getErrors().getErrors());
    assertEquals(1, saved.getWarnings().getNumber());
    assertEquals(Collections.singletonList("PI_NAME : 'Fabrizio D'ORTENZIO' Status: Invalid (not in NVS R40 table)"),
        saved.getWarnings().getWarnings());
  }

  private static void assertEquals5907141_meta_multipleName() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("5907141_meta_multipleName.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0006/5907141_meta_multipleName.nc", saved.getFile());
    assertEquals("FILE-ACCEPTED", saved.getStatus());
    assertEquals("DATA-VALIDATION", saved.getPhase());
    assertEquals("coriolis", saved.getMetadata().getDac());
    assertEquals("Argo meta-data", saved.getMetadata().getDATATYPE());
    assertEquals("3.1", saved.getMetadata().getFORMATVERSION());
    assertEquals("20251216052621", saved.getMetadata().getDATEUPDATE());
    assertEquals("IF", saved.getMetadata().getDATACENTRE());
    assertEquals("5907141", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals("Sabrina SPEICH, Michel ARHAN", saved.getMetadata().getPINAME());
    assertEquals("844", saved.getMetadata().getWMOINSTTYPE());
    assertEquals(0, saved.getErrors().getNumber());
    assertEquals(Collections.emptyList(), saved.getErrors().getErrors());
    assertEquals(0, saved.getWarnings().getNumber());
    assertEquals(Collections.emptyList(), saved.getWarnings().getWarnings());
  }

  private static void assertEqualsD4900757_024_CNDC_with_ADJUSTED_version_3() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("D4900757_024_CNDC_with_ADJUSTED_version-3.0_DMode-A.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0005/D4900757_024_CNDC_with_ADJUSTED_version-3.0_DMode-A.nc", saved.getFile());
    assertEquals("FILE-ACCEPTED", saved.getStatus());
    assertEquals("FORMAT-VERIFICATION", saved.getPhase());
    assertEquals("bodc", saved.getMetadata().getDac());
    assertEquals("Argo profile", saved.getMetadata().getDATATYPE());
    assertEquals("3.0", saved.getMetadata().getFORMATVERSION());
    assertEquals("20191028121515", saved.getMetadata().getDATEUPDATE());
    assertEquals("AO", saved.getMetadata().getDATACENTRE());
    assertEquals("4900757", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals("BRECK OWENS", saved.getMetadata().getPINAME());
    assertEquals("852", saved.getMetadata().getWMOINSTTYPE());
    assertEquals("A", saved.getMetadata().getDATAMODE());
    assertEquals("A", saved.getMetadata().getDIRECTION());
    assertEquals(1, saved.getMetadata().getNPROF());
    assertEquals(76, saved.getMetadata().getNLEVELS());
    assertEquals("024", saved.getMetadata().getCYCLENUMBER());
    assertEquals("19.6310", saved.getMetadata().getLATITUDE());
    assertEquals("-43.7750", saved.getMetadata().getLONGITUDE());
    assertEquals("20070126124001", saved.getMetadata().getJULDDtg());
    assertEquals("1", saved.getMetadata().getJULDQC());
    assertEquals("1", saved.getMetadata().getPOSITIONQC());
    assertEquals("F", saved.getMetadata().getPROFILETEMPQC());
    assertEquals("F", saved.getMetadata().getPROFILEPSALQC());
    assertEquals("null", saved.getMetadata().getPROFILEDOXYQC());
    assertEquals("PRES TEMP PSAL CNDC", saved.getMetadata().getSTATIONPARAMETERS());
    assertEquals("----", saved.getMetadata().getPARAMETERDATAMODE());
    assertEquals("99999.0000", saved.getMetadata().getPsalAdjMean());
    assertEquals("99999.0000", saved.getMetadata().getPsalAdjSdev());
    assertEquals(0, saved.getErrors().getNumber());
    assertEquals(Collections.emptyList(), saved.getErrors().getErrors());
    assertEquals(0, saved.getWarnings().getNumber());
    assertEquals(Collections.emptyList(), saved.getWarnings().getWarnings());
  }

  private static void assertEqualsD4900757_024_CNDC_without_ADJUSTED_version_3() throws JAXBException, IOException {
    FileCheckResults saved = readFile(outputDir.resolve("D4900757_024_CNDC_without_ADJUSTED_version-3.0_DMode-A.nc.filecheck"));
    assertEquals("2.2.2", saved.getSpecVersion());
    assertEquals("1.1.1", saved.getFilecheckerVersion());
    assertEquals("src/test/netcdf-test-files/TEST_ALL_0005/D4900757_024_CNDC_without_ADJUSTED_version-3.0_DMode-A.nc", saved.getFile());
    assertEquals("FILE-REJECTED", saved.getStatus());
    assertEquals("FORMAT-VERIFICATION", saved.getPhase());
    assertEquals("bodc", saved.getMetadata().getDac());
    assertEquals("Argo profile", saved.getMetadata().getDATATYPE());
    assertEquals("3.0", saved.getMetadata().getFORMATVERSION());
    assertEquals("20191028121515", saved.getMetadata().getDATEUPDATE());
    assertEquals("AO", saved.getMetadata().getDATACENTRE());
    assertEquals("4900757", saved.getMetadata().getPLATFORMNUMBER());
    assertEquals("BRECK OWENS", saved.getMetadata().getPINAME());
    assertEquals("852", saved.getMetadata().getWMOINSTTYPE());
    assertEquals("A", saved.getMetadata().getDATAMODE());
    assertEquals("A", saved.getMetadata().getDIRECTION());
    assertEquals("F", saved.getMetadata().getPROFILETEMPQC());
    assertEquals("F", saved.getMetadata().getPROFILEPSALQC());
    assertEquals("null", saved.getMetadata().getPROFILEDOXYQC());
    assertEquals("PRES TEMP PSAL CNDC", saved.getMetadata().getSTATIONPARAMETERS());
    assertEquals(1, saved.getErrors().getNumber());
    assertEquals(Collections.singletonList("Parameter group CNDC: Variables are missing for this group\n"
        + "\tRequired variables: 'CNDC_ADJUSTED' 'CNDC' 'CNDC_ADJUSTED_ERROR' 'PROFILE_CNDC_QC' 'CNDC_QC' 'CNDC_ADJUSTED_QC' \n"
        + "\tMissing variables:  'CNDC_ADJUSTED' 'CNDC_ADJUSTED_ERROR' 'CNDC_ADJUSTED_QC' "), saved.getErrors().getErrors());
    assertEquals(0, saved.getWarnings().getNumber());
    assertEquals(Collections.emptyList(), saved.getWarnings().getWarnings());
  }

  private static FileCheckResults readFile(Path path) throws IOException, JAXBException {
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      return (FileCheckResults) JAXBContext.newInstance(FileCheckResults.class).createUnmarshaller().unmarshal(reader);
    }
  }

  private static class TestContextBuilder {


    private String dacName;
    private boolean doNulls = false;
    private boolean doFormatOnly = false;
    private boolean doNameCheck = true;
    private boolean doPsalStats = false;
    private boolean doFormatOnlyPre31 = true;
    private Path inDir;

    public TestContextBuilder withDacName(String dacName) {
      this.dacName = dacName;
      return this;
    }

    public TestContextBuilder withDoNulls(boolean doNulls) {
      this.doNulls = doNulls;
      return this;
    }

    public TestContextBuilder withDoFormatOnly(boolean doFormatOnly) {
      this.doFormatOnly = doFormatOnly;
      return this;
    }

    public TestContextBuilder withDoNameCheck(boolean doNameCheck) {
      this.doNameCheck = doNameCheck;
      return this;
    }

    public TestContextBuilder withDoPsalStats(boolean doPsalStats) {
      this.doPsalStats = doPsalStats;
      return this;
    }

    public TestContextBuilder withDoFormatOnlyPre31(boolean doFormatOnlyPre31) {
      this.doFormatOnlyPre31 = doFormatOnlyPre31;
      return this;
    }

    public TestContextBuilder withInDir(Path inDir) {
      this.inDir = inDir;
      return this;
    }

    public TestContext build() {
      return new TestContext(dacName, doNulls, doFormatOnly, doNameCheck, doPsalStats, doFormatOnlyPre31, inDir);
    }
  }

  private static class TestContext implements ArgoFileCheckExecutorContext {

    public static TestContextBuilder builder() {
      return new TestContextBuilder();
    }

    private final String dacName;
    private final boolean doNulls;
    private final boolean doFormatOnly;
    private final boolean doNameCheck;
    private final boolean doPsalStats;
    private final boolean doFormatOnlyPre31;
    private final Path inDir;


    private TestContext(String dacName, boolean doNulls, boolean doFormatOnly, boolean doNameCheck, boolean doPsalStats, boolean doFormatOnlyPre31,
        Path inDir) {
      this.dacName = dacName;
      this.doNulls = doNulls;
      this.doFormatOnly = doFormatOnly;
      this.doNameCheck = doNameCheck;
      this.doPsalStats = doPsalStats;
      this.doFormatOnlyPre31 = doFormatOnlyPre31;
      this.inDir = inDir;
    }


    @Override
    public String getDacName() {
      return dacName;
    }

    @Override
    public boolean isUseOnlineNVS() {
      return false;
    }

    @Override
    public Path getSpecDirName() {
      return Paths.get("../file_checker_spec");
    }

    @Override
    public boolean isDoNulls() {
      return doNulls;
    }

    @Override
    public boolean isDoFormatOnly() {
      return doFormatOnly;
    }

    @Override
    public boolean isDoNameCheck() {
      return doNameCheck;
    }

    @Override
    public boolean isDoPsalStats() {
      return doPsalStats;
    }

    @Override
    public boolean isDoFormatOnlyPre31() {
      return doFormatOnlyPre31;
    }

    @Override
    public Path getInDir() {
      return inDir;
    }

    @Override
    public Path getOutDir() {
      return outputDir;
    }

    @Override
    public boolean isDoXml() {
      return true;
    }

    @Override
    public boolean isUseInternalSpecs() {
      return false;
    }

    @Override
    public ApplicationProperties getApplicationProperties() {
      return new ApplicationProperties() {
        @Override
        public String getFcVersion() {
          return "1.1.1";
        }

        @Override
        public String getNvsBaseUrl() {
          return ApplicationProperties.NVS_DEFAULT_BASE_URL;
        }
      };
    }

    @Override
    public VersionInfoProperties getVersionInfoProperties() {
      return new VersionInfoProperties() {

        @Override
        public String getSpVersion() {
          return "2.2.2";
        }
      };
    }
  }

}