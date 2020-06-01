package com.oldsboy.applypermissions;

/**
 * @ProjectName: BaseWebView
 * @Package: com.oldsboy.basewebview
 * @ClassName: PermissionsUtils
 * @Description: java类作用描述
 * @Author: 作者名 oldsboy
 * @CreateDate: 2020/5/25 16:26
 * @UpdateUser: 更新者：
 * @UpdateDate: 2020/5/25 16:26
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限工具类
 */

public class PermissionsUtils {
    private final int mRequestCode = 100;//权限请求码
    public static boolean showSystemSetting = true;

    private PermissionsUtils() {
    }

    private static PermissionsUtils permissionsUtils;
    private IPermissionsResult mPermissionsResult;

    public static PermissionsUtils getInstance() {
        if (permissionsUtils == null) {
            permissionsUtils = new PermissionsUtils();
        }
        return permissionsUtils;
    }

    public boolean checkPermissions(final Activity activity, String[] permissions, IPermissionsResult permissionsResult) {
        //创建监听权限的接口对象
        if (permissionsResult == null) {
            permissionsResult = new PermissionsUtils.IPermissionsResult() {
                @Override
                public void passPermissons() {
                    Log.d(this.getClass().getSimpleName(), "权限通过，可以做其他事情!");
                }

                @Override
                public void forbitPermissons() {
                    Log.d(this.getClass().getSimpleName(), "权限不通过!功能不能用");
                    Toast.makeText(activity, "权限不通过!功能不能用", Toast.LENGTH_SHORT).show();
                }
            };
        }
        mPermissionsResult = permissionsResult;

        if (Build.VERSION.SDK_INT < 23) {//6.0才用动态权限
            permissionsResult.passPermissons();
            return true;
        }

        //创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
        List<String> mPermissionList = new ArrayList<>();
        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(activity, permissions, mRequestCode);
            return false;
        } else {
            //说明权限都已经通过，可以做你想做的事情去
            permissionsResult.passPermissons();
            return true;
        }
    }

    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    public void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                if (showSystemSetting) {
                    showSystemPermissionsSettingDialog(activity);//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
                } else {
                    if (mPermissionsResult != null) {
                        mPermissionsResult.forbitPermissons();
                    }
                }
            } else {
                //全部权限通过，可以进行下一步操作。。。
                if (mPermissionsResult != null) {
                    mPermissionsResult.passPermissons();
                }
            }
        }
    }

    /**
     * 不再提示权限时的展示对话框
     */
    Dialog dialog;
    private void showSystemPermissionsSettingDialog(final Activity activity) {
        if (dialog == null) {
            dialog = new Dialog(activity, R.style.MyDialog);
            dialog.setContentView(LinearLayout.inflate(activity, R.layout.dialog_permission, null));
            dialog.show();
            dialog.findViewById(R.id.tv_setting).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelPermissionDialog();

                    Uri packageURI = Uri.parse("package:" + activity.getPackageName());
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    activity.startActivity(intent);
                    activity.finish();
                }
            });
            dialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //关闭页面或者做其他操作
                    cancelPermissionDialog();
                    if (mPermissionsResult != null) {
                        mPermissionsResult.forbitPermissons();
                    }
                }
            });
            if (dialog.getWindow() != null) {
                Window window = dialog.getWindow();
                window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.width = px2dp(activity, 300);
                attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(attributes);
                window.getDecorView().setPadding(0, 0, 0, px2dp(activity, 40));
            }
        }
        dialog.show();
    }

    private int px2dp(Activity activity, int px){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px,activity.getResources().getDisplayMetrics());
    }

    //关闭对话框
    private void cancelPermissionDialog() {
        if (dialog != null) {
            dialog.cancel();
            dialog = null;
        }
    }


    public interface IPermissionsResult {
        void passPermissons();

        void forbitPermissons();
    }
}


