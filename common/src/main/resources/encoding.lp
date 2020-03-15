%% encoding

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

% epic priority, i.e. tickets which belong to higher-priority epics should be assigned "first".
% the key idea is that epic priority only makes a difference when we can't schedule all tickets
% (otherwise everything gets done anyway). in that case, given an unassigned ticket, there cannot
% be a ticket from a lower-priority epic that is assigned over it.
:- task(T1), assign(T1,unassigned), epic_task(E1,T1), epic_priority(E1,P1),
  task(T2), assign(T2,S), sprint(S), S != unassigned, epic_task(E2,T2), epic_priority(E2,P2), P1 < P2.

unassigned_count(D) :- D = #count { T : assign(T,unassigned) }.
#minimize { D : unassigned_count(D) }.

#show assign/2.
#show sprint_total/2.
