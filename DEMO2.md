# Loadouts Demo 2

Use two browser profiles or a regular and private window. Sign in as the owner in one and as a different user in the other. Leave the test report and issue log open before the demo.

## Functional tests (40 points)

| Requirement and case | Show | Expected | Observed in verification |
| --- | --- | --- | --- |
| Create and protect a draft, then publish (Loadout.Create, Loadout.View, SE-3; L01, L04, L09) | Create a named draft with a product. Copy its URL into the other window. Publish it, search for it, and reopen the URL. | The other user cannot see the draft. After publish the user can view it, but cannot update it. | Edge showed an unavailable draft and a visible published loadout. The API suite also verified owner-only changes and a 403 for a non-owner edit after publication. |
| Display missing and zero prices, then update totals (Price.Missing, Price.Total.Update; PR01–PR03) | Create a loadout with Mixer at $24.50, a free sample at $0 and a spoon without a price. Edit the spoon to $10.25, then remove the mixer. | The first subtotal is $24.50 with one unknown; after the edit the total is $34.75; after removal it is $10.25. | The Spring API test verified all three totals. Edge separately showed the missing price and known subtotal for a zero-price item. |
| Community and privacy (Social.Like.Add, Bookmark.View, Profile.Status, SE-2; C01, C05, C07, A08) | With the second account, like and bookmark the published loadout, then comment and review. Set the owner profile to private; reopen the other account's bookmarks. | Actions save for the signed-in user. When the owner goes private, the profile and loadout are hidden and the bookmark stops appearing. | Edge showed successful community actions and the bookmark disappeared after privacy was enabled. The API test rejected anonymous community writes. |

Use local disposable data. If the demo browser or dataset changes, rehearse these flows and describe what you observe during the presentation.

## Code coverage (30 points)

From the repository root, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\mvnw-demo.ps1 -Test
```

Open target/site/jacoco/index.html. The November 14 run executed 16 backend integration tests across AuthAndLoadoutIntegrationTest, LoadoutValidationTest, ProductIntegrationTest, and Demo2IntegrationTest. It reported 319 of 333 Java lines (95.8%) and 169 of 237 branches (71.3%). Use the freshly generated counts if they differ.

Line coverage tells you how many executable source lines ran. Branch coverage tells you which alternatives in decisions ran. Open the loadout package and show a covered authorization decision and a missed branch. Reported coverage comes from the Java backend suite and its in-memory H2 database; it does not measure React, prove every expected behavior, or validate a production deployment. The Edge browser check ran separately against the Vite proxy and local demo profile.

## Issue tracking (20 points)

Open ISSUES.md and show LO-01 (session indicator disappearing after reload): observed behavior, reproduction, expected behavior, fix, and verification. Then point to LO-03 to distinguish closed work from an open release check. If your team uses a shared tracker, you can present an equivalent real issue there.

## Presentation order (10 points)

Start with the user-facing tests, then show the terminal and coverage report, then show the issue record. For every test, say what requirement it checks, what should happen, and what actually happened. Keep later features separate: loadout reporting, stronger email format rules, automatic retailer data import, and the tutorial are beyond Demo 2.

