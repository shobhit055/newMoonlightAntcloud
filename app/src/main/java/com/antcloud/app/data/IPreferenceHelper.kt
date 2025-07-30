package com.antcloud.app.data

interface IPreferenceHelper {
    fun setPcExit(res: Boolean)
    fun getPcExit(): Boolean
    fun setExitTime(date: Long)
    fun getExitTime(): Long
    fun setProperExit(res: Boolean)
    fun getProperExit(): Boolean
    fun clearPrefs()
}