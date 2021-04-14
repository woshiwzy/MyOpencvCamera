package com.demo.cv42.ml;

import android.content.Context;

import com.wangzy.face.DaoMaster;
import com.wangzy.face.DbController;
import com.wangzy.face.People;

import java.util.ArrayList;

/**
 * Created by wangzy on 4/14/21
 * description:
 */
public class MyMl {


    private ArrayList<People> peoples;
    public MyMl(Context context) {
        this.peoples=new ArrayList<>();
        loadData(context);
    }


    public void loadData(Context context){
        this.peoples.clear();
        peoples.addAll(DbController.getInstance(context).getSession().getPeopleDao().loadAll());
    }


    public ArrayList<People> findNears(int n,ArrayList<Float> inputVectors){




        return null;

    }


    public int getSampleSize(){
        return  peoples.size();
    }

}
