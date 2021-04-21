package com.demo.cv42.ml;

import android.content.Context;

import com.wangzy.face.DbController;
import com.wangzy.face.People;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wangzy on 4/14/21
 * description:
 */
public class MyMl {


    private static MyMl ml;

    public static MyMl getInstance(Context context){
        if(null==ml){
            ml=new MyMl(context);
        }
        return ml;
    }


    private ArrayList<People> peoples;
    private Context context;

    public MyMl(Context context) {
        this.peoples = new ArrayList<>();
        this.context=context;
        loadData(context);
    }
    public void reload(){
        loadData(this.context);
    }


    private void loadData(Context context) {
        this.peoples.clear();
        peoples.addAll(DbController.getInstance(context).getSession().getPeopleDao().loadAll());
    }

    public HashMap<Integer, RecResult> findNears(int n, ArrayList<Float> inputVectors) {
        HashMap<Integer, RecResult> nearPeoples = new HashMap<>(n);

        for (People people : this.peoples) {
            float totalDistance = computeDistancePercent(inputVectors, people);
            if (nearPeoples.containsKey(1)) {
                if (totalDistance < nearPeoples.get(1).distance) {
                    nearPeoples.put(1,new RecResult(people,totalDistance));
                }
            }else {
                nearPeoples.put(1,new RecResult(people,totalDistance));
            }
        }
        return nearPeoples;
    }


    public int getSampleSize() {
        return peoples.size();
    }


    public static float computeDistancePercent(ArrayList<Float> inputFeature, People inPutPeople) {
        List<Float> inputPeopleFeatures = inPutPeople.getVector();
        float peopleTotalFeature = 0;
        float distanceTotal = 0;
        for (int i = 0, isize = inputPeopleFeatures.size(); i < isize; i++) {
            peopleTotalFeature += inPutPeople.getVector().get(i);
            distanceTotal += Math.sqrt(Math.pow(inPutPeople.getVector().get(i) - inputFeature.get(i), 2));
        }
        return distanceTotal;
    }
}
