// Phase 1 Step 2 placeholder. Real wiring lands with the Step 3 vertical slice.
// Hilt entry point per ADR-0008.
package dev.digitalgnosis.atalaya.node

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AtalayaApplication : Application()
