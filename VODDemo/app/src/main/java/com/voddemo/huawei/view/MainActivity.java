package com.voddemo.huawei.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.voddemo.huawei.BaseApplication;
import com.voddemo.huawei.Constant;
import com.voddemo.huawei.R;
import com.voddemo.huawei.model.utility.CipherUtil;
import com.voddemo.huawei.model.utility.ImageHandler;
import com.voddemo.huawei.model.utility.PaymentHandler;

import org.json.JSONException;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private ImageView profileImage;
    private TextView profileName;
    private VideoView videoView;
    private ImageButton btnPlayPause;
    private ImageView imageViewOverLayer;

    private ProgressDialog dialog;

    private AuthHuaweiId authHuaweiId;

    private String videoURL = "https://pureix.com/huawei/test_video.mp4";
    private boolean isVideoLoaded = false;

    private PaymentHandler paymentHandler;
    private List<ProductInfo> productInfoList;
    private boolean itemOwned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        videoView = findViewById(R.id.video_view);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        imageViewOverLayer = findViewById(R.id.image_view_over_layer);

        authHuaweiId = BaseApplication.getInstance().huaweiAccount;

        if(authHuaweiId == null){
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }

        new ImageHandler().loadImageWithGlide(this, authHuaweiId.getAvatarUriString(), profileImage);
        profileName.setText(authHuaweiId.getDisplayName());
        toolbar.setTitle(authHuaweiId.getDisplayName());
        setSupportActionBar(toolbar);

        paymentHandler = new PaymentHandler();

        checkOwnedPurchasesItems();

        paymentHandler.getOwnedPurchases(this, new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                // Obtain the execution result.
                if (result != null && !result.getInAppPurchaseDataList().isEmpty()) {
                    for (int i = 0; i < result.getInAppPurchaseDataList().size(); i++) {
                        String inAppPurchaseData = result.getInAppPurchaseDataList().get(i);
                        String InAppSignature = result.getInAppSignature().get(i);
                        // use the payment public key to verify the signature of the inAppPurchaseData.
                        // if success.
                        try {
                            InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);
                            int purchaseState = inAppPurchaseDataBean.getPurchaseState();
                        } catch (JSONException e) {
                        }
                    }
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                } else {
                    // Other external errors
                }
            }
        });

        imageViewOverLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemOwned) {
                    playVideo(videoURL);
                }else{
                    subscribe();
                }
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemOwned) {
                    playVideo(videoURL);
                }else{
                    subscribe();
                }
            }
        });
    }

    private void playVideo(String videoURL) {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        try {
            if(!videoView.isPlaying()) {
                if(!isVideoLoaded) {
                    Uri uri = Uri.parse(videoURL);
                    videoView.setVideoURI(uri);
                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            imageViewOverLayer.setVisibility(View.VISIBLE);
                            btnPlayPause.setImageResource(R.drawable.ic_play);
                            imageViewOverLayer.setBackgroundResource(R.color.screen_background_dark_transparent);
                            imageViewOverLayer.setImageResource(R.drawable.ic_play);
                            imageViewOverLayer.setVisibility(View.VISIBLE);
                        }
                    });
                }else{
                    videoView.start();
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                    dialog.dismiss();
                    imageViewOverLayer.setVisibility(View.VISIBLE);
                    imageViewOverLayer.setBackgroundResource(0);
                    imageViewOverLayer.setImageResource(0);
                }
            } else {
                videoView.pause();
                btnPlayPause.setImageResource(R.drawable.ic_play);
                dialog.dismiss();
                imageViewOverLayer.setBackgroundResource(R.color.screen_background_dark_transparent);
                imageViewOverLayer.setImageResource(R.drawable.ic_play);
                imageViewOverLayer.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            dialog.dismiss();
        }

        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isVideoLoaded = true;
                dialog.dismiss();
                mp.setLooping(true);
                videoView.start();
                btnPlayPause.setImageResource(R.drawable.ic_pause);
                imageViewOverLayer.setVisibility(View.VISIBLE);
                imageViewOverLayer.setBackgroundResource(0);
                imageViewOverLayer.setImageResource(0);
            }
        });
    }

    private void subscribe() {
        if(productInfoList != null && !productInfoList.isEmpty()) {
            for (int i = 0; i < productInfoList.size(); i++) {
                if(productInfoList.get(i).getProductId().equals(Constant.DEMO_PRODUCT_ID)){
                    String productId = productInfoList.get(i).getProductId();
                    paymentHandler.gotoPay(MainActivity.this, productId, IapClient.PriceType.IN_APP_SUBSCRIPTION);
                }
            }
        }
    }

    private void checkOwnedPurchasesItems() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        paymentHandler.getOwnedPurchaseRecord(this, new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                // Obtain the execution result.
                if(result != null && !result.getItemList().isEmpty()) {
                    for (int i = 0; i < result.getItemList().size(); i++) {
                        if (result.getItemList().get(i).equals(Constant.DEMO_PRODUCT_ID)) {
                            itemOwned = true;
                            dialog.dismiss();
                            return;
                        }
                    }
                    itemOwned = false;
                    // if Product item not owned
                    loadProducts();
                }else{
                    // if Product item not owned
                    loadProducts();
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    int returnCode = apiException.getStatusCode();
                } else {
                    // Other external errors
                }
                // if Product item not owned
                loadProducts();
            }
        });
    }

    private void loadProducts() {
        paymentHandler.loadProduct(this, new OnSuccessListener<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                if (result != null && !result.getProductInfoList().isEmpty()) {
                    productInfoList = result.getProductInfoList();
                    Log.d(TAG, result.getProductInfoList().toString());
                }
                dialog.dismiss();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQ_CODE_BUY) {
            if (data == null) {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                return;
            }
            PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    // verify signature of payment results.
                    boolean success = CipherUtil.doCheck(purchaseResultInfo.getInAppPurchaseData(),
                            purchaseResultInfo.getInAppDataSignature(), Constant.getPublicKey());
                    if (success) {
                        itemOwned = true;
                        // Call the consumeOwnedPurchase interface to consume it after successfully delivering the product to your user.
                        paymentHandler.consumeOwnedPurchase(this, purchaseResultInfo.getInAppPurchaseData());
                    } else {
                        Toast.makeText(this, "Pay successful, sign failed", Toast.LENGTH_SHORT).show();
                    }
                    return;
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    // The User cancels payment.
                    Toast.makeText(this, "user cancel", Toast.LENGTH_SHORT).show();
                    return;
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    itemOwned = true;
                    // The user has already owned the product.
                    Toast.makeText(this, "you have owned the product", Toast.LENGTH_SHORT).show();
                    // you can check if the user has purchased the product and decide whether to provide goods
                    // if the purchase is a consumable product, consuming the purchase and deliver product
                    return;

                default:
                    Toast.makeText(this, "Pay failed", Toast.LENGTH_SHORT).show();
                    break;
            }
            return;
        }

    }

}
