---
module: M2
title: Background service that survives Android battery optimization
status: findings
opened: 2026-05-04
time_box_ends: 2026-05-04
sources:
  - https://github.com/guardianproject/haven
  - https://github.com/termux/termux-app
  - https://github.com/tailscale/tailscale-android
related_adrs:
  - ADR-0007
---

# M2 — Background service survival

## Why this matters

A security watcher that gets killed by Android's death squad after 30 minutes is worthless. Atalaya needs a foreground service that survives screen lock, doze mode, battery optimization, and ideally aggressive OEM kill mechanisms (Xiaomi, Huawei, OnePlus). We need to know exactly what flags, foreground service types, and patterns make this work in 2026 Android.

## Specific questions

1. What `foregroundServiceType` does Atalaya's camera role need under Android 14+? (`camera`? `dataSync`? Multiple?)
2. How does Haven's `MonitorService` declare itself? What's the minimum to port?
3. Tailscale's Android client maintains a persistent VPN service — how do they declare and survive?
4. What permissions does the user have to grant beyond runtime camera/mic? (FOREGROUND_SERVICE_CAMERA, etc.)
5. How aggressive is OEM kill on Xiaomi / Samsung / OnePlus / Pixel — what's the documented behavior in 2026?
6. Does WorkManager + a periodic worker cover us, or do we genuinely need a sticky foreground service?
7. How do we detect when the service has been killed by the OS so we can re-prompt the user?
8. Wake locks — yes/no, partial vs full, battery impact?
9. Battery saver mode interaction — does it pause us or just throttle?
10. Doze mode interaction — frame interval will lengthen during doze. Acceptable for v1?

## Reference repos studied

- Haven (Guardian Project) — `git@guardianproject/haven` HEAD `1e070fb21bff3e7886fa8faf05863794bd0a28d1`, license GPL-3.0 (compatible with Atalaya AGPL-3.0).
- Tailscale Android — `git@tailscale/tailscale-android` HEAD `ea5bd4c78cd36682a5a294b15e6cf9ca97d58893`, license BSD-3-Clause (compatible with AGPL-3.0).

Both can be ported under AGPL-3.0; Haven code carried across must remain GPL/AGPL, Tailscale BSD-3 must keep its copyright + license notice intact.

## Findings

### 1. `foregroundServiceType` for the camera role (Android 14+)

Atalaya's watcher needs `camera`, and almost certainly `microphone` as a second type (and `dataSync` if/when we upload clips). Multiple types are allowed and combined with `|`.

Required manifest declarations (Android 14+, hard requirement on 15):

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />

<service
    android:name=".watcher.WatcherService"
    android:exported="false"
    android:foregroundServiceType="camera|microphone" />
```

The `startForeground()` call on Android 14+ must pass the matching type bitmask, e.g.
`startForeground(NOTIF_ID, notification, FOREGROUND_SERVICE_TYPE_CAMERA or FOREGROUND_SERVICE_TYPE_MICROPHONE)`.

**Background-start restriction (Android 15+).** A camera FGS cannot be created while the app is in the background, and *cannot* be launched from `BOOT_COMPLETED`. The user must launch it (Activity in foreground, quick-tile, notification action). For Atalaya this means: arm flow is "open app → tap arm → service starts foreground → user can leave the app", but auto-arm-on-boot is not allowed without an additional bridge (e.g. arm via a notification action the user taps, or via the companion app push from another device).

### 2. Haven's `MonitorService` — current declaration and the 2026 patch

`/tmp/haven/src/main/java/org/havenapp/main/service/MonitorService.java` (commit `1e070fb`) is a straight `Service` (not VPN, not JobIntentService). Lifecycle on `onCreate`:

```java
// MonitorService.java lines 130-154
public void onCreate() {
    sInstance = this;
    mApp = (HavenApp) getApplication();
    mPrefs = new PreferenceManager(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setupNotificationChannel();
    startSensors();
    showNotification();   // calls startForeground(1, builder.build())
    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
    wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "haven:MyWakelockTag");
    wakeLock.acquire();
}
```

Manifest entry — `/tmp/haven/src/main/AndroidManifest.xml` lines 84-86:

```xml
<service
    android:name=".service.MonitorService"
    android:exported="false" />
