package ca.drsystems.unleash;

import java.io.Serializable;

/**
 * Created by BBaxter3160 on 2/16/2015.
 */
public class startCondition implements Serializable {
    private static final long serialVersionUID = 43L;
    private boolean ready;
    private float xur, xll, yur, yll;
    private int number;
    public void startCondition(boolean ready, int number, float xur,float xll,float yur,float yll)
    {
        this.ready = ready;
        this.number = number;
        this.xur = xur;
        this.xll = xll;
        this.yur = yur;
        this.yll = yll;
    }
    public boolean getReady()
    {
        return ready;
    }
    public int getNumber()
    {
        return number;
    }

}
