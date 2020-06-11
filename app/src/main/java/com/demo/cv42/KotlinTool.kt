package com.demo.cv42

import android.app.Activity
import android.content.Intent

/**
 * Created by wangzy on 2020-04-29
 * description:
 */

fun Activity.jump(clazz: Class<out Activity>) {
    var intent = Intent(this, clazz)
    startActivity(intent)
}

