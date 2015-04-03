package ca.drsystems.unleash;

import java.io.Serializable;

//Wrapper class for all Objects being sent over the Output Stream
public class UnleashPackage implements Serializable{

    private static final long serialVersionUID = 42L;
    private int header;
    private Object data;

    public UnleashPackage(int h, Object d){
        this.header = h;
        this.data = d;
    }
    //setters
    public void setHeader(int h){
        this.header = h;
    }

    public void setData(Object d){this.data = d;}
    //getters
    public int getHeader(){
        return this.header;
    }

    public Object getData(){
        return this.data;
    }
}
