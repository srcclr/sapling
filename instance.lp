
%% instance

% sprint(1..1).
% task(1..2).
% epic(1..2).
task(2).
sprint(1).
epic(1).

% specify tasks and their dependencies
task_weight(2,1).

% task_depends_on(1,2).

% specify sprint capacities, including reductions from leave and uncertainty
sprint_capacity(1,1).

% hard constraint: we always want task 1 in sprint 2
% :- not assign(1,2).

% specify epics
epic_priority(1,5).

% associate epics and tasks
epic_task(1,2).

