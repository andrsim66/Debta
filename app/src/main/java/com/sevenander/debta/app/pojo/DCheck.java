package com.sevenander.debta.app.pojo;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.sevenander.debta.app.data.DebtaContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by andrii on 07.03.15.
 */
@ParseClassName(DebtaContract.DCheckEntry.TABLE_NAME)
public class DCheck extends ParseObject {

    public String getGroupId() {
        return getString(DebtaContract.DCheckEntry.COLUMN_GROUP_ID);
    }

    public void setGroupId(String groupId) {
        put(DebtaContract.DCheckEntry.COLUMN_GROUP_ID, groupId);
    }

    public Date getDate() {
        return getDate(DebtaContract.DCheckEntry.COLUMN_DATE);
    }

    public void setDate(Date date) {
        put(DebtaContract.DCheckEntry.COLUMN_DATE, date);
    }

    public double getDebt() {
        return getDouble(DebtaContract.DCheckEntry.COLUMN_DEBT);
    }

    public void setDebt(double debt) {
        put(DebtaContract.DCheckEntry.COLUMN_DEBT, debt);
    }

    public String getUserId() {
        return getString(DebtaContract.DCheckEntry.COLUMN_USER_ID);
    }

    public void setUserId(String userId) {
        put(DebtaContract.DCheckEntry.COLUMN_USER_ID, userId);
    }

    public String getEditId() {
        return getString(DebtaContract.DCheckEntry.COLUMN_EDIT_ID);
    }

    public void setEditId(String editId) {
        put(DebtaContract.DCheckEntry.COLUMN_EDIT_ID, editId);
    }

    public List<String> getItems() {
        return getList(DebtaContract.DCheckEntry.COLUMN_ITEMS);
    }

    public void setItems(List<String> items) {
        put(DebtaContract.DCheckEntry.COLUMN_ITEMS, items);
    }

    public List<String> getConfirmedUsers() {
        return getList(DebtaContract.DCheckEntry.COLUMN_USERS);
    }

    public void setConfirmedUsers(List<String> confirmedUsers) {
        put(DebtaContract.DCheckEntry.COLUMN_USERS, confirmedUsers);
    }

    public boolean isPaid() {
        return getBoolean(DebtaContract.DCheckEntry.COLUMN_IS_PAID);
    }

    public void setPaid(boolean isPaid) {
        put(DebtaContract.DCheckEntry.COLUMN_IS_PAID, isPaid);
    }

    public boolean isConfirmed() {
        return getBoolean(DebtaContract.DCheckEntry.COLUMN_IS_CONFIRMED);
    }

    public void setConfirmed(boolean isConfirmed) {
        put(DebtaContract.DCheckEntry.COLUMN_IS_CONFIRMED, isConfirmed);
    }
}
