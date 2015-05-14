package com.sevenander.debta.app.pojo;

import com.parse.ParseClassName;
import com.parse.ParseUser;
import com.sevenander.debta.app.data.DebtaContract;

/**
 * Created by andrii on 02.03.15.
 */
@ParseClassName("_" + DebtaContract.UserEntry.TABLE_NAME)
public class User extends ParseUser {

    private double debt;

    public String getFName() {
        return getString(DebtaContract.UserEntry.COLUMN_FNAME);
    }

    public void setFName(String name) {
        put(DebtaContract.UserEntry.COLUMN_FNAME, name);
    }

    public String getLName() {
        return getString(DebtaContract.UserEntry.COLUMN_LNAME);
    }

    public void setLName(String lastName) {
        put(DebtaContract.UserEntry.COLUMN_LNAME, lastName);
    }

    public double getDebt() {
        return debt;
    }

    public void setDebt(double debt) {
        this.debt = debt;
    }
}
