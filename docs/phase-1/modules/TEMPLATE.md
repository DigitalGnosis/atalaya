---
module: <module-name>
status: stub | drafted | implemented
phase: <N>
---

# `<module-name>`

## Responsibility

One paragraph: what this module owns and what it does NOT own.

## Public API

The interfaces / types this module exposes to its consumers.

```kotlin
// or Python, or Go, depending on the module
interface Example {
    fun doThing(input: Type): Result
}
```

## Dependencies

| On | Why |
|----|-----|
| `package-x` | _why we depend on it_ |

## Consumers

| App or package | Use |
|----------------|-----|
| `apps/node` | _what it does with this module_ |

## Code example (illustrative)

A representative usage from a consumer's perspective.

```kotlin
val ex: Example = inject()
val result = ex.doThing(input)
```

## Open questions

What's still unsettled. References to research notes that will resolve them.

## Test surface

What tests this module needs (unit, integration, contract).

## Versioning notes

If the module's API changes, what's the migration story?
