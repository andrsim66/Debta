package com.sevenander.debta.app.pojo;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.sevenander.debta.app.data.DebtaContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrii on 07.03.15.
 */
@ParseClassName(DebtaContract.DCheckItemEntry.TABLE_NAME)
public class DCheckItem extends ParseObject {

    public String getName() {
        return getString(DebtaContract.DCheckItemEntry.COLUMN_NAME);
    }

    public void setName(String name) {
        put(DebtaContract.DCheckItemEntry.COLUMN_NAME, name);
    }

    public double getPrice() {
        return getDouble(DebtaContract.DCheckItemEntry.COLUMN_PRICE);
    }

    public void setPrice(double price) {
        put(DebtaContract.DCheckItemEntry.COLUMN_PRICE, price);
    }

    public double getDebt() {
        return getDouble(DebtaContract.DCheckItemEntry.COLUMN_DEBT);
    }

    public void setDebt(double debt) {
        put(DebtaContract.DCheckItemEntry.COLUMN_DEBT, debt);
    }

    public List<String> getUsers() {
        return getList(DebtaContract.DCheckItemEntry.COLUMN_USERS);
    }

    public void setUsers(ArrayList<String> users) {
        put(DebtaContract.DCheckItemEntry.COLUMN_USERS, users);
    }
}
