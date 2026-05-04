---
module: core-protocol
status: example shape
phase: 1
---

# `core-protocol` — Wire types

## Responsibility

`core-protocol` owns the message shapes that flow between Atalaya components. In Phase 1 a single phone produces and consumes everything itself — but defining the shapes now lets Phase 2's hub consume them without redesign.

It does NOT own:
- Transport (FCM, ntfy, MQTT, WebSocket — those live in role-specific modules)
- Persistence (event storage lives in the hub)
- Rule logic (that's `core-rules`)
- Inference (that's `core-ml`)

## Public API

The two foundational types: an `Observation` (what a node saw) and an `Alert` (a rule-matched observation).

```kotlin
// packages/core-protocol/src/main/kotlin/dev/digitalgnosis/atalaya/protocol/Observation.kt

package dev.digitalgnosis.atalaya.protocol

import kotlinx.serialization.Serializable

/**
 * A single sensor reading from a node, before any rule has been applied.
 * Phase 1 only emits VisionObservation; Phase 3 adds Audio, Contact, Motion, etc.
 */
@Serializable
sealed interface Observation {
    val nodeId: NodeId
    val role: Role
    val capturedAt: Instant
}

@Serializable
data class VisionObservation(
    override val nodeId: NodeId,
    override val role: Role = Role.CAMERA,
    override val capturedAt: Instant,
    /** Free-text description from the on-device VLM. */
    val description: String,
    /** Pointer to the source frame, if persisted. Null if frame was discarded after inference. */
    val frameRef: FrameRef? = null,
    /** Inference latency for telemetry / debugging. */
    val inferenceMs: Long,
) : Observation

@Serializable
@JvmInline
value class NodeId(val value: String)

@Serializable
enum class Role { CAMERA, AUDIO, CONTACT, MOTION, PRESENCE, KEYPAD, SIREN, HUB }
```

```kotlin
// packages/core-protocol/src/main/kotlin/dev/digitalgnosis/atalaya/protocol/Alert.kt

package dev.digitalgnosis.atalaya.protocol

import kotlinx.serialization.Serializable

/**
 * A rule matched an observation. Alerts are what get pushed to the user.
 */
@Serializable
data class Alert(
    val id: AlertId,
    val ruleId: RuleId,
    val observation: Observation,
    val firedAt: Instant,
    val severity: Severity = Severity.NORMAL,
)

@Serializable
@JvmInline
value class AlertId(val value: String)

@Serializable
@JvmInline
value class RuleId(val value: String)

@Serializable
enum class Severity { LOW, NORMAL, HIGH, CRITICAL }
```

## Dependencies

| On | Why |
|----|-----|
| `kotlinx.serialization` | Cross-platform JSON for protocol payloads |
| `kotlinx.datetime` | `Instant` type that serializes consistently |

Nothing else. `core-protocol` is the leaf module — it imports from no other Atalaya module. That's the rule that makes it portable.

## Consumers

| App or package | Use |
|----------------|-----|
| `apps/node` | Builds `VisionObservation` from each Gemma description, pipes into `core-rules`, builds `Alert` on match. |
| `packages/core-rules` | Consumes `Observation` to evaluate against rules. |
| `apps/hub` (Phase 2) | Receives `Observation` and `Alert` over the wire from nodes. |
| `apps/control` (Phase 4) | Receives `Alert` for display. |

## Code example (illustrative — what a vertical slice 4 looks like)

```kotlin
// inside apps/node, AtalayaService.onFrameCaptured(...)

val description = inference.describe(frameJpeg, prompt = SCENE_PROMPT)
val observation = VisionObservation(
    nodeId = nodeId,
    capturedAt = Clock.System.now(),
    description = description,
    inferenceMs = inferenceMs,
    frameRef = framePersister.save(frameJpeg),
)
val match = ruleEngine.evaluate(observation, activeRules)
if (match != null) {
    pushPipeline.fire(Alert(
        id = AlertId.next(),
        ruleId = match.rule.id,
        observation = observation,
        firedAt = Clock.System.now(),
    ))
}
```

## Open questions

- **Schema evolution.** When Phase 2 adds `AudioObservation`, do existing nodes need to know about it? Sealed interfaces require care across versions. Mitigation: `core-protocol` is versioned independently; older clients ignore unknown subtypes.
- **Frame storage strategy.** `FrameRef` could be a path (local) or a CID (content-addressed in a hub). Phase 1 uses local paths; Phase 2 expands this.
- **Time format on the wire.** ISO 8601 strings vs epoch ms. `kotlinx.datetime.Instant` defaults to ISO 8601 in JSON; we keep that.

Resolved questions move to ADRs.

## Test surface

- **Round-trip serialization** for every type pair: serialize, deserialize, equals. Catches schema drift.
- **Sealed interface exhaustion** — adding a new `Observation` variant forces the compiler to flag every unhandled `when`. That's the safety net.
- **Cross-version compat** — Phase 2 adds shapes; verify a Phase 1 node can still run against a Phase 2 hub.

## Versioning notes

`core-protocol` follows semver. Major bumps when wire shapes change incompatibly. Minor bumps when adding new variants (existing clients ignore). Patch bumps for documentation.

The `protocolVersion` field is included in every `Observation` once Phase 2 lands, so the hub can negotiate.

## Why this is the example

Module specs should read like this. A reader lands here, in 90 seconds knows: what the module does, what it exposes, who depends on it, what the calling code looks like, and where the open questions live. Other module specs in this directory should match this depth.
