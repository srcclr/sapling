package com.sourceclear.agile.piplanning.service.objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
@JsonDeserialize(builder = AuthRequest.Builder.class)
public interface AuthRequest {
  String email();

  String password();

  class Builder extends AuthRequest_Builder {
  }
}
