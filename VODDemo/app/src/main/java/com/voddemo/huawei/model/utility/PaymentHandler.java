package com.voddemo.huawei.model.utility;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.util.Log;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OwnedPurchasesReq;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.support.api.client.Status;
import com.voddemo.huawei.Constant;

import org.json.JSONException;

import java.util.ArrayList;

public class PaymentHandler {
    private static final String TAG = PaymentHandler.class.getSimpleName();

    /**
     * Load products information and show the products
     */
    public void loadProduct(final Activity activity, OnSuccessListener<ProductInfoResult> productInfoResultOnSuccessListener,
                            OnFailureListener onFailureListener) {
        // obtain in-app product details configured in AppGallery Connect, and then show the products
        IapClient iapClient = Iap.getIapClient(activity);
        Task<ProductInfoResult> task = iapClient.obtainProductInfo(createProductInfoReq());
        task.addOnSuccessListener(productInfoResultOnSuccessListener).addOnFailureListener(onFailureListener);
    }

    private ProductInfoReq createProductInfoReq() {
        ProductInfoReq req = new ProductInfoReq();
        // In-app product type contains:
        // 0: consumable
        // 1: non-consumable
        // 2: auto-renewable subscription
        req.setPriceType(IapClient.PriceType.IN_APP_SUBSCRIPTION);
        ArrayList<String> productIds = new ArrayList<>();
        // Pass in the item_productId list of products to be queried.
        // The product ID is the same as that set by a developer when configuring product information in AppGallery Connect.
        productIds.add(Constant.DEMO_PRODUCT_ID);
        req.setProductIds(productIds);
        return req;
    }

    /**
     * create orders for in-app products in the PMS.
     * @param activity indicates the activity object that initiates a request.
     * @param productId ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @param type  In-app product type.
     */
    public void gotoPay(final Activity activity, String productId, int type) {
        Log.i(TAG, "call createPurchaseIntent");
        IapClient mClient = Iap.getIapClient(activity);
        Task<PurchaseIntentResult> task = mClient.createPurchaseIntent(createPurchaseIntentReq(type, productId));
        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                Log.i(TAG, "createPurchaseIntent, onSuccess");
                if (result == null) {
                    Log.e(TAG, "result is null");
                    return;
                }
                Status status = result.getStatus();
                if (status == null) {
                    Log.e(TAG, "status is null");
                    return;
                }
                // you should pull up the page to complete the payment process.
                if (status.hasResolution()) {
                    try {
                        status.startResolutionForResult(activity, Constant.REQ_CODE_BUY);
                    } catch (IntentSender.SendIntentException exp) {
                        Log.e(TAG, exp.getMessage());
                    }
                } else {
                    Log.e(TAG, "intent is null");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    int returnCode = apiException.getStatusCode();
                    Log.e(TAG, "createPurchaseIntent, returnCode: " + returnCode);
                    // handle error scenarios
                } else {
                    // Other external errors
                }

            }
        });
    }

    /**
     * Create a PurchaseIntentReq instance.
     * @param type In-app product type.
     * @param productId ID of the in-app product to be paid.
     *              The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @return PurchaseIntentReq
     */
    private PurchaseIntentReq createPurchaseIntentReq(int type, String productId) {
        PurchaseIntentReq req = new PurchaseIntentReq();
        req.setProductId(productId);
        req.setPriceType(type);
        req.setDeveloperPayload("test");
        return req;
    }

    /**
     * Consume the unconsumed purchase with type 0 after successfully delivering the product, then the Huawei payment server will update the order status and the user can purchase the product again.
     * @param inAppPurchaseData JSON string that contains purchase order details.
     */
    public void consumeOwnedPurchase(final Context context, String inAppPurchaseData) {
        Log.i(TAG, "call consumeOwnedPurchase");
        IapClient mClient = Iap.getIapClient(context);
        Task<ConsumeOwnedPurchaseResult> task = mClient.consumeOwnedPurchase(createConsumeOwnedPurchaseReq(inAppPurchaseData));
        task.addOnSuccessListener(new OnSuccessListener<ConsumeOwnedPurchaseResult>() {
            @Override
            public void onSuccess(ConsumeOwnedPurchaseResult result) {
                // Consume success
                Log.i(TAG, "consumeOwnedPurchase success");
                Toast.makeText(context, "Pay success, and the product has been delivered", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                    Log.e(TAG, "consumeOwnedPurchase fail,returnCode: " + returnCode);
                } else {
                    // Other external errors
                }

            }
        });
    }

    /**
     * Create a ConsumeOwnedPurchaseReq instance.
     * @param purchaseData JSON string that contains purchase order details.
     * @return ConsumeOwnedPurchaseReq
     */
    private ConsumeOwnedPurchaseReq createConsumeOwnedPurchaseReq(String purchaseData) {

        ConsumeOwnedPurchaseReq req = new ConsumeOwnedPurchaseReq();
        // Parse purchaseToken from InAppPurchaseData in JSON format.
        try {
            InAppPurchaseData inAppPurchaseData = new InAppPurchaseData(purchaseData);
            req.setPurchaseToken(inAppPurchaseData.getPurchaseToken());
        } catch (JSONException e) {
            Log.e(TAG, "createConsumeOwnedPurchaseReq JSONExeption");
        }

        return req;
    }

    /**
     * Create a getOwnedPurchaseRecord instance.
     *
     * @param activity indicates the activity object that initiates a request.
     * @param ownedPurchasesResultOnSuccessListener
     * @param failureListener
     * @return ConsumeOwnedPurchaseReq
     */
    public void getOwnedPurchases(Activity activity,
                                   OnSuccessListener<OwnedPurchasesResult> ownedPurchasesResultOnSuccessListener,
                                   OnFailureListener failureListener) {
        // Constructs a OwnedPurchasesReq object.
        OwnedPurchasesReq ownedPurchasesReq = new OwnedPurchasesReq();
        // In-app product type contains:
        // priceType: 0: consumable; 1: non-consumable; 2: auto-renewable subscription
        ownedPurchasesReq.setPriceType(IapClient.PriceType.IN_APP_SUBSCRIPTION);
        // to call the obtainOwnedPurchases API
        // To get the Activity instance that calls this API.
        Task<OwnedPurchasesResult> task = Iap.getIapClient(activity).obtainOwnedPurchases(ownedPurchasesReq);
        task.addOnSuccessListener(ownedPurchasesResultOnSuccessListener).addOnFailureListener(failureListener);
    }

    /**
     * Create a getOwnedPurchaseRecord instance.
     *
     * @param activity indicates the activity object that initiates a request.
     * @param ownedPurchasesResultOnSuccessListener
     * @param failureListener
     * @return ConsumeOwnedPurchaseReq
     */
    public void getOwnedPurchaseRecord(Activity activity, OnSuccessListener<OwnedPurchasesResult> ownedPurchasesResultOnSuccessListener, OnFailureListener failureListener) {
        // Constructs a OwnedPurchasesReq object.
        OwnedPurchasesReq req = new OwnedPurchasesReq();
        // In-app product type contains:
        // 0: consumable; 1: non-consumable; 2: auto-renewable subscription
        req.setPriceType(IapClient.PriceType.IN_APP_SUBSCRIPTION);
        // to call the obtainOwnedPurchaseRecord API.
        // To get the Activity instance that calls this API.
        Task<OwnedPurchasesResult> task = Iap.getIapClient(activity).obtainOwnedPurchaseRecord(req);
        task.addOnSuccessListener(ownedPurchasesResultOnSuccessListener).addOnFailureListener(failureListener);
    }
}
