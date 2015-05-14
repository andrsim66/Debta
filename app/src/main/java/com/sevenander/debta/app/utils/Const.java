package com.sevenander.debta.app.utils;

/**
 * Created by andrii on 06.03.15.
 */
public class Const {
    public static final int FIRST_CALL = 1;
    public static final int OTHER_CALL = 2;
    public static final String KEY_CALL_EXTRA = "callNum";
    public static final String KEY_DGROUP_ID = "groupId";
    public static final String KEY_DGROUP_NAME = "groupName";
    public static final String KEY_ADMIN_ID = "adminId";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_IDS = "userIds";
    public static final String KEY_USER_FNAME = "userfName";
    public static final String KEY_OBJECT_ID = "objectId";
    public static final String KEY_EDIT_ID = "editId";
    public static final String KEY_DCHECKS_TO_EDIT = "mapUserIdDCheckId";
    public static final String KEY_DCHECK_ITEM_IDS = "chequeItemsIds";
    public static final String KEY_CDHECK_DATE = "chequeDateLong";
    public static final String KEY_IS_EDIT = "isEdit";
    public static final String KEY_ACTION_CLEAR = "actionClear";
    public static final String KEY_TAB_POS = "tabPos";
    public static final String KEY_STRING_URI = "stringUri";

    public static final int COL_DGROUP_OBJ_ID = 1;
    public static final int COL_DGROUP_NAME = 2;
    public static final int COL_DGROUP_ADMIN_ID = 3;
    public static final int COL_DGROUP_MEMBERS = 4;

    public static final int COL_DCHECK_OBJ_ID = 1;
    public static final int COL_DCHECK_GROUP_ID = 2;
    public static final int COL_DCHECK_DATE = 3;
    public static final int COL_DCHECK_DEBT = 4;
    public static final int COL_DCHECK_USER_ID = 5;
    public static final int COL_DCHECK_ITEMS = 6;
    public static final int COL_DCHECK_USERS = 7;
    public static final int COL_DCHECK_IS_PAID = 8;
    public static final int COL_DCHECK_IS_CONFIRMED = 9;
    public static final int COL_DCHECK_EDIT_ID = 10;

    public static final int COL_DCHECK_ITEM_OBJ_ID = 1;
    public static final int COL_DCHECK_ITEM_NAME = 2;
    public static final int COL_DCHECK_ITEM_PRICE = 3;
    public static final int COL_DCHECK_ITEM_DEBT = 4;
    public static final int COL_DCHECK_ITEM_USERS = 5;

    public static final int COL_USER_OBJ_ID = 1;
    public static final int COL_USER_USERNAME = 2;
    public static final int COL_USER_FNAME = 3;
    public static final int COL_USER_LNAME = 4;

    public static final int DGROUP_LOADER = 0;
    public static final int USER_LOADER = 1;
    public static final int USER_SEARCH_LOADER = 2;
    public static final int DCHECK_LOADER = 3;
    public static final int DCHECK_ITEM_LOADER = 4;

    public static final int REQUEST_CODE = 1;
    public static final int REQUEST_CODE_EDIT_DGROUP = 2;

    public final static int TEXT_SIZE_16 = 16;
    public final static int MARGIN_16 = 16;
    public final static int MARGIN_8 = 8;
}
