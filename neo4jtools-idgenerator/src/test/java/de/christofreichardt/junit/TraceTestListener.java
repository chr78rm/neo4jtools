/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.junit;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.io.InputStream;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author Christof Reichardt
 */
public class TraceTestListener extends RunListener implements Traceable {

  @Override
  public void testFailure(Failure failure) throws Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "testFailure(Failure failure)");

    try {
      super.testFailure(failure);
      tracer.out().printfIndentln("%s[failure.getException() = %s]", failure.getDescription().getDisplayName(), failure.getException());
    }
    finally {
      tracer.wayout();
    }
  }

  @Override
  public void testFinished(Description description) throws Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "testFinished(Description description)");

    try {
      tracer.out().printfIndentln("description.getDisplayName() = %s", description.getDisplayName());
      traceMemory();
      
      super.testFinished(description);
    }
    finally {
      tracer.wayout();
    }
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    try {
      AbstractTracer tracer = getCurrentTracer();
      tracer.entry("void", this, "testRunFinished(Result result)");
      
      try {
        super.testRunFinished(result);
        traceResults(result);
      }
      finally {
        tracer.wayout();
      }
    }
    finally {
      TracerFactory.getInstance().closePoolTracer();
    }
  }
  
  private void traceResults(Result result) {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "traceResults(Result result)");

    try {
      tracer.out().printfIndentln("result.getRunCount() = %d", result.getRunCount());
      tracer.out().printfIndentln("result.getFailureCount() = %d", result.getFailureCount());
      tracer.out().printfIndentln("result.getRunTime() = %dms", result.getRunTime());
      
      System.out.printf("%nresult.getRunCount() = %d%n", result.getRunCount());
      System.out.printf("result.getFailureCount() = %d%n", result.getFailureCount());
      System.out.printf("result.getRunTime() = %dms%n%n", result.getRunTime());
      
      if (result.wasSuccessful()) {
        tracer.logMessage(LogLevel.INFO, "Test run has been successful.", getClass(), "traceResults(Result result)");
      }
      else {
        tracer.logMessage(LogLevel.WARNING, "Test run has failed.", getClass(), "traceResults(Result result)");
      }

      result.getFailures().forEach((Failure failure) -> {
        tracer.out().printIndentln("+-----------------------------------------------------------------------------------------------------+");
        tracer.out().printfIndentln("%s[failure.getException() = %s]", failure.getDescription().getDisplayName(), failure.getException());
        
        Throwable throwable = failure.getException();
        do {
          StackTraceElement[] stackTraceElements = throwable.getStackTrace();
          for (StackTraceElement stackTraceElement : stackTraceElements) {
            tracer.out().printfIndentln("  at %s.%s(%s:%d)", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getFileName(), stackTraceElement.getLineNumber());
          }
          throwable = throwable.getCause();
          if (throwable != null)
            tracer.out().printfIndentln("caused by = %s: %s", throwable.getClass().getName(), throwable.getMessage());
        } while(throwable != null);
      });
    }
    finally {
      tracer.wayout();
    }
  }

  @Override
  public void testRunStarted(Description description) throws Exception {
    initTracerFactory();
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "testRunStarted(Description description)");

    try {
      traceDescription(description);
      super.testRunStarted(description);
    }
    finally {
      tracer.wayout();
    }
  }
  
  private void initTracerFactory() throws TracerFactory.Exception {
    TracerFactory.getInstance().reset();
    InputStream resourceAsStream = MySuite.class.getClassLoader().getResourceAsStream("de/christofreichardt/junit/TraceConfig.xml");
    if (resourceAsStream != null) {
      TracerFactory.getInstance().readConfiguration(resourceAsStream);
    }
    TracerFactory.getInstance().openPoolTracer();
    TracerFactory.getInstance().getCurrentPoolTracer().initCurrentTracingContext();
  }

  @Override
  public void testStarted(Description description) throws Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "testStarted(Description description)");

    try {
      tracer.logMessage(LogLevel.INFO, "'" + description.getDisplayName() + "' started ...", getClass(), "testStarted(Description description)");
      
      System.runFinalization();
      System.gc();
      traceMemory();
      
      super.testStarted(description);
    }
    finally {
      tracer.wayout();
    }
  }

  private void traceDescription(Description description) {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "traceDescription(Description description)");

    try {
      tracer.out().printfIndentln("description.getDisplayName() = %s", description.getDisplayName());
      if (description.isSuite()) {
        tracer.out().printfIndentln("description.testCount() = %d", description.testCount());

        for (Description child : description.getChildren()) {
          traceDescription(child);
        }
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  private void traceMemory() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "traceMemory()");

    try {
      tracer.out().printfIndentln("Free: %d KB", Runtime.getRuntime().freeMemory()/1024);
      tracer.out().printfIndentln("Total: %d KB", Runtime.getRuntime().totalMemory()/1024);
      tracer.out().printfIndentln("Max: %d KB", Runtime.getRuntime().maxMemory()/1024);
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getCurrentPoolTracer();
  }
}
