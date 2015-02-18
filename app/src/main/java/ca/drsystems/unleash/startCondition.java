package ca.drsystems.unleash;

import java.io.Serializable;

/**
 * Created by BBaxter3160 on 2/16/2015.
 */
public class startCondition implements Serializable {
    private static final long serialVersionUID = 43L;
    private boolean ready;
    private double xur, xll, yur, yll;
    private int number;

    final int id = 254;

    public startCondition(boolean ready, int number, double xur,double yur,double xll,double yll)
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
