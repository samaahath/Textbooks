package com.bookviewer.myapplication;

public class classlistitems {
    public String ClassId;
    public String Classname; //Name


    public classlistitems(String ClassId,String Classname)
    {
        this.ClassId = ClassId;
        this.Classname = Classname;
    }

    public String getClassId() {
        return ClassId;
    }
    public String getClassname() {
        return Classname;
    }

}
