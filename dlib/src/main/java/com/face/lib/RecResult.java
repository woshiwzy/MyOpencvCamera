package com.face.lib;

import com.wangzy.db.People;

/**
 * Created by wangzy on 4/14/21
 * description:
 */
public class RecResult {


    public People people;
    public float percent;

    public RecResult(People people, float percent) {
        this.people = people;
        this.percent = percent;
    }

    @Override
    public String toString() {
        return "Result{" +
                "people=" + people.getName() +
                ", distance=" + percent +
                '}';
    }
}
