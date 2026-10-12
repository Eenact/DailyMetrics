---
name: team
description: Run the DailyMetrics agent team (project-manager, kmp-developer, android-developer, ios-developer, qa-tester, code-reviewer) on a feature, bug or backlog item. Use when the user says "/team <goal>", asks the team/manager to handle something, or wants a backlog item (DM-x) implemented end to end.
---

# Run the DailyMetrics team

Subagents cannot start other subagents, so **you (the main session) are the dispatcher** and the
`project-manager` agent is the planner. Follow this loop for the goal in `$ARGUMENTS`.

1. **Plan** — start `project-manager` with the goal (or backlog ID). Ask it for the task table, the
   dispatch order and open questions. If it lists open questions that only the user can answer, ask
   the user before going further.
2. **Build** — for each development task, in the order given, start the owner agent
   (`kmp-developer`, `android-developer`, `ios-developer`) with the task ID, its acceptance criteria
   and the relevant files. Run independent tasks in parallel; run dependent ones in sequence. Shared
   (common) work goes first.
3. **Test** — start `qa-tester` with what changed and which acceptance criteria to verify.
   If it reports FAIL, send the bugs back to the owning developer agent and repeat steps 2–3.
4. **Review** — start `code-reviewer` on the resulting diff. Send blocking findings back to the
   owning developer, then re-test.
5. **Close** — start `project-manager` again with the outcome so it updates `docs/BACKLOG.md`.
6. **Report to the user**: what was done, what was verified (with the commands that passed), what
   is still unverified (always mention iOS when on Windows), and the next suggested backlog item.

Keep each agent prompt self-contained: agents do not see this conversation. Do not commit or push
unless the user asked for it.
