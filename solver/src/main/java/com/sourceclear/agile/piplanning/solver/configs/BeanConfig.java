package com.sourceclear.agile.piplanning.solver.configs;

import com.sourceclear.agile.piplanning.service.services.ClingoService;
import com.sourceclear.agile.piplanning.service.services.ClingoServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

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
