package com.antcloud.app.data

import android.content.Context
import android.content.SharedPreferences

open class PreferenceManger constructor(context: Context) : IPreferenceHelper {
    private val PREFS_NAME = "cookies"
    private var preferences: SharedPreferences
    init {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun clearPrefs() {
        preferences.edit().clear().apply()
    }

    companion object {
        const val pc_exit = "pcExit"
        const val exit_time = "exitTime"
        const val proper_exit = "proper_exit"
    }

    override fun setPcExit(res: Boolean) {
        preferences[pc_exit] = res
    }

    override fun getPcExit(): Boolean {
        return preferences[pc_exit] ?: false
    }

    override fun setExitTime(date: Long) {
        preferences[exit_time] = date
    }

    override fun getExitTime(): Long {
        return preferences[exit_time]!!
    }

    override fun setProperExit(res: Boolean) {
        preferences[proper_exit] = res
    }

    override fun getProperExit(): Boolean {
        return preferences[proper_exit] ?: false
    }
}
/**
 * SharedPreferences extension function, to listen the edit() and apply() fun calls
 * on every SharedPreferences operation.
 */
private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    operation(editor)
    editor.apply()
}
/**
 * puts a key value pair in shared prefs if doesn't exists, otherwise updates value on given [key]
 */
private operator fun SharedPreferences.set(key: String, value: Any?) {
    when (value) {
        is String? -> edit { it.putString(key, value) }
        is Int -> edit { it.putInt(key, value) }
        is Boolean -> edit { it.putBoolean(key, value) }
        is Float -> edit { it.putFloat(key, value) }
        is Long -> edit { it.putLong(key, value) }
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}
/**
 * finds value on given key.
 * [T] is the type of value
 * @param defaultValue optional default value - will take null for strings, false for bool and -1 for numeric values if [defaultValue] is not specified
 */
private inline operator fun <reified T : Any> SharedPreferences.get(
    key: String,
    defaultValue: T? = null
): T? {
    return when (T::class) {
        String::class -> getString(key, defaultValue as? String) as T?
        Int::class -> getInt(key, defaultValue as? Int ?: 0) as T?
        Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
        Float::class -> getFloat(key, defaultValue as? Float ?: 0f) as T?
        Long::class -> getLong(key, defaultValue as? Long ?: 0) as T?
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}