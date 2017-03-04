package no.mesan.handterminator.model.db;

import com.orm.SugarRecord;

/**
 * Created by NegatioN on 20.04.2015.
 */
public class MatrixValue extends SugarRecord<MatrixValue> {
    private DBRoute parentroute;
    private int value;

    public MatrixValue(){}
    public MatrixValue(int value, DBRoute route){
        parentroute = route;
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

}
