package com.lanren.app.imagepicker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.AnimRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by zhanglei on 16/4/26.
 * @version 1.0 at 2016/04/26
 * A <code>ImagePicker</code> is a {@link ViewGroup} contains an add-image view to add an image
 * and a remove-image for removing the chosen image.When the add-image view has been clicked,
 * a dialog will be appeared for taking and picking image.This component will create an ImageView
 * as the add-image view if user had not supplied a custom one assigned in XML.The activity instantiates
 * this view should add {@link OnImageAttachListener} to be notified whenever the image is added and
 * should add {@link OnImageRemoveListener} to be notified whenever the image is removed.
 * <strong>
 *     User can only assign one ImageView as the child view when user want to custom the add-image view,
 *     If multi views exist, the first ImageView will be selected as the add-image view.
 *     You must register {@link PhotoPickerDialog} in AndroidManifest.xml.
 * </strong>
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_addDrawable
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_removeDrawble
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_canRemoving
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_takingText
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_pickingText
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_cancelText
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_showConfirmDialog
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_dialogLayout
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_removeButtonOffset
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_locationType
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_pickerDialogEnterAnimation
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_pickerDialogExitAnimation
 * @attr ref com.anjujiang.app.demo.R.styleable#ImagePicker_pickerDialogStyle
 */
@SuppressWarnings("unused")
public class ImagePicker extends ViewGroup{
    private static final String TAG = "ImagePicker";
    public static final int DEFAULT_IMAGE_SIZE = 72;//dp
    public static final int DEFAULT_REMOVING_BUTTON_SIZE = 18;//dp
    /**
     * If the window has multi ImagePicker，this value can be used to specialize the
     * PickerView which user had clicked.
     * */
    private static long CURRENT_IDENTIFIER = 0;
    /**
     * The add-image view.
     */
    private ImageView mContentView;
    /**
     * the pixel size for {@link #mContentView}.
     * */
    private int mImageWidth, mImageHeight;

    // The remove-image button.
    private ImageButton mRemoveButton;
    /**
     * the pixel size for {@link #mRemoveButton}.
     * */
    private int mRemoveButtonWidth, mRemoveButtonHeight;
    // The background drawable of add-image view and remove-image button.
    private Drawable mAddDrawable, mRemoveDrawable;
    /**
     * The layout resource of dialog for picking picture.
     * The component will show a default dialog if you have not assigned the value.
     * */
    private @LayoutRes int mDialogResource;
    // If true, the removing button is visible after you have chosen a picture.
    private boolean mCanRemoving;
    // Text represents taking photo inside of picking dialog.
    private CharSequence mTakingText;
    // Text represents picking photo inside of picking dialog.
    private CharSequence mPickingText;
    // Text represents cancel inside of picking dialog.
    private CharSequence mCancelText;
    // If true, a confirm dialog will show after you have clicked the removing-button.
    private boolean mShowConfirmDialog;
    private @AnimRes int mPickerDialogEnterAnimation, mPickerDialogExitAnimation;
    private @StyleRes int mPickerDialogStyle;
    // An enum to define location type for remove-button.
    public static final int LOCATION_TYPE_TL = 0, LOCATION_TYPE_TR = 1, LOCATION_TYPE_BL = 2, LOCATION_TYPE_BR = 3;
    @IntDef(flag = true, value = {LOCATION_TYPE_TL, LOCATION_TYPE_TR, LOCATION_TYPE_BL, LOCATION_TYPE_BR})

    public @interface LocationType{}
    private static final @LocationType int[] sLocationTypeArray = {
            LOCATION_TYPE_TL, LOCATION_TYPE_TR, LOCATION_TYPE_BL, LOCATION_TYPE_BR
    };

