package servicedog.bjut.com.corptest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TImage;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.model.TakePhotoOptions;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TakePhoto.TakeResultListener, InvokeListener {

    private ArrayList<TImage> images;
    private TakePhoto takePhoto;
    private InvokeParam invokeParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        takePhoto = getTakePhoto();
        takePhoto.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void take(View view) {
        TakePhoto takePhoto = getTakePhoto();
//        takePhoto.onPickMultiple(5);
        CompressConfig config = new CompressConfig.Builder()
                .setMaxSize(51200)
                .setMaxPixel(500)
                .enableReserveRaw(false)
                .enableQualityCompress(true)
                .create();
        takePhoto.onEnableCompress(config, true);
        CropOptions cropOptions = new CropOptions.Builder().setAspectX(1).setAspectY(1).setWithOwnCrop(true).create();
        File file = new File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        Uri imageUri = Uri.fromFile(file);
        takePhoto.onPickMultipleWithCrop(5,cropOptions);
//        takePhoto.onPickFromCapture(imageUri);
//        takePhoto.onPickFromCaptureWithCrop(uri,cropOptions);
//        takePhoto.onPickFromDocumentsWithCrop(uri,cropOptions);

        //或
//        takePhoto.onCrop(imageUri,outPutUri,cropOptions);
    }

    private void configTakePhotoOption(TakePhoto takePhoto) {
        TakePhotoOptions.Builder builder = new TakePhotoOptions.Builder();
        builder.setWithOwnGallery(true);
        builder.setCorrectImage(true);
        takePhoto.setTakePhotoOptions(builder.create());
    }

    public void crop(View view) {
        String path = "/storage/emulated/0/DCIM/P70301-143400.jpg";
        Uri uri = Uri.parse(path);
        startCropActivity(uri);
    }

    private void startCropActivity(@NonNull Uri uri) {
        String destinationFileName = "aaaa.png";
        Uri outputUri = Uri.fromFile(new File(getCacheDir(), "aaa.png"));
        UCrop uCrop = UCrop.of(uri, outputUri);

        uCrop = advancedConfig(uCrop);

        uCrop.start(MainActivity.this);
    }

    /**
     * Sometimes you want to adjust more options, it's done via {@link com.yalantis.ucrop.UCrop.Options} class.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();

        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(90);

        options.setHideBottomControls(false);
        return uCrop.withOptions(options);
    }


    @Override
    public void takeSuccess(TResult result) {
        String originalPath = result.getImage().getOriginalPath();
        Log.i("zl", "images==========" + originalPath);

//        images = result.getImages();
//        Log.i("zl", "images==========" + images.size() + "");
//        final Uri uri = Uri.parse(images.get(0).getOriginalPath());
//        Log.i("zl", "path==========" + uri.getPath() + "");
//        Log.i("zl", "path==========" + images.get(0).getOriginalPath() + "");
    }

    @Override
    public void takeFail(TResult result, String msg) {
        Log.i("zl", "msg==========" + msg);
    }

    @Override
    public void takeCancel() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //以下代码为处理Android6.0、7.0动态权限所需
        PermissionManager.TPermissionType type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handlePermissionsResult(this, type, invokeParam, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        takePhoto.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Log.i("zl", "path==========" + resultUri.getPath() + "");
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }

    }

    /**
     * 获取TakePhoto实例
     *
     * @return
     */
    public TakePhoto getTakePhoto() {
        if (takePhoto == null) {
            takePhoto = (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this, this));
        }
        return takePhoto;
    }

    @Override
    public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
        PermissionManager.TPermissionType type = PermissionManager.checkPermission(TContextWrap.of(this), invokeParam.getMethod());
        if (PermissionManager.TPermissionType.WAIT.equals(type)) {
            this.invokeParam = invokeParam;
        }
        return type;
    }
}
