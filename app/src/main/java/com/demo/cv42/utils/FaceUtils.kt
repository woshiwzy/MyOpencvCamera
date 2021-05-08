package com.demo.cv42.utils

import com.demo.cv42.App
import com.wangzy.db.DbController
import com.wangzy.db.People

/**
 * Created by wangzy on 4/27/21
 * description:
 */



fun recordPerson(name: String, hogFeatureString: String) {
    var peop = People()
    peop.name = name
//                                  peop.feature = featurs + "," + hogFeatureString//存储的时候加上hog特诊
    peop.feature = hogFeatureString//只用hog特征
    DbController.getInstance(App.app).getSession().peopleDao.insertOrReplace(peop)
}