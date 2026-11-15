# Loadouts issue log

This log tracks demo defects in the repository. Give each issue an ID, reproduction steps, expected behavior, status, and a verification reference. Update the same record when a fix is made and when it is verified. Keep unresolved issues open until there is evidence for closing them.

| ID | Issue | Status | Verification |
| --- | --- | --- | --- |
| LO-01 | Signed-in UI resets after a page refresh | Fixed and verified for Demo 2 | Backend session test and browser reload check |
| LO-02 | Missing product information could not be saved | Fixed and verified for Demo 2 | Product price and details test covers an item with no URL or price, and a separate item priced at zero |
| LO-03 | Browser support has not been checked across the full SRS matrix | Open | Verify Chrome, Safari, and Firefox and record results before Release 1.0 |

## LO-01 Signed-in UI resets after a page refresh

- Observed in Demo 1: sign in successfully, reload the page, and see Sign in even though the server session still exists.
- Expected: with a valid session, the signed-in controls return after refresh. After logout they stay unavailable.
- Fix: the frontend restores the account using GET /api/auth/me; login refreshes the session ID and logout invalidates the server session.
- Verification: Demo2IntegrationTest checks authenticated session lookup, session ID rotation and logout. The Demo 2 browser smoke check covers reloading while signed in and again after logout.
- Status history: observed during Demo 1; fixed in Demo 2; closed after verification.

LO-03 remains open until the cross-browser checks are recorded. A passing Java coverage run does not resolve it.

