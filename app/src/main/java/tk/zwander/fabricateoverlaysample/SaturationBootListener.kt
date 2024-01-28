package tk.zwander.fabricateoverlaysample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SaturationBootListener : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        when (p1?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                MainActivity.setSaturation()
            }
        }
    }
}