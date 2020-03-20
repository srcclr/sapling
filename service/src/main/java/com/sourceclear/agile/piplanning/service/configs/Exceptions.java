/*
 * © Copyright 2020 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.configs;

import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Function;
import java.util.function.Supplier;

public class Exceptions {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * Dee ess ell
   */
  public static class E extends ResponseStatusException implements Supplier<ResponseStatusException>, Function<String, ResponseStatusException> {

    private final HttpStatus s;

    public E(HttpStatus s) {
      super(s);
      this.s = s;
    }

    @Override
    public ResponseStatusException get() {
      return this;
    }

    @Override
    public ResponseStatusException apply(String m) {
      return new ResponseStatusException(s, m);
    }
  }

  public static final E notFound = new E(HttpStatus.NOT_FOUND);
  public static final E badRequest = new E(HttpStatus.BAD_REQUEST);
  public static final E internalServerError = new E(HttpStatus.INTERNAL_SERVER_ERROR);
  public static final E unauthorized = new E(HttpStatus.UNAUTHORIZED);
  public static final E notAcceptable = new E(HttpStatus.NOT_ACCEPTABLE);

  @FunctionalInterface
  public interface SupplierE<T, E extends Exception> {
    T get() throws E;
  }

  public static <T, E extends Exception> T Try(SupplierE<T, E> thing, T defaultValue) {
    try {
      return thing.get();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

}
