package com.mobile.storage.clean.utils

import android.content.Context
import com.tencent.mmkv.MMKV
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


object MMKVUtils {
    
    private lateinit var mmkv: MMKV
    

    fun initialize(context: Context) {
        MMKV.initialize(context)
        mmkv = MMKV.defaultMMKV()
    }
    

    fun getMMKV(): MMKV = mmkv
    

    fun stringProperty(defaultValue: String = "", key: String? = null): ReadWriteProperty<Any?, String> {
        return object : ReadWriteProperty<Any?, String> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): String {
                val propertyKey = key ?: property.name
                return mmkv.decodeString(propertyKey, defaultValue) ?: defaultValue
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                val propertyKey = key ?: property.name
                mmkv.encode(propertyKey, value)
            }
        }
    }
    

    fun intProperty(defaultValue: Int = 0, key: String? = null): ReadWriteProperty<Any?, Int> {
        return object : ReadWriteProperty<Any?, Int> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
                val propertyKey = key ?: property.name
                return mmkv.decodeInt(propertyKey, defaultValue)
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
                val propertyKey = key ?: property.name
                mmkv.encode(propertyKey, value)
            }
        }
    }
    

    fun longProperty(defaultValue: Long = 0L, key: String? = null): ReadWriteProperty<Any?, Long> {
        return object : ReadWriteProperty<Any?, Long> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
                val propertyKey = key ?: property.name
                return mmkv.decodeLong(propertyKey, defaultValue)
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
                val propertyKey = key ?: property.name
                mmkv.encode(propertyKey, value)
            }
        }
    }
    

    fun booleanProperty(defaultValue: Boolean = false, key: String? = null): ReadWriteProperty<Any?, Boolean> {
        return object : ReadWriteProperty<Any?, Boolean> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
                val propertyKey = key ?: property.name
                return mmkv.decodeBool(propertyKey, defaultValue)
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
                val propertyKey = key ?: property.name
                mmkv.encode(propertyKey, value)
            }
        }
    }
    

    fun floatProperty(defaultValue: Float = 0f, key: String? = null): ReadWriteProperty<Any?, Float> {
        return object : ReadWriteProperty<Any?, Float> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
                val propertyKey = key ?: property.name
                return mmkv.decodeFloat(propertyKey, defaultValue)
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
                val propertyKey = key ?: property.name
                mmkv.encode(propertyKey, value)
            }
        }
    }
    


    fun contains(key: String): Boolean {
        return mmkv.containsKey(key)
    }
}
