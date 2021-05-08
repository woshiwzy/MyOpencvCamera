package com.wangzy.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.ArrayList;
import java.util.List;


@Entity
public class People {


    @Id(autoincrement = true)
    private Long id;
    private String name;
    private String feature;


    @Generated(hash = 772505674)
    public People(Long id, String name, String feature) {
        this.id = id;
        this.name = name;
        this.feature = feature;
    }

    @Generated(hash = 1406030881)
    public People() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public List<Float> getVector() {
        String[] vecs = feature.split(",");
        List<Float> vecList = new ArrayList<>(vecs.length);
        for (int i = 0, isize = vecs.length; i < isize; i++) {
            vecList.add(Float.valueOf(vecs[i]));
        }
        return vecList;
    }
}
