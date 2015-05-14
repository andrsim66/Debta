package com.sevenander.debta.app.pojo;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.sevenander.debta.app.data.DebtaContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrii on 06.03.15.
 */
@ParseClassName(DebtaContract.DGroupEntry.TABLE_NAME)
public class DGroup extends ParseObject {

    private double debt;

    public String getName() {
        return getString(DebtaContract.DGroupEntry.COLUMN_NAME);
    }

    public void setName(String name) {
        put(DebtaContract.DGroupEntry.COLUMN_NAME, name);
    }

    public String getAdminId() {
        return getString(DebtaContract.DGroupEntry.COLUMN_ADMIN_ID);
    }

    public void setAdminId(String adminId) {
        put(DebtaContract.DGroupEntry.COLUMN_ADMIN_ID, adminId);
    }

    public List<String> getMembers() {
        //todo change method return type to arraylist
        return getList(DebtaContract.DGroupEntry.COLUMN_MEMBERS);
    }

    public void setMembers(ArrayList<String> members) {
        put(DebtaContract.DGroupEntry.COLUMN_MEMBERS, members);
    }

    public double getDebt() {
        return debt;
    }

    public void setDebt(double debt) {
        this.debt = debt;
    }
}
