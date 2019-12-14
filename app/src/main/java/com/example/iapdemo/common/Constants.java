package com.example.iapdemo.common;

public class Constants {
    /** requestCode for pull up the pay page */
    public static final int REQ_CODE_BUYWITHPRICE = 4001;

    /** requestCode for pull up the pmsPay page */
    public static final int REQ_CODE_BUY = 4002;

    /** requestCode for pull up the login page or agreement page for getBuyIntentWithPrice interface*/
    public static final int REQ_CODE_CONTINUE = 4005;

    /** requestCode for pull up the login page for isBillingSupported interface */
    public static final int REQ_CODE_LOGIN = 2001;

    /** the type of products */
    public static final int PRODUCT_TYPE_CONSUMABLE = 0;
    public static final int PRODUCT_TYPE_NON_CONSUMABLE = 1;
    public static final int PRODUCT_TYPE_RENEWABLE_SUBSCRIPTION = 2;
    public static final int PRODUCT_TYPE_RENEWABLE_NON_SUBSCRIPTION = 3;

    public static final String APPID = "100051813";
}
