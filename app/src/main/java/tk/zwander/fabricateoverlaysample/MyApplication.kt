package tk.zwander.fabricateoverlaysample

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
    }
}