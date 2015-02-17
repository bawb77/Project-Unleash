package ca.drsystems.unleash;

import java.io.Serializable;

public class UnleashPackage implements Serializable{

    private static final long serialVersionUID = 42L;
    private int header;
    private Object data;

    public UnleashPackage(int h, User d){
        this.header = h;
        this.data = d;
    }

    public void setHeader(int h){
        this.header = h;
    }

    public int getHeader(){
        return this.header;
    }

    public void setData(Object d){
        this.data = d;
    }

    public Object getData(){
        return this.data;
    }
}
