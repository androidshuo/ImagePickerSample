package com.lanren.app.imagepickersample;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.lanren.app.imagepicker.ImagePicker;

/**
 * @author zhanglei at 2016/04/27
 */
public class ImagePickerDemo extends AppCompatActivity implements ImagePicker.OnImageAttachListener, ImagePicker.OnImageRemoveListener{

    private static final String TAG = "ImagePickerDemo";
    private static final SparseIntArray ADD_DRAWABLE_COLLECTION = new SparseIntArray(){
        {
            put(R.id.add_drawable_rb1, R.mipmap.ic_add_image1);
            put(R.id.add_drawable_rb2, R.mipmap.ic_add_image2);
        }
    };
    private static final SparseIntArray REMOVE_DRAWABLE_COLLECTION = new SparseIntArray(){
        {
            put(R.id.remove_drawable_rb1, R.mipmap.ic_remove_image1);
            put(R.id.remove_drawable_rb2, R.mipmap.ic_remove_image2);
        }
    };
    private static final SparseIntArray DIALOG_LAYOUT_COLLECTION = new SparseIntArray(){
        {
            put(R.id.dialog_layout_rb1, R.layout.dialog_photo_picker1);
            put(R.id.dialog_layout_rb2, R.layout.dialog_photo_picker2);
        }
    };
    private static final SparseIntArray LOCATION_TYPE_COLLECTION = new SparseIntArray(){
        {
            put(R.id.location_type_rb1, ImagePicker.LOCATION_TYPE_TL);
            put(R.id.location_type_rb2, ImagePicker.LOCATION_TYPE_TR);
            put(R.id.location_type_rb3, ImagePicker.LOCATION_TYPE_BL);
            put(R.id.location_type_rb4, ImagePicker.LOCATION_TYPE_BR);

        }
    };
    private boolean mShowCode = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker_demo);
        setupActionBar();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("图片选择控件");
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final ImagePicker ip = (ImagePicker)findViewById(R.id.ip);
        if(ip != null){
            RadioGroup addDrawableView = (RadioGroup)findViewById(R.id.add_drawable_rg);
            if(addDrawableView != null){
                addDrawableView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        View childView = group.findViewById(checkedId);
                        ip.setAddDrawable(ContextCompat.getDrawable(ImagePickerDemo.this, ADD_DRAWABLE_COLLECTION.get(checkedId)));
                        showCode("ImagePicker.setAddDrawable(...)");
                    }
                });
            }
            RadioGroup removeDrawableView = (RadioGroup)findViewById(R.id.remove_drawable_rg);
            if(removeDrawableView != null){
                removeDrawableView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        View childView = group.findViewById(checkedId);
                        ip.setRemoveDrawable(ContextCompat.getDrawable(ImagePickerDemo.this, REMOVE_DRAWABLE_COLLECTION.get(checkedId)));
                        showCode("ImagePicker.setRemoveDrawable(...)");
                    }
                });
            }
            EditText takingTextView = (EditText)findViewById(R.id.takingText_et);
            if(takingTextView != null){
                takingTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        ip.setTakingText(s);
                    }
                });
                takingTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            showCode("ImagePicker.setTakingText(...)");
                        }
                    }
                });
            }
            EditText pickingTextView = (EditText)findViewById(R.id.pickingText_et);
            if(pickingTextView != null){
                pickingTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        ip.setPickingText(s);
                    }
                });
                pickingTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            showCode("ImagePicker.setPickingText(...)");
                        }
                    }
                });
            }
            EditText cancelTextView = (EditText)findViewById(R.id.cancelText_et);
            if(cancelTextView != null){
                cancelTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        ip.setCancelText(s);
                    }
                });
                cancelTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            showCode("ImagePicker.setCancelText(...)");
                        }
                    }
                });
            }
            CheckBox canRemovingView = (CheckBox)findViewById(R.id.can_removing_ck);
            if (canRemovingView != null) {
                canRemovingView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ip.setCanRemoving(isChecked);
                        if(isChecked){
                            showCode("ImagePicker.setCanRemoving(...)");
                        }
                    }
                });
            }
            CheckBox showConfirmDialogView = (CheckBox)findViewById(R.id.show_confirm_dialog_ck);
            if (showConfirmDialogView != null) {
                showConfirmDialogView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ip.setShowConfirmDialog(isChecked);
                        if(isChecked){
                            showCode("ImagePicker.setShowConfirmDialog(...)");
                        }
                    }
                });
            }
            RadioGroup dialogLayoutView = (RadioGroup)findViewById(R.id.dialog_layout_rg);
            if (dialogLayoutView != null) {
                dialogLayoutView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        ip.setDialogResource(DIALOG_LAYOUT_COLLECTION.get(checkedId));
                        showCode("ImagePicker.setDialogResource(...)");
                    }
                });
            }
            final RadioGroup locationTypeView = (RadioGroup)findViewById(R.id.location_type_rg);
            if(locationTypeView != null){
                locationTypeView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        @ImagePicker.LocationType int type = LOCATION_TYPE_COLLECTION.get(checkedId);
                        ip.setLocationType(type);
                        showCode("ImagePicker.setLocationType(...)");
                    }
                });
            }
            SeekBar offsetSeekBar = (SeekBar)findViewById(R.id.offset_seekbar);
            if(offsetSeekBar != null){
                offsetSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        ip.setRemoveButtonOffset(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        showCode("ImagePicker.setRemoveButtonOffset(...)");
                    }
                });
            }
            // Add call back.
            ip.setOnImageAttachListener(this);
            ip.setOnImageRemoveListener(this);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_image_picker, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.action_tutorial:
//                Intent i = new Intent(ImagePickerDemo.this, TutorialActivity.class);
//                startActivity(i);
//                break;
//            case R.id.action_preview_code:
//                i = new Intent(ImagePickerDemo.this, PreviewCodeActivity.class);
//                startActivity(i);
//                break;
//            case R.id.action_open_code_tip:
//                item.setChecked(mShowCode = !item.isChecked());
//                break;
//        }
//        return false;
//    }

    private void showCode(String code){
        if(mShowCode){
            Toast.makeText(this, code, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onImageAttach(ImagePicker picker, Uri uri) {
        if(mShowCode){
            Toast.makeText(this, "chosen uri: " + uri, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onImageRemove(ImagePicker picker, Uri uri) {
        if(mShowCode){
            Toast.makeText(this, "delete uri: " + uri, Toast.LENGTH_SHORT).show();
        }
    }
}
