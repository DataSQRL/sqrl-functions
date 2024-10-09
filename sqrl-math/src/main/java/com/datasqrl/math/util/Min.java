package com.datasqrl.math.util;

import com.google.auto.service.AutoService;
import org.apache.commons.math3.util.FastMath;
import org.apache.flink.table.functions.ScalarFunction;

/** Returns the minimum of two values. Returns null if any value is null. */
@AutoService(ScalarFunction.class)
public class Min extends ScalarFunction {
  public Integer eval(Integer a, Integer b) {
    if (a == null || b == null) return null;
    return FastMath.min(a, b);
  }

  public Long eval(Long a, Long b) {
    if (a == null || b == null) return null;
    return FastMath.min(a, b);
  }

  public Float eval(Float a, Float b) {
    if (a == null || b == null) return null;
    return FastMath.min(a, b);
  }

  public Double eval(Double a, Double b) {
    if (a == null || b == null) return null;
    return FastMath.min(a, b);
  }
}