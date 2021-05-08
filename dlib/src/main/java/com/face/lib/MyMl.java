package com.face.lib;

import android.content.Context;

import com.wangzy.db.DbController;
import com.wangzy.db.People;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wangzy on 4/14/21
 * description:
 */
public class MyMl {


    private static MyMl ml;

    public static MyMl getInstance(Context context) {
        if (null == ml) {
            ml = new MyMl(context);
        }
        return ml;
    }


    private ArrayList<People> peoples;
    private Context context;

    public MyMl(Context context) {
        this.peoples = new ArrayList<>();
        this.context = context;
        loadData(context);
    }

    public void reload() {
        loadData(this.context);
    }


    private void loadData(Context context) {
        this.peoples.clear();
        peoples.addAll(DbController.getInstance(context).getSession().getPeopleDao().loadAll());
    }

    public HashMap<Integer, RecResult> findNears(int n, ArrayList<Float> inputVectors) {
        HashMap<Integer, RecResult> nearPeoples = new HashMap<>(n);

        for (People people : this.peoples) {
            float totalDistance = VectorTool.computeSimilarity2(inputVectors, people.getVector());
            if (nearPeoples.containsKey(1)) {
                if (totalDistance > nearPeoples.get(1).percent) {
                    nearPeoples.put(1, new RecResult(people, totalDistance));
                }
            } else {
                nearPeoples.put(1, new RecResult(people, totalDistance));
            }
        }
        return nearPeoples;
    }


    public int getSampleSize() {
        return peoples.size();
    }

}
