package com.sourceclear.agile.piplanning.service.configs;

import com.sourceclear.agile.piplanning.service.components.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Enables more run-time {@code Origin} checking than does the default {@link #CorsConfiguration}.
 */
@Component
@ManagedResource
public class DynamicCORSConfiguration extends CorsConfiguration {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicCORSConfiguration.class);

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final AppProperties appProperties;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  public DynamicCORSConfiguration(AppProperties appProperties) {
    this.appProperties = appProperties;

    final List<String> fixedOrigins = new ArrayList<>();
    final List<String> additionalOrigins = this.appProperties.getAdditionalAllowedOrigins();

    if (additionalOrigins != null) {
      LOGGER.info("Additional allowed origins: {}", additionalOrigins);
      fixedOrigins.addAll(new HashSet<>(additionalOrigins));
    }
    setAllowedOrigins(fixedOrigins);
    setAllowedMethods(List.of("*"));
    setAllowedHeaders(List.of("*"));
    setAllowCredentials(true);
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------

}
