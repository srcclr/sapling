package com.sourceclear.agile.piplanning.service.services;


import com.sourceclear.agile.piplanning.objects.Fact;
import com.sourceclear.agile.piplanning.objects.Problem;
import com.sourceclear.agile.piplanning.objects.Soln;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

public interface ClingoService {
  Set<Soln> solve(Problem problem) throws IOException;

  Set<Soln> solveRemotely(URI uri, Problem problem) throws IOException;

  String solve(String instance) throws IOException;

  List<Fact> parse(String instance);
}
