# Requirements

Questions and answers captured during requirements clarification.

---

## Q1

**Q:** The architecture document mentions creating a "standalone repository" (`navigation-interop-lab`), but also says "Create in current repository test project." Which approach do you want -- a completely separate Git repository, or a new module/directory within the existing `nav2-nav3-migration` repo?

**A:** The current `nav2-nav3-migration` repo is already the standalone repo. Build the lab project directly inside it -- no need for a separate repository.

---

## Q2

**Q:** What minimum Android SDK version and Kotlin version should the lab target? And do you have a preference for the Gradle version / AGP version?

**A:** minSdk = 24. No specific preference stated for Kotlin, Gradle, or AGP versions -- use latest stable.

---

## Q3

**Q:** The architecture mentions Koin DI as "(optional)." Do you want to use Koin for dependency injection in the lab, or keep it simpler with manual DI / no framework?

**A:** Yes, use Koin as DI.

---

## Q4

**Q:** For the Nav3 dependency -- Navigation 3 is currently in alpha (`androidx.navigation3:*`). Are you okay using the latest alpha version, and do you have a specific version in mind? Also, should the lab use the Compose Material 3 integration for Nav3, or just the core Nav3 APIs?

**A:** Yes, use the latest alpha version. No specific version pinned -- use whatever is latest stable alpha. (Material 3 integration not explicitly addressed -- default to including it for completeness.)

---

## Q5

**Q:** The architecture defines 4 milestones (M1-M4). Do you want to implement all milestones in this planning cycle, or focus on a specific milestone first (e.g., M1 -- repo boots, case browser, T1/T2/T3 topologies)?

**A:** Implement all milestones (M1-M4).

---

## Q6

**Q:** For the case browser UI -- should it be a simple list-based Compose screen, or do you want something more elaborate (e.g., grouped by case family A-H, filtering, search, run status indicators)?

**A:** Use a simple list-based Compose screen.

---

## Q7

**Q:** For the live timeline panel (trace viewer showing container changes, stack changes, fragment transactions, etc.) -- should this be a separate screen navigated to after running a case, or an inline panel visible during case execution (e.g., bottom sheet or split view)?

**A:** Inline panel visible during case execution.

---

## Q8

**Q:** The architecture mentions three run modes: manual, scripted, and stress. For scripted mode -- should scenarios auto-advance through steps with configurable delays, or do you have a different mechanism in mind? And for stress mode -- is this rapid repeated execution of the same scenario to detect race conditions?

**A:** Yes to both. Scripted mode auto-advances with configurable delays. Stress mode is rapid repeated execution to detect race conditions.

---

## Q9

**Q:** For the `LabTraceStore` -- should trace data persist across app restarts (e.g., saved to local DB or file), or is in-memory storage sufficient (traces lost on app kill)?

**A:** In-memory storage is sufficient.

---

## Q10

**Q:** For the `SOURCE_SNAPSHOT.md` sync process and `tools/sync/refresh_inventory.sh` -- since the lab is built inside this repo (not separate), do you still need a sync mechanism with the production repo? If yes, what's the production repo path or URL for the script to reference?

**A:** No, not needed. Skip the sync mechanism (`SOURCE_SNAPSHOT.md`, `tools/sync/`, `sync-with-source-repo.md`).

---

## Q11

**Q:** For CI -- the architecture mentions a "CI smoke pipeline" in M4. Do you want to set up GitHub Actions (or another CI) as part of this plan, or is CI out of scope for now?

**A:** Yes, set up GitHub Actions CI.

---

## Q12

**Q:** For the fake screens used in test cases -- should they be minimal (colored boxes with labels identifying the screen/route), or do you want them to include basic interactive elements (buttons, text fields) to simulate real user flows like form submission or result passing?

**A:** Minimal -- colored boxes with labels. No interactive form elements needed.

---

## Q13

**Q:** For the invariant checks (pass/fail per case) -- should failures be displayed only in the inline trace panel, or also produce system notifications / logcat output / test report files for easier debugging?

**A:** Also produce logcat output (in addition to the inline trace panel).

---
