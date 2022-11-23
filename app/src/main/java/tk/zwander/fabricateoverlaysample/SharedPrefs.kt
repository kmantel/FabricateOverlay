package tk.zwander.fabricateoverlaysample

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

/**
 * Android's SharedPreferences for storing key-value pairs.
 * DO NOT INSTANTIATE THIS CLASS - Use [Prefs] for your needs.
 */
internal class SharedPrefs(
    context: Context,
    prefName: String = PREFERENCES_NAME,
    prefMode: Int = PREFERENCES_MODE
) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(prefName, prefMode)

    private val editor: SharedPreferences.Editor
        get() = prefs.edit()

    /**
     * Retrieves the Int value from the shared prefs
     */
    internal operator fun get(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    /**
     * Puts the Int value from the shared prefs
     */
    internal fun put(key: String, value: Int) {
        editor.putInt(key, value).commit()
    }

    /**
     * Retrieves the String value from the shared prefs
     */
    internal operator fun get(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: ""
    }

    /**
     * Puts the String value from the shared prefs
     */
    internal fun put(key: String, value: String) {
        editor.putString(key, value).commit()
    }

    /**
     * Retrieves the Boolean value from the shared prefs
     */
    internal operator fun get(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    /**
     * Puts the Boolean value from the shared prefs
     */
    internal fun put(key: String, value: Boolean) {
        editor.putBoolean(key, value).commit()
    }

    /**
     * Clears the preferences
     */
    internal fun clear() {
        editor.clear().commit()
    }

    /**
     * Check if key is contained in the shared prefs
     */
    internal fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    companion object {
        private const val PREFERENCES_NAME = BuildConfig.APPLICATION_ID
        private const val PREFERENCES_MODE = MODE_PRIVATE
    }
}