    /**
     * Determine location of remove-button, value from {@link LocationType}
     * */
    private @LocationType int mLocationType = LOCATION_TYPE_TR;
    /**
     * This value to determine location of the remove-image button.By default, the value to guarantee the the remove-image button locates
     * at the top-right of add-image view.User can change the value to modify the size of add-image
     * view to locate the remove-image button wherever you what.If the {@link #mCanRemoving} is false,
     * this property is invalid.
     * @see #mLocationType
     * */
    private int mOffset;// unit pixel
    private OnImageAttachListener mAttachCallBack;
    private OnImageRemoveListener mRemoveCallBack;
    // Current PickerView's identifier.
    private long mIdentifier;
    // Uri of chosen image.
    private Uri mUri;
    public ImagePicker(Context context){
        super(context, null);
    }
    public ImagePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Populate value from attribute set.
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImagePicker);
        mAddDrawable = a.getDrawable(R.styleable.ImagePicker_addDrawable);
        mImageWidth = a.getDimensionPixelOffset(R.styleable.ImagePicker_imageWidth, 0);
        mImageHeight = a.getDimensionPixelOffset(R.styleable.ImagePicker_imageHeight, 0);
        mRemoveDrawable = a.getDrawable(R.styleable.ImagePicker_removeDrawable);
        mRemoveButtonWidth = a.getDimensionPixelOffset(R.styleable.ImagePicker_removeButtonWidth, 0);
        mRemoveButtonHeight = a.getDimensionPixelOffset(R.styleable.ImagePicker_removeButtonHeight, 0);
        mTakingText = a.getString(R.styleable.ImagePicker_takingText);
        mPickingText = a.getString(R.styleable.ImagePicker_pickingText);
        mCancelText = a.getString(R.styleable.ImagePicker_cancelText);
        mCanRemoving = a.getBoolean(R.styleable.ImagePicker_canRemoving, true);
        mShowConfirmDialog = a.getBoolean(R.styleable.ImagePicker_showConfirmDialog, false);
        mDialogResource = a.getResourceId(R.styleable.ImagePicker_dialogLayout, R.layout.dialog_pick_image);
        mOffset = a.getDimensionPixelOffset(R.styleable.ImagePicker_removeButtonOffset, 0);
        mLocationType = sLocationTypeArray[a.getInt(R.styleable.ImagePicker_locationType, 0)];
        mPickerDialogEnterAnimation = a.getResourceId(R.styleable.ImagePicker_pickerDialogEnterAnimation, 0);
        mPickerDialogExitAnimation = a.getResourceId(R.styleable.ImagePicker_pickerDialogExitAnimation, 0);
        mPickerDialogStyle = a.getResourceId(R.styleable.ImagePicker_pickerDialogStyle, R.style.dialog_picker_style);
        a.recycle();
        makeUnique();
    }

    /**
     * If multi ImagePicker displayed on one view, we need an identifier to tell which should be attached
     * after chosen image, generally use view's id as the identifier.If the user had not set view's id,
     * retrieve the current system time as the identifier.But we still recommend setting your own id.
     */
    private void makeUnique(){

        if((mIdentifier = getId()) == NO_ID){
            mIdentifier = System.currentTimeMillis();
        }
    }

    /**
     * When inflate completed, retrieve the child to initialize the property, if any.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();
    }

    private void initViews() {
        retrieveContentView();
        // If user has not supply a valid ImageView.Create a default one as the content view.
        if(mContentView == null){
            mContentView = createDefaultContentView();
            if(mContentView != null){
                addView(mContentView);
            }else{
                throw new IllegalArgumentException("You must supply a ImageView to attach image.");
            }
        }
        mContentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickerDialog();
                CURRENT_IDENTIFIER = mIdentifier;
            }
        });
        if(mAddDrawable == null){
            mContentView.setBackgroundResource(android.R.drawable.ic_input_add);
        }else{
            mContentView.setBackground(mAddDrawable);
        }
        if(mCanRemoving){
            mRemoveButton = createRemoveButton();
            if(mRemoveButton != null){
                addView(mRemoveButton);
                mRemoveButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptRemoveImage();
                    }
                });
            }else{
                Log.v(TAG, "You have set mRemoveButton true, but did not supplied a remove button.");
            }
        }
    }

    private void retrieveContentView(){
        // Lookup the first child ImageView.
        for(int i = 0, C = getChildCount(); i < C; i++){
            View childView = getChildAt(i);
            // If current view is ImageView,get it as the content view.
            if(childView instanceof ImageView){
                mContentView = (ImageView)childView;
                break;
            }
        }
    }
    /**
     * A method to create a ImageView as the content view and can be override.
     * @return  ImageView
     * */
    protected ImageView createDefaultContentView() {
        ImageView iv = new ImageButton(getContext());
        iv.setPadding(0, 0, 0, 0);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        return iv;
    }

    /**Display a dialog for user to pick picture.*/
    private void showPickerDialog() {
        Context ctx = getContext();
        Intent i = new Intent(ctx, PhotoPickerDialog.class);
        if(!TextUtils.isEmpty(mTakingText)){
            i.putExtra(PhotoPickerDialog.INTENT_FIELD_TAKING, mTakingText.toString());
        }
        if(!TextUtils.isEmpty(mPickingText)){
            i.putExtra(PhotoPickerDialog.INTENT_FIELD_PICKING, mPickingText.toString());
        }
        if(!TextUtils.isEmpty(mCancelText)){
            i.putExtra(PhotoPickerDialog.INTENT_FIELD_CANCEL, mCancelText.toString());
        }
        if(mDialogResource != 0){
            i.putExtra(PhotoPickerDialog.INTENT_FIELD_LAYOUT, mDialogResource);
        }
        if(mPickerDialogStyle != 0){
            i.putExtra(PhotoPickerDialog.INTENT_FIELD_THEME, mPickerDialogStyle);
        }
        if(mPickerDialogExitAnimation != 0){
            i.putExtra(PhotoPickerDialog.INTENT_FIELD_EXIT_ANIMATION, mPickerDialogExitAnimation);
        }
        ctx.startActivity(i);
        if(ctx instanceof Activity){
            ((Activity) ctx).overridePendingTransition(mPickerDialogEnterAnimation, 0);
        }
    }

    /**
     * Create a removing button.
     * @return Button
     * */
    protected ImageButton createRemoveButton(){
        ImageButton b = new ImageButton(getContext());
        b.setPadding(0, 0, 0, 0);
        b.setScaleType(ImageView.ScaleType.FIT_XY);
        b.setVisibility(GONE);
        if(mRemoveDrawable == null){
            b.setImageResource(android.R.drawable.ic_delete);
        }else{
            b.setImageDrawable(mRemoveDrawable);
        }
        return b;
    }

    protected Dialog createConfirmDialog(){
        return new AlertDialog.Builder(getContext())
                .setMessage(getContext().getString(R.string.confirm_to_delete))
                .setPositiveButton(getContext().getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeImage();
                    }
                }).setNegativeButton(getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }

    private void attemptRemoveImage() {
        if(mShowConfirmDialog){
            Dialog dialog = createConfirmDialog();
            if(dialog != null){
                dialog.show();
            }
        }else{
            removeImage();
        }
    }

    /**
     * Removing the chosen image from content view;
     */
    private void removeImage(){
        if(mRemoveCallBack != null){
            mRemoveCallBack.onImageRemove(this, mUri);
        }
        setImageURI(null);
    }

    public void setImageURI(Uri uri){
        mContentView.setImageURI(uri);
        if(mCanRemoving){
            mRemoveButton.setVisibility(uri == null ? GONE : VISIBLE);
        }else{
            mRemoveButton.setVisibility(GONE);
        }
        mUri = uri;
    }

    public void setAddDrawable(Drawable drawable){
        mContentView.setBackground(drawable);
        mAddDrawable = drawable;
    }

    public void setRemoveDrawable(Drawable drawable){
        if(mRemoveButton != null){
            mRemoveButton.setImageDrawable(drawable);
        }
        mRemoveDrawable = drawable;
    }

    public void setTakingText(CharSequence cs){
        mTakingText = cs;
    }

    public void setPickingText(CharSequence cs){
        mPickingText = cs;
    }

    public void setCancelText(CharSequence cs){
        mCancelText = cs;
    }

    public void setCanRemoving(boolean canRemoving){
        mCanRemoving = canRemoving;
    }

    public void setShowConfirmDialog(boolean isShow){
        mShowConfirmDialog = isShow;
    }

    public void setDialogResource(int resource){
        mDialogResource = resource;
    }

    public void setLocationType(@LocationType int type){
        mLocationType = type;
    }
    /**
     * @param offset unit is dp
     * */
    public void setRemoveButtonOffset(int offset){
        if(mCanRemoving){
            mOffset = (int)convertDpToPixel(offset);
            requestLayout();
        }
    }

    public void setOnImageAttachListener(OnImageAttachListener l){
        mAttachCallBack = l;
    }

    public void setOnImageRemoveListener(OnImageRemoveListener l){
        mRemoveCallBack = l;
    }

    public Uri getUri(){
        return mUri;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // If assigned child image size is greater than view's size, make child's size equals view's size.
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        adjustChild(measuredWidth, measuredHeight, mode);
        if(mOffset == 0){
            mOffset = mRemoveButtonWidth / 2;
        }
        Log.e(TAG, "RemoveButton has bean resized");
        mContentView.measure(MeasureSpec.makeMeasureSpec(mImageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mImageHeight, MeasureSpec.EXACTLY));
        mRemoveButton.measure(MeasureSpec.makeMeasureSpec(mRemoveButtonWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mRemoveButtonHeight, MeasureSpec.EXACTLY));
        setMeasuredDimension(mImageWidth + mOffset + getPaddingLeft() + getPaddingRight(), mImageHeight + mOffset + getPaddingTop() + getPaddingBottom());
    }

    private void adjustChild(int measuredWidth, int measuredHeight, int mode){
        if(MeasureSpec.EXACTLY == mode){
            if(mImageWidth > measuredWidth || mImageWidth == 0){
                mImageWidth = measuredWidth - mOffset;
                Log.e(TAG, "ImageView has bean resized");
            }
            if(mImageHeight > measuredHeight || mImageHeight == 0){
                mImageHeight = measuredHeight - mOffset;
                Log.e(TAG, "ImageView has bean resized");
            }
            // Enforce remove-button's max size less than half of view'size.
            if(mRemoveButtonWidth > measuredWidth / 2 || mRemoveButtonWidth == 0){
                mRemoveButtonWidth = measuredWidth / 2;
            }
            if(mRemoveButtonHeight > measuredHeight / 2 || mRemoveButtonHeight == 0){
                mRemoveButtonHeight = measuredHeight / 2;
                Log.e(TAG, "RemoveButton has bean resized");
            }
        }else{
            mImageWidth = mImageWidth == 0 ? (int)convertDpToPixel(DEFAULT_IMAGE_SIZE) : mImageWidth;
            mImageHeight = mImageHeight == 0 ? (int)convertDpToPixel(DEFAULT_IMAGE_SIZE) : mImageHeight;
            mRemoveButtonWidth = mRemoveButtonWidth == 0 ? (int)convertDpToPixel(DEFAULT_REMOVING_BUTTON_SIZE) : mRemoveButtonWidth;
            mRemoveButtonHeight = mRemoveButtonHeight == 0 ? (int)convertDpToPixel(DEFAULT_REMOVING_BUTTON_SIZE) : mRemoveButtonHeight;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutWithOffset();
    }

    private void layoutWithOffset(){
        switch (mLocationType){
            case LOCATION_TYPE_TL:
                if(mContentView != null){
                    mContentView.layout(mOffset, mOffset, mImageWidth + mOffset, mImageHeight + mOffset);
                }
                if(mRemoveButton != null){
                    // Layout removing Button
                    mRemoveButton.layout(0, 0, mRemoveButtonWidth, mRemoveButtonHeight);
                }
                break;
            case LOCATION_TYPE_TR:
                if(mContentView != null){
                    mContentView.layout(0, mOffset, mImageWidth, mImageWidth + mOffset);
                }
                if(mRemoveButton != null){
                    // Layout removing Button
                    mRemoveButton.layout(mImageWidth + mOffset - mRemoveButtonWidth, 0, mImageWidth + mOffset, mRemoveButtonHeight);
                }
                break;
            case LOCATION_TYPE_BL:
                if(mContentView != null){
                    mContentView.layout(mOffset, 0, mImageWidth + mOffset, mImageHeight);
                }
                if(mRemoveButton != null){
                    // Layout removing Button
                    mRemoveButton.layout(0, mImageHeight + mOffset - mRemoveButtonHeight, mRemoveButtonWidth, mImageHeight + mOffset);
                }
                break;
            case LOCATION_TYPE_BR:
                if(mContentView != null){
                    mContentView.layout(0, 0, mImageWidth , mImageHeight);
                }
                if(mRemoveButton != null){
                    // Layout removing Button
                    mRemoveButton.layout(mImageWidth + mOffset - mRemoveButtonWidth, mImageHeight + mOffset - mRemoveButtonHeight, mImageWidth + mOffset, mImageHeight + mOffset);
                }
                break;
        }
    }

    public long getCurrentIdentifier() {
        return mIdentifier;
    }

    private float convertDpToPixel(float dp){
        Resources r = getResources();
        DisplayMetrics dm = r.getDisplayMetrics();
        return dp * ((float)dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private PickerReceiver mReceiver;
    private static final String ACTION = "com.anjujiang.app.widgets.imagepicker.RECEIVER";
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mReceiver = new PickerReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, new IntentFilter(ACTION));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    /**A interface called when user has postProcessors such as upload file after chosen a picture.*/
    public interface OnImageAttachListener{
        void onImageAttach(ImagePicker picker, Uri uri);
    }
    /**A interface called when user has postProcessors such as delete file from sever after remove the picture.*/
    public interface OnImageRemoveListener{
        void onImageRemove(ImagePicker picker, Uri uri);
    }

    public class PickerReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(CURRENT_IDENTIFIER == mIdentifier){
                Uri uri = intent.getParcelableExtra("uri");
                setImageURI(uri);
                if(mAttachCallBack != null){
                    mAttachCallBack.onImageAttach(ImagePicker.this, uri);
                }
            }
        }
    }
    /**
     * The picking-dialog.
     * */
    public static class PhotoPickerDialog extends Activity {
        private static final int REQUEST_IMAGE_CAPTURE = 1;
        private static final int REQUEST_IMAGE_PICK = 2;
        private static final String INTENT_FIELD_THEME = "theme";
        private static final String INTENT_FIELD_TAKING = "taking";
        private static final String INTENT_FIELD_PICKING = "picking";
        private static final String INTENT_FIELD_CANCEL = "cancel";
        private static final String INTENT_FIELD_LAYOUT = "layout";
        private static final String INTENT_FIELD_EXIT_ANIMATION = "exit_animation";
        private @AnimRes
        int mExitAnimation;
        private Uri mCaptureTargetUri;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // If user has offered a dialog layout, use it.Otherwise, use a default one.
            Intent i = getIntent();
            if(i != null){
                int theme = i.getIntExtra(INTENT_FIELD_THEME, 0);
                if(theme == 0){
                    setTheme(R.style.dialog_picker_style);
                }else{
                    setTheme(theme);
                }
                int layoutId = i.getIntExtra(INTENT_FIELD_LAYOUT, 0);
                if(layoutId == 0){
                    setContentView(R.layout.dialog_pick_image);
                }else{
                    setContentView(layoutId);
                }
            }else{
                setContentView(R.layout.dialog_pick_image);
            }
            // Locate the position to bottom of screen.
            getWindow().setGravity(Gravity.BOTTOM);
            getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
            init();
        }

        private void init(){
            // Retrieve the parameter from intent and fill data.
            Intent i = getIntent();
            String takeText = "", pickText = "", cancelText = "";
            if(i != null){
                takeText = i.getStringExtra(INTENT_FIELD_TAKING);
                pickText = i.getStringExtra(INTENT_FIELD_PICKING);
                cancelText = i.getStringExtra(INTENT_FIELD_CANCEL);
                mExitAnimation = i.getIntExtra(INTENT_FIELD_EXIT_ANIMATION, 0);
            }
            TextView takeView = (TextView)findViewById(R.id.take_photo_tv);
            if(takeView != null){
                takeView.setText(takeText);
                takeView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent();
                    }
                });
            }else{
                Log.e(TAG, "Required id 'take_photo_tv'");
            }
            TextView pickView = (TextView)findViewById(R.id.pick_photo_tv);
            if(pickView != null){
                pickView.setText(pickText);
                pickView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchPickPictureIntent();
                    }
                });
            }else{
                Log.e(TAG, "Required id 'pick_photo_tv'");
            }
            TextView cancelView = (TextView)findViewById(R.id.cancel_tv);
            if(cancelView != null){
                cancelView.setText(cancelText);
                cancelView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }else{
                Log.e(TAG, "Required id 'cancel_tv'");
            }
        }

        private void dispatchTakePictureIntent() {
            mCaptureTargetUri = createImageUri();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCaptureTargetUri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }

        private void dispatchPickPictureIntent(){
            Intent intent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            } else {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
            }
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            Uri resultUri = null;
            if(resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE){
                resultUri = mCaptureTargetUri;
            }else if(resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_PICK){
                resultUri = data.getData();
            }
            Intent intent = new Intent();
            intent.setAction(ACTION);
            intent.putExtra("uri", resultUri);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            finish();
        }

        @Override
        public void finish() {

            //TODO Exit animation not working.
            super.finish();
            overridePendingTransition(0, mExitAnimation);

        }

        private Uri createImageUri() {
            ContentResolver contentResolver = getContentResolver();
            ContentValues cv = new ContentValues();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            cv.put(MediaStore.Images.Media.TITLE, timeStamp);
            return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        }
    }
}
