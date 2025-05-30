package com.tencent.liteav.demo.superplayer.helper;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

public class ContextUtils {

    /**
     * 从 Context 中递归查找 Activity 实例。
     * 如果找不到，则抛出 IllegalStateException。
     *
     * @param context 传入的上下文
     * @return 对应的 Activity 实例
     * @throws IllegalStateException 如果无法获取 Activity
     */
    public static Activity getActivityFromContext(Context context) {
        if (context == null) {
            Log.e("ContextUtils", "context is null");
            throw new IllegalStateException("Context is null and cannot be used to get Activity");
        }
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            return getActivityFromContext(baseContext);
        }

        Log.e("ContextUtils", "Cannot extract Activity from context: " + context.getClass().getName());
        throw new IllegalStateException("Context cannot be cast to Activity: " + context.getClass().getName());
    }

}