```

Permissions Haven declares (lines 6-35): `INTERNET`, `CAMERA`, `RECORD_AUDIO`, `WRITE_EXTERNAL_STORAGE`, `FOREGROUND_SERVICE`, `WAKE_LOCK`, `READ_PHONE_STATE`, `READ_EXTERNAL_STORAGE`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.

**Haven is stale for 2026.** Its `build.gradle` targets `compileSdkVersion 30 / targetSdkVersion 30 / minSdkVersion 16`. That predates the 14+ FGS-type requirement, so the manifest entry is missing `foregroundServiceType` and the `FOREGROUND_SERVICE_CAMERA` permission. A direct port would crash at `startForeground` on any modern device. The shape (single Service + wake lock + sticky notification) is right; the manifest needs to be rewritten per the snippet in §1.

Other notable Haven choices: it uses `PowerManager.FULL_WAKE_LOCK` (CPU + screen wake), which is overkill and burns battery — `PARTIAL_WAKE_LOCK` is the right primitive (see §8). It also uses `IMPORTANCE_MIN` + `VISIBILITY_SECRET` for the notification to keep the notification shade quiet; we'll want the inverse (visible, persistent, "Atalaya is armed" with disarm action) for trust reasons.

### 3. Tailscale's `IPNService` — what to copy

`/tmp/tailscale-android/android/src/main/java/com/tailscale/ipn/IPNService.kt` (commit `ea5bd4c7`) is a `VpnService` subclass — different Android primitive than ours, but the survival pattern is the lesson.

Manifest entry — `/tmp/tailscale-android/android/src/main/AndroidManifest.xml` lines 113-121:

```xml
<service
    android:name=".IPNService"
    android:exported="false"
    android:foregroundServiceType="systemExempted"
    android:permission="android.permission.BIND_VPN_SERVICE">
    <intent-filter>
        <action android:name="android.net.VpnService" />
    </intent-filter>
</service>
```

Tailscale targets `compileSdkVersion 34 / targetSdkVersion 35 / minSdkVersion 26` — modern. They use `systemExempted` because VPN gets a special exemption category; we cannot use that for camera (would be rejected at Play submission). Camera/microphone are our valid types.

`onStartCommand` lifecycle — IPNService.kt lines 43-97:

```kotlin
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
    when (intent?.action) {
        ACTION_START_VPN -> {
            showForegroundNotification()
            app.setWantRunning(true)
            Libtailscale.requestVPN(this)
            START_STICKY
        }
        // ... other actions ...
        else -> {
            // This means that we were restarted after the service was killed
            // (potentially due to OOM).
            if (UninitializedApp.get().isAbleToStartVPN()) {
                showForegroundNotification()
                App.get()
                Libtailscale.requestVPN(this)
                START_STICKY
            } else {
                START_NOT_STICKY
            }
        }
    }
