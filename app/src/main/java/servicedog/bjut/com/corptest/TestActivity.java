package servicedog.bjut.com.corptest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TImage;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;

import static android.R.attr.maxHeight;
import static android.R.attr.maxWidth;

/**
 * Created by beibeizhu on 17/3/1.
 */

public class TestActivity extends AppCompatActivity implements TakePhoto.TakeResultListener, InvokeListener {

    private CustomHelper mCustomHelper;
    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private ArrayList<TImage> images = new ArrayList<>();
    private int index = 0;
    private UCrop.Options options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        takePhoto = getTakePhoto();
        takePhoto.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mCustomHelper = new CustomHelper();

        options=new UCrop.Options();
        options.setToolbarColor(getResources().getColor(R.color.multiple_image_select_primaryDark));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        takePhoto.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            index++;
            if (index < images.size()) {
                crop(index);
            } else {
                Toast.makeText(getApplicationContext(), "全部完成", Toast.LENGTH_SHORT).show();
            }
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

    public void take(View view) {
        mCustomHelper.onClick(view, getTakePhoto());
    }

    @Override
    public void takeSuccess(TResult result) {
        images = result.getImages();
        index = 0;
        crop(index);
    }


    public void crop(int position) {
        String compressPath = images.get(position).getCompressPath();
        Uri imageContentUri = UriUtils.getImageContentUri(TestActivity.this, new File(compressPath));
        File file = new File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        Uri destinationUri = Uri.fromFile(file);
        UCrop.of(imageContentUri, destinationUri)
                .withOptions(options)
//                .withAspectRatio(1, 1)
                .withMaxResultSize(maxWidth, maxHeight)
                .start(TestActivity.this);
    }


    @Override
    public void takeFail(TResult result, String msg) {

    }

    @Override
    public void takeCancel() {

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
