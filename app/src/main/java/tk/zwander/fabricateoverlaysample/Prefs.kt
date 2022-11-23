package tk.zwander.fabricateoverlaysample

import android.content.Context

object Prefs {

    private lateinit var sharedPrefs_: SharedPrefs
    private lateinit var context: Context

    private val sharedPrefs: SharedPrefs
        get() {
            if (!Prefs::sharedPrefs_.isInitialized) {
                throw RuntimeException("Prefs has not been instantiated. Call init() with context")
            }
            return sharedPrefs_
        }

    /**
     * Initialise the Prefs object for future static usage.
     * Make sure to initialise this in Application class.
     *
     * @param context The context to initialise with.
     */
    fun init(context: Context) {
        if (Prefs::sharedPrefs_.isInitialized) {
            throw RuntimeException("Prefs has already been instantiated")
        }
        sharedPrefs_ = SharedPrefs(context)
        Prefs.context = context
    }

    fun contains(key: String): Boolean {
        return sharedPrefs.contains(key)
    }

    fun put(key: String, value: Int) {
        sharedPrefs.put(key, value)
    }

    fun put(key: String, value: String) {
        sharedPrefs.put(key, value)
    }

    fun put(key: String, value: Boolean) {
        sharedPrefs.put(key, value)
    }

    fun getBoolean(key: String): Boolean {
        return sharedPrefs[key, false]
    }

    var saturationValue: String
        set(value) {
            sharedPrefs.put("saturationValue", value)
        }
        get() {
            return sharedPrefs["saturationValue", "1.20"]
        }
}