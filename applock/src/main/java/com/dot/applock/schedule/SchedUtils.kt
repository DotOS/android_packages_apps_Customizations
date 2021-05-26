package com.dot.applock.schedule

import android.content.Context
import android.content.SharedPreferences
import com.dot.applock.ObjectSerializer

class SchedUtils(val context: Context) {

    val SCHED_TEMP_TAG = "scheduledapps_temp"
    val SCHED_TAG = "scheduledapps"
    private val tempPref: SharedPreferences =
        context.getSharedPreferences(SCHED_TEMP_TAG, Context.MODE_PRIVATE)
    private val permPref: SharedPreferences =
        context.getSharedPreferences(SCHED_TAG, Context.MODE_PRIVATE)


    fun tempAddApp(packagename: String) {
        val editor: SharedPreferences.Editor = tempPref.edit()
        val list = ObjectSerializer.deserialize(tempPref.getString("apps",
            ObjectSerializer.serialize(ArrayList<String>()))) as ArrayList<String>
        list.add(packagename)
        editor.putString("apps", ObjectSerializer.serialize(list))
        editor.apply()
    }

    fun tempRemoveApp(packagename: String) {
        val editor: SharedPreferences.Editor = tempPref.edit()
        val list = ObjectSerializer.deserialize(tempPref.getString("apps",
            ObjectSerializer.serialize(ArrayList<String>()))) as ArrayList<String>
        list.remove(packagename)
        editor.putString("apps", ObjectSerializer.serialize(list))
        editor.apply()
    }

    fun tempGetApps(): ArrayList<String> {
        return ObjectSerializer.deserialize(tempPref.getString("apps",
            ObjectSerializer.serialize(ArrayList<String>()))) as ArrayList<String>
    }

    fun addApp(packagename: String) {
        val editor: SharedPreferences.Editor = permPref.edit()
        val list = ObjectSerializer.deserialize(permPref.getString("apps",
            ObjectSerializer.serialize(ArrayList<String>()))) as ArrayList<String>
        list.add(packagename)
        editor.putString("apps", ObjectSerializer.serialize(list))
        editor.apply()
    }

    fun removeApp(packagename: String) {
        val editor: SharedPreferences.Editor = permPref.edit()
        val list = ObjectSerializer.deserialize(permPref.getString("apps",
            ObjectSerializer.serialize(ArrayList<String>()))) as ArrayList<String>
        list.remove(packagename)
        editor.putString("apps", ObjectSerializer.serialize(list))
        editor.apply()
    }

    fun getApps(): ArrayList<String> {
        return ObjectSerializer.deserialize(permPref.getString("apps",
            ObjectSerializer.serialize(ArrayList<String>()))) as ArrayList<String>
    }

}