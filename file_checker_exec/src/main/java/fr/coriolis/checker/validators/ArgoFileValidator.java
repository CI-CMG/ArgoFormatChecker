package fr.coriolis.checker.validators;

import fr.coriolis.checker.core.ValidationResult;
import java.io.IOException;

public interface ArgoFileValidator {

  boolean validateData(boolean singleCycle, String dacName, boolean ckNulls) throws IOException;

  boolean validateData(boolean ckNulls) throws IOException;

  boolean validateData(String dacName, boolean ckNulls) throws IOException;

  boolean validateFormat(String dacName);

  ValidationResult getValidationResult();

  void rudimentaryDateChecks();

  boolean validateGdacFileName();
}
