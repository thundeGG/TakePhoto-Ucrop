package servicedog.bjut.com.corptest;

import android.net.Uri;
import android.os.Environment;
import android.view.View;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.LubanOptions;
import com.jph.takephoto.model.TakePhotoOptions;

import java.io.File;


/**
 * - 支持通过相机拍照获取图片
 * - 支持从相册选择图片
 * - 支持从文件选择图片
 * - 支持多图选择
 * - 支持批量图片裁切
 * - 支持批量图片压缩
 * - 支持对图片进行压缩
 * - 支持对图片进行裁剪
 * - 支持对裁剪及压缩参数自定义
 * - 提供自带裁剪工具(可选)
 * - 支持智能选取及裁剪异常处理
 * - 支持因拍照Activity被回收后的自动恢复
 * Author: crazycodeboy
 * Date: 2016/9/21 0007 20:10
 * Version:4.0.0
 * 技术博文：http://www.cboy.me
 * GitHub:https://github.com/crazycodeboy
 * Eamil:crazycodeboy@gmail.com
 */
public class CustomHelper {

    private boolean isCrop = false;//是否剪裁
    private boolean isCompress = true;//是否压缩
    private boolean isShowProgressBar = true;//压缩中是否显示
    private boolean isEnableRawFile = false;//压缩后是否保存原图
    private boolean isCompressTool = true;//压缩工具默认用自带的，false为鲁班
    private boolean isCropOwn = true;//压缩剪裁工具默认用自带的，false为第三方
    private boolean isGallery = true;//从哪里选择图片  true相册  false文件
    private boolean isPickWithOwn = true;//使用自带相册  false使用系统相册
    private boolean isCorrect = true;//是否自动纠正拍照角度
    private int limit = 5;//最大选择数量
    private int maxSize = 102400;//压缩最大值不超过
    int width = 800;//宽
    int height = 800;//高
    private int etCropWidth = 3;
    private int etCropHeight = 2;

    public boolean isCrop() {
        return isCrop;
    }

    public void setCrop(boolean crop) {
        isCrop = crop;
    }

    public boolean isCompress() {
        return isCompress;
    }

    public void setCompress(boolean compress) {
        isCompress = compress;
    }

    public boolean isShowProgressBar() {
        return isShowProgressBar;
    }

    public void setShowProgressBar(boolean showProgressBar) {
        isShowProgressBar = showProgressBar;
    }

    public boolean isEnableRawFile() {
        return isEnableRawFile;
    }

    public void setEnableRawFile(boolean enableRawFile) {
        isEnableRawFile = enableRawFile;
    }

    public boolean isCompressTool() {
        return isCompressTool;
    }

    public void setCompressTool(boolean compressTool) {
        isCompressTool = compressTool;
    }

    public boolean isCropOwn() {
        return isCropOwn;
    }

    public void setCropOwn(boolean cropOwn) {
        isCropOwn = cropOwn;
    }

    public boolean isGallery() {
        return isGallery;
    }

    public void setGallery(boolean gallery) {
        isGallery = gallery;
    }

    public boolean isPickWithOwn() {
        return isPickWithOwn;
    }

    public void setPickWithOwn(boolean pickWithOwn) {
        isPickWithOwn = pickWithOwn;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getEtCropWidth() {
        return etCropWidth;
    }

    public void setEtCropWidth(int etCropWidth) {
        this.etCropWidth = etCropWidth;
    }

    public int getEtCropHeight() {
        return etCropHeight;
    }

    public void setEtCropHeight(int etCropHeight) {
        this.etCropHeight = etCropHeight;
    }

    public static CustomHelper of() {
        return new CustomHelper();
    }

    public void onClick(View view, TakePhoto takePhoto) {
        File file = new File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        Uri imageUri = Uri.fromFile(file);

        configCompress(takePhoto);
        configTakePhotoOption(takePhoto);
        switch (view.getId()) {
            case R.id.btn_choise:
                if (limit > 1) {
                    if (isCrop) {
                        takePhoto.onPickMultipleWithCrop(limit, getCropOptions());
                    } else {
                        takePhoto.onPickMultiple(limit);
                    }
                    return;
                }
                if (!isGallery) {
                    if (isCrop) {
                        takePhoto.onPickFromDocumentsWithCrop(imageUri, getCropOptions());
                    } else {
                        takePhoto.onPickFromDocuments();
                    }
                    return;
                } else {
                    if (isCrop) {
                        takePhoto.onPickFromGalleryWithCrop(imageUri, getCropOptions());
                    } else {
                        takePhoto.onPickFromGallery();
                    }
                }
                break;
            case R.id.btn_take:
                if (isCrop) {
                    takePhoto.onPickFromCaptureWithCrop(imageUri, getCropOptions());
                } else {
                    takePhoto.onPickFromCapture(imageUri);
                }
                break;
            default:
                break;
        }
    }

    private void configTakePhotoOption(TakePhoto takePhoto) {
        TakePhotoOptions.Builder builder = new TakePhotoOptions.Builder();
        if (isPickWithOwn) {
            builder.setWithOwnGallery(true);
        }
        if (isCorrect) {
            builder.setCorrectImage(true);
        }
        takePhoto.setTakePhotoOptions(builder.create());

    }

    private void configCompress(TakePhoto takePhoto) {
        if (!isCompress) {
            takePhoto.onEnableCompress(null, false);
            return;
        }
        CompressConfig config;
        if (isCompressTool) {
            config = new CompressConfig.Builder()
                    .setMaxSize(maxSize)
                    .setMaxPixel(width >= height ? width : height)
                    .enableReserveRaw(isEnableRawFile)
                    .create();
        } else {
            LubanOptions option = new LubanOptions.Builder()
                    .setMaxHeight(height)
                    .setMaxWidth(width)
                    .setMaxSize(maxSize)
                    .create();
            config = CompressConfig.ofLuban(option);
            config.enableReserveRaw(isEnableRawFile);
        }
        takePhoto.onEnableCompress(config, isShowProgressBar);
    }

    private CropOptions getCropOptions() {
        if (!isCrop) return null;
        CropOptions.Builder builder = new CropOptions.Builder();
        builder.setAspectX(3).setAspectX(2);
        builder.setWithOwnCrop(isCropOwn);
        return builder.create();
    }

}
