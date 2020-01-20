package com.voddemo.huawei.model.utility;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class ImageHandler {
    public synchronized void loadImageWithGlide(final Context context,
                                                      String url, final ImageView imageView) {
        if (url != null) {
            if (!url.equalsIgnoreCase("")) {
                Glide.with(context)
                        .load(url)
                        .into(imageView);
            }
        }
    }
}
