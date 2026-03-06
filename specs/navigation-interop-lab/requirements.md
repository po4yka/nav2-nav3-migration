# Requirements

Questions and answers captured during requirements clarification.

This file is historical. It records what was decided while shaping the lab, not the best path for onboarding today.

Use these documents first for current work:

- [MIGRATION_RESEARCH_GUIDE.md](../../MIGRATION_RESEARCH_GUIDE.md)
- [README.md](../../README.md)
- [navigation_interop_lab_architecture.md](../../navigation_interop_lab_architecture.md)

## Current Implementation Status

Current repository status:

- lab implemented directly inside this repository
- milestones `M1-M5` complete
- scenario coverage: `A-H` (76) plus `R01-R25` (25)
- toolchain in use: Gradle wrapper `9.4.0`, AGP `9.1.0`, Kotlin `2.3.10`
- CI instrumentation smoke workflow active at `.github/workflows/android-instrumentation-smoke.yml`

## What This File Is Useful For

- understanding why specific implementation constraints were chosen
- checking whether a confusing architectural detail was an intentional requirement
- confirming which ideas were explicitly rejected during planning

---

## Q1

**Q:** The architecture document mentions creating a "standalone repository" (`navigation-interop-lab`), but also says "Create in current repository test project." Which approach was chosen?

**A:** The current `nav2-nav3-migration` repo is already the standalone repo. Build the lab project directly inside it -- no need for a separate repository.

---

## Q2

**Q:** What minimum Android SDK version and Kotlin version should the lab target, and was there a Gradle or AGP preference?

**A:** minSdk = 24. No specific preference stated for Kotlin, Gradle, or AGP versions -- use latest stable.

---

## Q3

**Q:** The architecture mentions Koin DI as "(optional)." Was Koin selected?

**A:** Yes, use Koin as DI.

---

## Q4

**Q:** What Nav3 dependency choice was made, and should Material 3 integration be included?

**A:** Yes. Nav3 1.0.1 (stable) is now used. Material 3 integration included for completeness.

---

## Q5

**Q:** The architecture initially defined `M1-M4`. Was the intent to implement all milestones or phase them?

**A:** Implement all milestones (M1-M4).

---

## Q6

**Q:** How simple should the case browser UI be?

**A:** Use a simple list-based Compose screen.

---

## Q7

**Q:** Where should the live trace and timeline panel appear?

**A:** Inline panel visible during case execution.

---

## Q8

**Q:** How were `scripted` and `stress` modes intended to behave?

**A:** Yes to both. Scripted mode auto-advances with configurable delays. Stress mode is rapid repeated execution to detect race conditions.

---

## Q9

**Q:** Should `LabTraceStore` persist across app restarts?

**A:** In-memory storage is sufficient.

---

## Q10

**Q:** Was any sync mechanism with a production repo kept?

**A:** No, not needed. Skip the sync mechanism (`SOURCE_SNAPSHOT.md`, `tools/sync/`, `sync-with-source-repo.md`).

---

## Q11

**Q:** Was CI included in scope?

**A:** Yes, set up GitHub Actions CI.

---

## Q12

**Q:** How realistic should the fake screens be?

**A:** Minimal -- colored boxes with labels. No interactive form elements needed.

---

## Q13

**Q:** Where should invariant failures be reported?

**A:** Also produce logcat output (in addition to the inline trace panel).

---
