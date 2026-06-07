# Volley Live Scores — Requirements v1

> WAVOC Volleyball Club · June 2026

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Admin — Team Management](#2-admin--team-management)
3. [Admin — Game Planning](#3-admin--game-planning)
4. [Game Scorer — Game List](#4-game-scorer--game-list)
5. [Game Scorer — Score Input](#5-game-scorer--score-input)
6. [Data Model](#6-data-model)
7. [Technical Decisions](#7-technical-decisions)
8. [Still To Build — v1 Completion](#8-still-to-build--v1-completion)

---

## 1. Project Overview

A live volleyball scoring application for WAVOC, a Belgian volleyball club. Replaces manual score tracking with a mobile-first scorer UI, an admin game planning interface, and a live dashboard for display screens.

Team master data is shared with the existing **newRanking** project — a daily ranking HTML generator that fetches standings from external league systems (VVB/Sporta).

**Tech stack:** Spring Boot 4 · Java 21 · MongoDB Atlas (free tier) · Thymeleaf + vanilla JS · Railway or Render (cloud hosting)

---

## 2. Admin — Team Management

### 2.1 User Stories

- *As an Admin I want to add a team so that new teams are available in live game scoring and ranking pages*
- *As an Admin I want to be able to edit teams so I can update team attributes*
- *As an Admin I want to soft-delete teams so they are not available anymore for new games to be planned*

### 2.2 Implementation Notes

- Team attributes: code (unique), name, league (VVB/KWB/Sporta), reeks, color (preset palette of 8 colors), sponsor 1 (name + logo path), sponsor 2 (name + logo path), active (boolean)
- Soft-deleted teams appear greyed out in the admin list with a **Reactivate** button
- Inactive teams are excluded from the game planning dropdown and from newRanking generation
- Color dropdown shows the selected color as its background for visual clarity
- When a team attribute is updated, the change is cascaded to all games where that team is embedded as home team

---

## 3. Admin — Game Planning

### 3.1 User Stories

- *As an Admin I want to see the list of games per day so I can manage the game schedule*
- *As an Admin I want to add a game for a specific date, time block, court, home team and away team so that scorers can find and score it*
- *As an Admin I want to set the away team color when creating a game so scorers do not have to configure it*
- *As an Admin I want that no duplicate games can be set on a court-timeblock combination*
- *As an Admin I want that no duplicate games can be set on a homeTeam-timeblock combination*
- *As an Admin I want to delete a game so I can correct planning mistakes*

### 3.2 Implementation Notes

- Time blocks: 10:00, 12:00, 14:00, 16:00, 18:00, 20:00 (enum)
- Courts: A1, A2, A3, B1, B2 (enum — hall A has 3 courts, hall B has 2)
- Away team is free text (not a system team)
- Away team color selected from preset palette at game creation
- Duplicate validation runs on both court+timeblock and homeTeam+timeblock per date
- Error message shown inline when duplicate detected, form values preserved
- Admin page links to scorer UI per game (Score ↗)

---

## 4. Game Scorer — Game List

### 4.1 User Stories

- *As a GameScorer I want to see the list of games with current status for today ordered by starting time and court so I can choose the game I will score*
- *As a GameScorer I want that the color of the font for home team is predefined on team level so that I do not have to set it each time*
- *As a GameScorer I want to see the current set score and current points in the active set for games in progress in the list of games so that I can evaluate everything is up to date*
- *As a GameScorer I want to see the final set score for games that have ended in the list of games so that I know what is finished*

### 4.2 Implementation Notes

- Game list at `/score` — auto-refreshes every 5 seconds via JS fetch
- Home team name shown in team color, away team name shown in away team color
- IN_PROGRESS games show: sets won (home-away, each in winner's color) `|` current set score
- FINISHED games show: final sets won score in team colors
- Cards centered with max-width, displayed as block elements to avoid whitespace rendering artifacts

---

## 5. Game Scorer — Score Input

### 5.1 User Stories

- *As a GameScorer I want the app to ask me the current set score, home team side and color away team when I start scoring a game that is scheduled so that I can set the initial starting point correctly even when I am late*
- *As a GameScorer I want that when I resume scoring a game that is already in progress that I see the current game, set score and team sides so that I can continue scoring the game*
- *As a GameScorer I want that I can easily add a point to the team that won a point so that current score can be updated easily*
- *As a GameScorer I want the app asks me for confirmation when the set would end based on set score so that I can not end a set by accident*
- *As a GameScorer I want the app to switch team sides when a new set starts so that I can score left to right as on the court*
- *As a GameScorer I want to see the court on my complete screen each side half the screen when in landscape*
- *As a GameScorer I want to see the code of the home team on the left side when home team is left so that side on my screen matches the real side on the court*
- *As a GameScorer I want to see per side the current set points in large and the set score smaller so that in one view I can see current set and game score*
- *As a GameScorer I want the app asks me for confirmation when the game would end based on match score so that I can not end a game by accident*
- *As a GameScorer I want the app to return to the game overview for today when I end a game so that I see the current status and can start scoring a next game*
- *As a GameScorer I want the app to clearly indicate when the server rejects my score because of a conflict and show me the current score from the server so that I can verify*
- *As a GameScorer I want an undo icon so I can revert the last point set when I made a mistake*

### 5.2 Implementation Notes

- Landscape-only UI at `/score/{id}` — rotate hint shown in portrait
- Dark navy background (`#0d2137`) with court lines (attack lines, outer border) at low opacity
- Vertical net line in center as divider; undo button (↩) centered on the net
- Each half tinted with team color (radial gradient overlay)
- Score numbers ~50% of screen height (44vh), team code in team color at top of each half
- Sets won shown at bottom flanking the net in team colors (no label text)
- Topbar: `← Games` | `time · court · Set N` | completed set scores
- Set history in topbar: home score - away score per set, entire score in the winner's color
- **Press-and-hold (600ms)** required to score a point — prevents accidental taps in a crowd
- Rising fill animation in team color from bottom during hold; releasing early cancels without scoring
- Set end confirmation: aligned to current court sides, team names in their color; current score unchanged on screen; confirmation shows the would-be score (e.g. 25-18)
- Game end confirmation: final set score aligned to court sides
- After game ends: redirect to game list after 1.5 seconds
- Conflict handling: HTTP 409 on version mismatch; red banner shown with server score; UI synced

### 5.3 Volleyball Rules Implemented

| Rule | Detail |
|------|--------|
| Sets 1-4 | First to 25 points, win by 2 (deuce from 24-24) |
| Set 5 | First to 15 points, win by 2 |
| Match | Best of 5 sets — first team to win 3 sets |
| Side switch | Teams switch sides after each set |
| Starting side | Home team starting side configurable at game start |

---

## 6. Data Model

### 6.1 Team

| Field | Type | Notes |
|-------|------|-------|
| id | String | MongoDB ObjectId |
| code | String | Unique, e.g. `DA` |
| name | String | e.g. `Dames A - Promo 2B` |
| league | Enum | VVB / KWB / Sporta |
| reeks | String | League code for ranking fetch, e.g. `ADP2-B` |
| color | String | Hex color, preset palette of 8 |
| sponsor | Object | name + logo path |
| sponsor2 | Object | name + logo path |
| active | Boolean | Default true; false = soft deleted |

MongoDB collection: `teams` · Shared between volley-live-scores and newRanking

### 6.2 Game

| Field | Type | Notes |
|-------|------|-------|
| id | String | MongoDB ObjectId |
| date | String | Stored as `"YYYYMMDD"` — no timezone ambiguity |
| timeBlock | Enum | BLOCK_10 … BLOCK_20 |
| court | Enum | A1, A2, A3, B1, B2 |
| homeTeam | Team | Embedded full Team object |
| awayTeam | String | Visiting club name |
| awayColor | String | Hex — set by admin at creation |
| status | Enum | SCHEDULED / IN_PROGRESS / FINISHED |

MongoDB collection: `games` · homeTeam embedded for performance; cascaded on team update

### 6.3 Score

| Field | Type | Notes |
|-------|------|-------|
| id | String | MongoDB ObjectId |
| gameId | String | Unique index → Game |
| sets | List\<SetScore\> | Completed sets (home + away per set) |
| currentSetHome | int | Live score in current set |
| currentSetAway | int | Live score in current set |
| currentSet | int | Set number 1-5 |
| homeLeftSide | boolean | Flips after each set |
| version | int | Incremented per point for optimistic concurrency |

MongoDB collection: `scores` · One document per game, created when scoring starts

---

## 7. Technical Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Database | MongoDB Atlas free tier | Document model fits volleyball data; free tier sufficient for scale |
| Team embedding in Game | Embedded (not referenced) | No joins at query time; cascaded on team update |
| Away team color | Stored on Game | Immutable once set; admin configures at game creation |
| Concurrency | Optimistic locking (version field) | 1-2 scorers per game; conflicts rare; no locking overhead |
| Press-and-hold scoring | 600ms threshold | Prevents accidental taps in noisy crowd environment |
| Soft delete teams | active boolean | Teams may return next season; full history preserved |

---

## 8. v1 Complete — tagged v1.0.0

All MVP features shipped:

- [x] **Dashboard** — big-screen display, court cards with gap/margin, auto-refresh every 5 seconds, filterable by `?timeBlock=BLOCK_XX` for OptiSigns per time slot
- [x] **Admin** — team CRUD (soft delete, color cascade), game planning with duplicate + same-color validation
- [x] **Scorer UI** — press-and-hold scoring, undo, set/game end confirmation, conflict handling
- [x] **Set 5 side handling** — scorer chooses sides after coin toss; teams auto-switch at 8 points mid-set
- [x] **Shared team data** — MongoDB Atlas, read by both volley-live-scores and newRanking

## 9. Backlog — v2

### 🔴 High Priority

- [ ] **Color readability on dashboard** — verify all home/away color combinations are readable from 10m on a TV screen; dark background makes some combos (e.g. purple vs blue) hard to distinguish; may need contrast check and palette restriction
- [ ] **Youth game format** — youth games always play all 4 sets regardless of match score; needs a boolean flag (e.g. `alwaysPlayAllSets`) on Game level; scoring logic must skip match-won check for sets 1-3 when flag is set
- [ ] **Security / login** — Google OAuth2 via Spring Security; club already uses Google so all members have accounts
  - **Admin role** — manage teams and games; granted per Google account in MongoDB
  - **Scorer role** — enter scores for today's games; granted per Google account in MongoDB
  - **Dashboard** — no login; protected by a secret URL token (e.g. `?token=abc123`) so it can run unattended on a TV/OptiSigns
  - Needs: Google Cloud OAuth2 client ID + secret; `User` collection in MongoDB storing Google subject ID + role

### 🟡 Normal Priority

- [ ] **Google Cloud Console branding** — OAuth2 app is published (no more test user list); complete the branding fields (logo, privacy policy, support email, terms of service) before onboarding a second tenant; low priority until then
- [ ] **Admin game list — sections per timeblock** — group games by timeblock on the admin day view; each timeblock is a collapsible or static section header (e.g. "10:00", "12:00"); makes it easier to scan a full match day at a glance
- [ ] **Admin add game — per timeblock** — show an "Add game" button inside each timeblock section; clicking prefills the timeblock field in the add game form so the admin doesn't have to select it manually
- [ ] **Dashboard — upcoming games ribbon** — optional bottom ribbon showing planned (SCHEDULED) games for the next timeblock(s) today; controlled via URL parameter (e.g. `?ribbon=true` or `?ribbon=1` for next 1 timeblock); useful for screens that display live scores but also want to preview what's coming next

- [ ] **Profile icon + logout** — show a small profile avatar (Google picture or initials) in the top corner of all authenticated views (admin, scorer); clicking opens a dropdown with logout option
- [ ] **Role switcher for admin** — admin can switch between admin, scorer, and dashboard views from a dropdown on the profile icon, without navigating manually to each URL; dashboard view should pass the token automatically so admin can preview what OptiSigns sees

- [ ] **Dashboard refresh optimisation** — finished games have no live score to update; skip score fetch for FINISHED games to reduce unnecessary polling; consider stopping refresh entirely when all games are finished
- [ ] **Scorer overview refresh** — evaluate whether 5s auto-refresh is needed on the game list (`/score`); scorers navigate manually so polling may be unnecessary overhead

### ✅ Done

- [x] **Cloud hosting** — deployed to Railway with MongoDB Atlas; auto-deploy on PR merge to main
- [x] **Score simulator / fast input** — `?dev=true` URL flag disables hold requirement; orange DEV MODE badge shown

## 10. Backlog — v3 (after summer, September+)

- [ ] **VolleyScore game fetching API** — fetch official game schedule and results from the VVB/Sporta league API; new season starts in September so API data is not available before then
- [ ] **MongoDB IP whitelist** — Atlas currently allows all IPs (0.0.0.0/0); blocked by Railway Pro ($20/month) needed for static egress IPs; local dev can use NordVPN dedicated IP; revisit after summer

## 11. Testing Backlog

- [ ] **Concurrent scorers** — test behaviour when two scorers are on the same game simultaneously; verify optimistic locking (HTTP 409) is handled correctly and both scorers see the conflict banner and resync
- [ ] **Mobile — iOS** — test scorer UI on iOS Safari; verify press-and-hold works correctly with touch events, landscape lock, and no unwanted browser gestures interfering
- [ ] **Mobile — Android** — same as iOS but on Android Chrome; touch event behaviour may differ
