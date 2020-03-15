package com.sourceclear.agile.piplanning.service.objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
@JsonDeserialize(builder = UserRegistration.Builder.class)
public interface UserRegistration {

  String email();

  String name();

  String password();

  class Builder extends UserRegistration_Builder {
  }
}
