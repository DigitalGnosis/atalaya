// Phase 1 Step 2 placeholder. Real screens land with the Step 3 vertical slice.
package dev.digitalgnosis.atalaya.node

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                Text(text = "Atalaya Node — skeleton")
            }
        }
    }
}
