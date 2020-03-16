# Architecture

<!--

digraph G {
  fe [label = "front end", shape = "box"];
  be [label = "app server", shape = "box"];
  solver [label = "solver", shape = "box"];
  db [label = "database", style = "bold"];
  fe -> be;
  be -> solver [style = "dotted"];
  be -> db;
}

pbpaste | docker run -i --rm tsub/graph-easy --as=boxart | pbcopy

-->

Sapling has a very standard client-server architecture. The client is a SPA that is [deployed](frontend.md) separately.

```
               ┌────────────┐
               │ front end  │
               └────────────┘
                 │
                 │
                 ▼
┌────────┐     ┌────────────┐
│ solver │ ◀·· │ app server │
└────────┘     └────────────┘
                 │
                 │
                 ▼
               ┏━━━━━━━━━━━━┓
               ┃  database  ┃
               ┗━━━━━━━━━━━━┛
```


The solver can either be run as a separate [service](../solver) (so it can be scaled up independently, in which case set `SOLVER_REMOTE` and `SOLVER_URI`) or on the [application server](../server) itself.