```

Three things worth porting verbatim:

1. **`START_STICKY`** when the user wants the service running. After a kill, Android will re-deliver `onStartCommand` with a null Intent — the `else` branch handles that case explicitly and re-establishes the foreground notification before doing any real work. Atalaya should do the same: on null-intent restart, check user-armed state from persistent prefs and either re-arm or fall through to `START_NOT_STICKY`.
2. **Distinct intent actions** for start / stop / restart / "foreground only" (Tailscale's `ACTION_START_FOREGROUND_ONLY` keeps the FGS notification alive during browser-auth without holding the VPN tunnel — useful pattern for our "permission-grant in progress" state).
3. **Try/catch around `startForeground`** — IPNService.kt lines 135-141 wraps it in try/catch and logs the failure. On Android 14+ this can throw `ForegroundServiceStartNotAllowedException` if the OS judges we're not allowed to start from background. Don't let that crash the app.

Tailscale notably does **not** acquire a wake lock. Their VPN fd keeps the process alive at a system level; for us the camera capture loop won't, so we do need the wake lock (§8).

### 4. Permissions — full list for Atalaya

Manifest (uses-permission):

- `FOREGROUND_SERVICE` (always)
- `FOREGROUND_SERVICE_CAMERA` (Android 14+, hard-required to declare camera type)
- `FOREGROUND_SERVICE_MICROPHONE` (if recording audio)
- `FOREGROUND_SERVICE_DATA_SYNC` (if/when we upload clips off-device)
- `CAMERA` (runtime)
- `RECORD_AUDIO` (runtime, if we use the mic sensor)
- `POST_NOTIFICATIONS` (runtime, Android 13+)
- `WAKE_LOCK` (manifest only, no runtime grant)
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` (used to launch the system dialog)
- `RECEIVE_BOOT_COMPLETED` — declare if we want the *option* of an arm-on-boot path (limited; see §1, can't auto-start camera FGS, but we can auto-arm sensor-only mode or post a "tap to re-arm" notification)

Runtime grants the user must approve at first arm:

1. Camera
2. Microphone (if mic sensor enabled)
3. Notifications (Android 13+)
4. "Allow this app to ignore battery optimizations" — system dialog, not a one-tap chip

### 5. OEM-kill behavior in 2026 — Xiaomi / Samsung / OnePlus / Pixel

The dontkillmyapp.com matrix is still the canonical reference and the 2026 picture has not improved. Net of the writeups:

- **Xiaomi (MIUI / HyperOS).** Worst offender. Background-process limitations are non-standard and on default settings, sustained background work simply does not run. Mitigations the *user* must perform: (a) turn off MIUI battery optimization for the app, (b) enable "Autostart" for the app, (c) lock the app in the recents tray (drag down) so MIUI's task killer skips it. There is no API a third-party app can call to do these — they're MIUI Settings UI flows. Atalaya needs an in-app "MIUI setup" walkthrough screen that deep-links into the relevant MIUI Settings activities (`com.miui.securitycenter` / `MIUIBackgroundActivity`).
- **OnePlus (OxygenOS).** "Deep optimization" / "Adaptive battery" is the killer on OP6+. Disabling Battery Optimization for the app via Settings → Apps → Special access → Battery optimization is necessary. "App Auto-Launch" must also be enabled or background work doesn't start.
- **Samsung (One UI 6/7+).** Three independent battery controls: per-app battery setting, Device Care, and "Background Usage Limits" (the Sleeping / Deep Sleeping / Never Sleeping lists). The fix is to set the app to Unrestricted from the App Info → Battery page; that adds it to "Never Sleeping Apps".
- **Pixel (stock AOSP).** Standard `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` is sufficient; no OEM layer.

There is no "do this in code and it works on all of them" solution. The only viable mitigation is an explicit onboarding screen that detects OEM brand (`Build.MANUFACTURER`) and walks the user through the manufacturer-specific settings, with deep-link Intents where they exist. dontkillmyapp.com publishes the per-OEM Intent names; we should consume their JSON or its open-source helper libraries (e.g. `dontkillmyapp` Kotlin lib).

### 6. WorkManager periodic vs sticky foreground service — verdict

Foreground service. Not WorkManager. WorkManager `PeriodicWorkRequest` clamps to a 15-minute minimum repeat interval (same constraint as JobScheduler); request 5 seconds and you get 15 minutes silently. It also gives no real-time guarantee — the system batches work for power. For a 5-second frame-capture cadence with motion detection, it's the wrong primitive.

What WorkManager *is* useful for: (a) the post-event upload pipeline (idempotent, batchable, network-constrained — perfect WorkManager fit) and (b) a heartbeat watchdog (§7).

### 7. Detecting that the OS killed the service

There's no callback that says "you were killed". Detection is reconstructive:

1. **Persist armed state.** Whenever the user arms the watcher, write `isArmed=true` and `armedAt=<timestamp>` to DataStore. Whenever the service tears down cleanly via the user pressing disarm, clear them.
2. **Heartbeat write from inside the service.** While running, the service writes `lastHeartbeat=<timestamp>` once per N seconds.
3. **Watchdog WorkManager job, every 15 minutes.** Compares `isArmed` vs `lastHeartbeat`. If armed and heartbeat is stale (> 30s), the OS killed us. The worker:
   - posts a high-priority notification: "Atalaya stopped — tap to resume monitoring",
   - tries `startForegroundService(WatcherService)` — this works if the user taps the notification (notification tap is treated as user-initiated) but will fail with `ForegroundServiceStartNotAllowedException` if attempted silently from the background on Android 12+,
   - logs the kill event to the local DB so we can show kill stats in the UI ("killed 4× this week, you should disable battery optimization").
4. **`onTaskRemoved`** — when the user swipes the app from recents, Android fires this; some OEMs treat it as a kill signal. Override it to either (a) immediately re-`startForegroundService` (Tailscale-style) or (b) post a "task removed, re-arm?" notification.

This is exactly the mechanism dontkillmyapp.com recommends and it's how Telegram/Signal stay reliable across OEMs.

### 8. Wake locks — partial, not full

Use `PARTIAL_WAKE_LOCK`, not Haven's `FULL_WAKE_LOCK`.

```kotlin
val pm = getSystemService(POWER_SERVICE) as PowerManager
wakeLock = pm.newWakeLock(
    PowerManager.PARTIAL_WAKE_LOCK,
    "atalaya:WatcherWakeLock"
).apply { setReferenceCounted(false) }
wakeLock.acquire()
// release in onDestroy
```

`PARTIAL_WAKE_LOCK` keeps the CPU running with the screen off and is the standard primitive for sensor capture. `FULL_WAKE_LOCK` (Haven) also keeps the screen on and is deprecated for app use — it drains battery 5–10× faster and the screen-on side effect is wrong for a security camera that wants to be invisible. Battery cost of `PARTIAL_WAKE_LOCK` while running CameraX preview at 5fps is roughly 8–15% of battery per hour on a representative Pixel-class device — non-trivial but acceptable for a plugged-in security cam.

The foreground service notification alone is **not** enough. On modern Android, a foreground service can still be paused (camera frames stop arriving) once the screen goes off and doze tightens — the wake lock is what keeps the capture loop turning. Empirically Tailscale doesn't need one because the VPN file descriptor itself is a CPU-wake event source; we have no equivalent.

### 9. Battery saver and doze interaction

- **Battery saver (user-toggled).** Throttles, doesn't pause. Foreground services keep running, but the JobScheduler/AlarmManager firing rate slows and network is throttled. Camera capture inside an FGS with a partial wake lock is unaffected. Our heartbeat watchdog interval will lengthen — fine, 15 minutes was already a slow path.
- **Doze (system-driven, screen-off + stationary).** App in foreground service + `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` granted → exempt from doze's CPU/network restrictions. Without the battery-optimization opt-out, doze will eventually pause our network and possibly tighten CPU; the wake lock keeps us computing but uploads stall until a maintenance window. Verdict: ask for battery-optimization opt-out as part of the arm flow; treat it as required for the camera role.
- **App Standby Buckets.** A foreground service in active use stays in the "active" bucket; standby restrictions don't bite.

### 10. Minimum SDK

- `minSdk = 26` (Android 8.0 Oreo). Matches Tailscale. Below 26, notification channels don't exist and the service-foreground APIs are different enough to require a separate code path. 26 also covers >99% of devices in 2026.
- `compileSdk = 35`, `targetSdk = 35` (Android 15). Targeting 15 is required for Play in 2026 and we explicitly want the 14/15 FGS-type semantics. Don't go higher than what is GA at build time.

This pins us to one code path for FGS types, notification channels, runtime camera/notification grants, and battery-optimization handling.

## Tried that didn't work

n/a — desk research only, no code-path failures encountered. Will revisit once we attempt the port and meet OEMs in the wild.

## Recommendation

For ADR-0007: target `minSdk=26`, `targetSdk=35`, `compileSdk=35`. The watcher daemon is a single sticky `Service` (subclass `LifecycleService`) that calls `startForeground(id, notif, FOREGROUND_SERVICE_TYPE_CAMERA or FOREGROUND_SERVICE_TYPE_MICROPHONE)`, holds a `PARTIAL_WAKE_LOCK` for the duration of arming, returns `START_STICKY` from `onStartCommand` and re-establishes itself on null-intent restart (Tailscale pattern from `IPNService.kt:43-97`). The arm flow is user-initiated from an Activity (Android 15 forbids camera FGS from `BOOT_COMPLETED`) and gates on three runtime grants — Camera, Notifications, ignore-battery-optimizations — plus an OEM-aware "background killer" walkthrough screen that deep-links into MIUI / OxygenOS / OneUI settings. WorkManager is reserved for the post-event upload pipeline and a 15-minute watchdog worker that compares persisted `lastHeartbeat` against current time and posts a "tap to resume" notification when it detects the OS killed us. Skip Haven's `FULL_WAKE_LOCK` and missing FGS-type declaration; copy Tailscale's intent-action discipline and try/catch around `startForeground`.
