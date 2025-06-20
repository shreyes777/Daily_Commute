package com.example.dailycommute;

import static com.example.dailycommute.map_tool.name;

//Backend for Database Read-Write
public class users
{
    String name, sou, des;

    public users() {}

    public users(String name, String sou, String des)
    {
        this.name = name;
        this.sou = sou;
        this.des = des;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSource()
    {
        return sou;
    }

    public void setSource(String sou)
    {
        this.sou = sou;
    }

    public String getDestination()
    {
        return des;
    }

    public void setDestination(String des)
    {
        this.des = des;
    }


}
