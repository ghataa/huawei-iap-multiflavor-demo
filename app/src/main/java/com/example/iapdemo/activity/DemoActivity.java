package com.example.iapdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.iapdemo.common.CipherUtil;
import com.example.iapdemo.common.Constants;
import com.example.iapdemo.common.IapRequestHelper;
import com.example.iapdemo.common.Key;
import com.example.iapdemo.common.Utils;
import com.hmsdemo.doubleballsapp.huawei.R;
import com.huawei.hms.support.api.entity.iap.OrderStatusCode;
import com.huawei.hms.support.api.iap.BuyResultInfo;
import com.huawei.hms.support.api.iap.json.Iap;

public class DemoActivity extends Activity implements View.OnClickListener {

    public static final String TAG = "DemoActivity";
    // to purchase PMS product
    private Button buyBtn;
    // to subscribe
    private Button subscribeBtn;

    private Button getPurchaseHistoryBtn;
    //jump to the subscription management page
    private Button manageSubscribeBtn;

    private static int priceType = Constants.PRODUCT_TYPE_CONSUMABLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_layout);
        initView();
        // to check if the account service country supports IAP ,query product information show to user
        IapRequestHelper.initIap(this);
    }

    private void initView() {
        buyBtn = (Button) findViewById(R.id.buy_btn);
        subscribeBtn = (Button) findViewById(R.id.sub_btn);
        getPurchaseHistoryBtn = (Button) findViewById(R.id.getpurchasehistory_btn);
        manageSubscribeBtn = (Button) findViewById(R.id.manage_sub_btn);
        getPurchaseHistoryBtn.setOnClickListener(this);
        buyBtn.setOnClickListener(this);
        subscribeBtn.setOnClickListener(this);
        manageSubscribeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Check if the network is available
        if (!Utils.netWorkState(this)) {
            Toast.makeText(this, getString(R.string.no_network_connection_prompt), Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.getpurchasehistory_btn:
                IapRequestHelper.getPurchaseHistory(this);
                break;
            case R.id.buy_btn:
                priceType = Constants.PRODUCT_TYPE_CONSUMABLE;
                IapRequestHelper.getBuyIntent(this, "ballcost01", Constants.PRODUCT_TYPE_CONSUMABLE);
                break;
            case R.id.sub_btn:
                priceType = Constants.PRODUCT_TYPE_RENEWABLE_SUBSCRIPTION;
                IapRequestHelper.getBuyIntent(this, "ballsub101", Constants.PRODUCT_TYPE_RENEWABLE_SUBSCRIPTION);
                break;
            case R.id.manage_sub_btn:
                jumpToManageSubscribePage();
                break;
            default:
                break;
        }
    }

    /**
     * 跳转到订阅管理页面
     */
    private void jumpToManageSubscribePage() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("pay://com.huawei.hwid.external/subscriptions?package=" + this.getPackageName() + "&appid=" + Constants.APPID));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this,"Please update HMS", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQ_CODE_BUY) {
            if (data != null) {
                BuyResultInfo buyResultInfo = Iap.getIapClient(this).getBuyResultInfoFromIntent(data);
                if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_CANCEL) {
                    // user cancels payment
                    Toast.makeText(this, getString(R.string.cancel), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_ITEM_ALREADY_OWNED) {
                    // item already owned
                    Toast.makeText(this, getString(R.string.item_already_owned), Toast.LENGTH_SHORT).show();
                    // you can check if the user has purchased the product and decide whether to provide goods
                    // if the purchase is a consumable product, consuming the purchase and deliver product
                    return;
                }
                if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                    // verify signature of payment results
                    boolean success = CipherUtil.doCheck(buyResultInfo.getInAppPurchaseData(), buyResultInfo.getInAppDataSignature(), Key.getPublicKey());
                    if (success) {
                        // if the user purchase a consumable item, you need to call the consumption interface to consume it after delivering the product to your user
                        if (priceType == Constants.PRODUCT_TYPE_CONSUMABLE) {
                            IapRequestHelper.consumePurchase(this, buyResultInfo.getInAppPurchaseData(), buyResultInfo.getInAppDataSignature());
                        }else if(priceType == Constants.PRODUCT_TYPE_RENEWABLE_SUBSCRIPTION){
                            Toast.makeText(this, "Subscribe success", Toast.LENGTH_SHORT).show();
                        }else if(priceType == Constants.PRODUCT_TYPE_NON_CONSUMABLE){
                            Toast.makeText(this,"Buy non consumable product",Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(this, getString(R.string.pay_success_signfail), Toast.LENGTH_SHORT).show();
                    }
                    return;
                } else {
                    Toast.makeText(this, getString(R.string.pay_fail), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else {
                Log.i(TAG, "data is null");
            }
            return;
        }

        if (requestCode == Constants.REQ_CODE_LOGIN) {
            int returnCode = 1;
            if (data != null) {
                returnCode = data.getIntExtra("returnCode", -1);
            }
            if (returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
                //query the products that the user has purchased and deliver products
                IapRequestHelper.getPurchase(this, Constants.PRODUCT_TYPE_CONSUMABLE);
                // query products and show products information to user
                IapRequestHelper.queryProductInfo(this);
            } else if(returnCode == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED){
                Toast.makeText(this,"This is unavailable in your country/region.",Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG,"user cancel login");
            }
            return;
        }

    }
}

