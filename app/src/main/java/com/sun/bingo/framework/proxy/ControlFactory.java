package com.sun.bingo.framework.proxy;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.sun.bingo.framework.proxy.callback.AsyncMethodAtomInterceptor;
import com.sun.bingo.framework.proxy.callback.Interceptor;
import com.sun.bingo.framework.proxy.filter.AsyncMethodFilter;

import java.io.File;

public class ControlFactory {

    private static File mCacheDir;

    public static void init(Context context) {
        int version = getVersionCode(context);
        for (int i = 1; i < version; i++) {
            File file = context.getDir(i + "", Context.MODE_PRIVATE);
            deleteDirection(file);
        }
        mCacheDir = context.getDir(version + "", Context.MODE_PRIVATE);
    }

    private static int getVersionCode(Context context) {
        // 获取packagemanager的实例
        int version = 0;
        try {
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionCode;
        } catch (Exception exception) {
        }
        return version;
    }

    private static boolean deleteDirection(File file) {
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            int length = files.length;
            for (int i = 0; i < length; i++) {
                files[i].delete();
            }
            file.delete();
        }
        return false;
    }

    /**
     * control 代理工厂
     *
     * @param clazz
     * @return
     */
    public static <T> T getControlInstance(Class<T> clazz, MessageProxy mMethodCallBack) {
        Enhancer<T> enhancer;
        if (mMethodCallBack != null) {
            enhancer = new Enhancer<T>(mCacheDir, clazz,
                    new Class[]{mMethodCallBack.getClass()},
                    new Object[]{mMethodCallBack});
        } else {
            enhancer = new Enhancer<T>(mCacheDir, clazz);
        }

        /**
         * 添加拦截器
         */
        enhancer.addCallBacks(new Interceptor[]{new AsyncMethodAtomInterceptor(mMethodCallBack)});
        /**
         * 添加过滤器
         */
        enhancer.addFilter(new AsyncMethodFilter());

        return enhancer.create();
    }

    /**
     * 构建无参数构造函数
     *
     * @param clazz
     * @return
     */
    public static <T> T getControlInstance(Class<T> clazz) {
        return getControlInstance(clazz, null);
    }

}
