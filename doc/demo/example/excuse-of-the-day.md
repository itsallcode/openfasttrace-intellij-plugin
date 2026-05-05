# Excuse of the Day Demo

This small specification is the live demo project for the OpenFastTrace IntelliJ plugin.

## Features

### Excuse of the Day
`feat~excuse-of-the-day~1`

The demo app gives the user a random harmless excuse for being late.

Needs: req

<!-- Demo step 4: add the Homework Excuse feature here. -->

### Foo
`req~foo~1`

Bar

Covers:

* [``](#)

Needs: req



## User Requirements

### Tell a Late-Work Excuse
`req~tell-late-work-excuse~1`

The app provides one short reason the user can use when they are late for work.

Covers:
- `feat~excuse-of-the-day~1`

Needs: scn

## Scenarios

### Pick a Late-Work Excuse
`scn~tell-late-work-excuse~1`

**Given** a user needs an excuse before the daily stand-up
**When** the user asks for today's excuse
**Then** the app returns one reason from the prepared excuse list

Covers:
- `req~tell-late-work-excuse~1`

Needs: dsn

## Design

### Random Excuse Selection
`dsn~tell-late-work-excuse~1`

The script stores a short list of excuses and selects one entry at random for each run.

Covers:
- `scn~tell-late-work-excuse~1`

Needs: impl
