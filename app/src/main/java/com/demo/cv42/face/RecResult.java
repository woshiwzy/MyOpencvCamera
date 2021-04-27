package com.demo.cv42.face;

import com.wangzy.face.People;

/**
 * Created by wangzy on 4/14/21
 * description:
 */
public class RecResult {

    public People people;
    public float distance;

    public RecResult(People people, float distance) {
        this.people = people;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Result{" +
                "people=" + people.getName() +
                ", distance=" + distance +
                '}';
    }
}
