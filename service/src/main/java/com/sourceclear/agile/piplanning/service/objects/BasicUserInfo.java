package com.sourceclear.agile.piplanning.service.objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

import java.util.Optional;

@FreeBuilder
@JsonDeserialize(builder = BasicUserInfo.Builder.class)
public interface BasicUserInfo {

  long id();

  String email();

  String firstName();

  Optional<String> lastName();

  class Builder extends BasicUserInfo_Builder {
  }
}
