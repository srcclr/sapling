package com.sourceclear.agile.piplanning.service.objects;

/**
 * The types of database we support.
 */
public enum DatabaseType {
  POSTGRESQL("postgresql"),
  ;

  private final String name;

  DatabaseType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
