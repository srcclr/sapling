%% encoding

#script (python)

# https://potassco.org/clingo/python-api/5.4/
from clingo import Number

def minus(a, b):
  return Number(a.number - b.number)

#end.

% the unassigned sprint
sprint(unassigned).

% total per sprint is the sum of all tasks for that sprint
sprint_total(S,To) :- To = #sum { W,T : task_weight(T,W), assign(T,S) }, sprint(S).

% each task is assigned to exactly one sprint
{ assign(T,S) : sprint(S) } = 1 :- task(T).

% sprint total cannot exceed capacity (except if it's the unassigned sprint, which has unlimited capacity)
:- sprint_total(S,A), sprint(S), sprint_capacity(S,E), A > E, S != unassigned.

% tasks cannot appear before dependencies
:- assign(T1,S1), assign(T2,S2), task_depends_on(T1,T2), S1 < S2.

% this counts the number of violations of epic priority, where a task is assigned to an
% earlier sprint than one in a higher-priority epic.
% it's optimization instead of integrity constraints because we don't want to unassign
% tasks because of bad priority; we should just do our best in that case.
epic_priority_violations(C) :- C = #count { T1,T2 :
  task(T1), sprint(S1), assign(T1,S1), epic_task(E1,T1), epic_priority(E1,P1),
  task(T2), sprint(S2), assign(T2,S2), epic_task(E2,T2), epic_priority(E2,P2),
  % if either task is unassigned, this shouldn't apply
  S1 != unassigned, S2 != unassigned,
  S1 > S2,
  % priority numbers are reversed, so lower = more urgent
  P1 < P2
}.
#minimize { C@2 : epic_priority_violations(C) }.
% might be useful for explainability
% #show epic_priority_violations/1.

% this is the distance between dependent tasks. we want to make this as small as possible
% to preserve context. this is less significant than dependencies_in_same_sprint because
% that tries to increase the distance; we want to give it a lower bound before minimizing
dependency_distance(S) :-
  S = #sum { D,T1,T2 :
  D = @minus(S1, S2), % will be positive, since T1 -> T2 implies that T1 is in a later (higher) sprint. TODO might need to ensure this on the back end
  task_depends_on(T1,T2), assign(T1,S1), assign(T2,S2),
  task(T1), task(T2), sprint(S1), sprint(S2),
  S1 != unassigned, S2 != unassigned
}
.

#minimize { S@3 : dependency_distance(S) }.
% #show dependency_distance/1.

% this maximises parallelism by moving dependencies to different sprints if possible.
% if a task has a dependency in the same sprint, it's taking up capacity but can't be
% done in parallel.

dependencies_in_same_sprint(C) :- C = #count { T1,T2 :
  task_depends_on(T1,T2), assign(T1,S), assign(T2,S),
  task(T1), task(T2), sprint(S), S != unassigned
}.
#minimize { C@4 : dependencies_in_same_sprint(C) }.
% #show dependencies_in_same_sprint/1.

% the above is very slow. instead try to maximise the number of non-dependent stuff in each sprint

% non_dependencies_in_same_sprint(C) :- C = #count { T1,T2 :
%   not task_depends_on(T1,T2), assign(T1,S), assign(T2,S),
%   task(T1), task(T2), sprint(S), S != unassigned
% }.
% #maximize { C@4 : dependencies_in_same_sprint(C) }.


% TODO prioritise earlier sprints over later ones


unassigned_count(C) :- C = #count { T : assign(T,unassigned) }.
#minimize { C@5 : unassigned_count(C) }.

#show assign/2.
#show sprint_total/2.
