package com.sourceclear.agile.piplanning.service.components;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@Component
@ManagedResource
@ConfigurationProperties(prefix = "props")
public class AppProperties {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @NotNull
  private String jwtPublicKey;

  @NotNull
  private String jwtPrivateKey;

  private List<String> additionalAllowedOrigins;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

  public String getJwtPublicKey() {
    return jwtPublicKey;
  }

  public void setJwtPublicKey(String jwtPublicKey) {
    this.jwtPublicKey = jwtPublicKey;
  }

  public String getJwtPrivateKey() {
    return jwtPrivateKey;
  }

  public void setJwtPrivateKey(String jwtPrivateKey) {
    this.jwtPrivateKey = jwtPrivateKey;
  }

  public List<String> getAdditionalAllowedOrigins() {
    return additionalAllowedOrigins;
  }

  public void setAdditionalAllowedOrigins(List<String> additionalAllowedOrigins) {
    this.additionalAllowedOrigins = additionalAllowedOrigins;
  }
}
