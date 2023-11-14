package io.github.linuxforhealth.hl7.resource;

import java.util.*;

import io.github.linuxforhealth.api.ResourceValue;

public class ResourceEvaluationResult {


  private List<ResourceValue> additionalResolveValues;
  private Map<String, Object> resolveValues;
  private PendingExpressionState pendingExpressions;


  public ResourceEvaluationResult(Map<String, Object> resolveValues) {
    this(resolveValues, new ArrayList<>(), null);
  }

  public ResourceEvaluationResult(Map<String, Object> resolveValues,
      List<ResourceValue> additionalResolveValues) {
    this(resolveValues, additionalResolveValues,
        PendingExpressionState.emptyPendingExpressionState());
  }

  public ResourceEvaluationResult(Map<String, Object> resolveValues,
      List<ResourceValue> additionalResolveValues, PendingExpressionState pendingExpressions) {
    this.additionalResolveValues = new ArrayList<>();
    this.additionalResolveValues.addAll(additionalResolveValues);
    this.resolveValues = new TreeMap<>();
    this.resolveValues.putAll(resolveValues);
    this.pendingExpressions = pendingExpressions;
  }



  public List<ResourceValue> getAdditionalResolveValues() {
    return additionalResolveValues;
  }


  public Map<String, Object> getResolveValues() {
    return resolveValues;
  }

  public PendingExpressionState getPendingExpressions() {
    return pendingExpressions;
  }

}
