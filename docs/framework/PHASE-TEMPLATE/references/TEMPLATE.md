---
project: <project-name>
url: <repo URL>
license: <SPDX license id>
read_at_commit: <commit hash or version tag>
read_at_date: <YYYY-MM-DD>
ported_in_atalaya:
  - <relative path in our repo>
---

# Reference — `<project-name>`

## What it is

One paragraph describing the source project.

## License

`<license>` — confirm AGPL-3.0 compatibility from [`README.md`](README.md#license-compatibility).

## What we ported

One or two paragraphs describing the specific pattern or snippet we lifted.

## Files in their repo we read

- `<their/file/path>` — _what it does there_
- `<their/file/path>` — _what it does there_

## Files in Atalaya that reflect this

- `<our/file/path>` — _what we built using their pattern_

## Required attribution

Header that goes in any of our files derived from this:

```
// Adapted from <project-name> (https://...)
// Licensed under <license> at the time we read it (commit <hash>).
// Modifications © Digital Gnosis, AGPL-3.0-only.
```

## Differences

What we changed from their version, and why.

## Notes for next time

Anything tricky about porting from this project — gotchas, inferred conventions, etc.
