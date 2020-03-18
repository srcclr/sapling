package com.sourceclear.agile.piplanning.service.configs;

import com.sourceclear.agile.piplanning.service.components.SolverProperties;
import com.sourceclear.agile.piplanning.service.objects.DatabaseType;
import com.sourceclear.agile.piplanning.service.services.ClingoService;
import com.sourceclear.agile.piplanning.service.services.ClingoServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class BeanConfig {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(7);
  }

  @Bean
  public Map<DatabaseType, Properties> databaseProperties() throws IOException {
    final Map<DatabaseType, Properties> propertiesMap = new HashMap<>();

    // This file contains the properties required for schemacrawler to work well with postgres. As of this writing,
    // the only property defined is to make schemacrawler able to get all FK references and unique constraints.
    try (final InputStream inputStream = getClass().getResourceAsStream("/configs/pg.config.properties")) {
      final Properties props = new Properties();
      props.load(inputStream);
      propertiesMap.put(DatabaseType.POSTGRESQL, props);
    }

    return propertiesMap;
  }

  @Bean
  public ClingoService clingoService(SolverProperties solverProperties) {
    return new ClingoServiceImpl(solverProperties.getTimeout(), false);
  }

  @Bean
  public ClingoService clingoServiceNew(SolverProperties solverProperties) {
    return new ClingoServiceImpl(solverProperties.getTimeout(), true);
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

}
