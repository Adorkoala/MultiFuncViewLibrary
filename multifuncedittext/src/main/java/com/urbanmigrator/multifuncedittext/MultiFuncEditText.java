package com.urbanmigrator.multifuncedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import com.urbanmigrator.multifuncedittext.listener.OnMultiFuncTextWatcher;
import com.urbanmigrator.multifuncedittext.listener.OnRightClickListener;

/**
 * Author: LiuJianjun
 * Date: 2017/6/27
 * Description:
 */

public class MultiFuncEditText extends AppCompatEditText {

    private static final String TAG = MultiFuncEditText.class.getSimpleName();

    private Drawable mRightDrawable;
    private Drawable mEyeOpenDrawable;
    /**普通类型*/
    private static final int TYPE_NORMAL = -1;
    /**自带清除文本功能的类型*/
    private static final int TYPE_CAN_CLEAR = 0;
    /**自带密码查看功能的类型*/
    private static final int TYPE_CAN_WATCH_PWD = 1;
    /*
     * 功能的类型
     * 默认为 -1，没有功能
     * 0，带有清除文本功能
     * 1，带有查看密码功能
     */
    private int funcType;
    private boolean eyeOpen = false;
    private int eyeCloseResourceId;
    private int eyeOpenResourceId;
    private int leftWidth;
    private int leftHeight;
    private int rightWidth;
    private int rightHeight;
    private TextWatcher mTextWatcher;

    private OnRightClickListener mRightClickListener;
    private OnMultiFuncTextWatcher mTextChangedListener;


    public MultiFuncEditText(Context context) {
        this(context, null);
    }

    public MultiFuncEditText(Context context, AttributeSet attrs){
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public MultiFuncEditText(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiFuncEditText,defStyle, 0);

        init(a);

        a.recycle();
    }

    private void init(TypedArray typedArray){
        funcType = typedArray.getInt(R.styleable.MultiFuncEditText_funcType,TYPE_NORMAL);

        eyeCloseResourceId = typedArray.getResourceId(R.styleable.MultiFuncEditText_eyeClose,R.mipmap.ic_eye_close);
        eyeOpenResourceId = typedArray.getResourceId(R.styleable.MultiFuncEditText_eyeOpen,R.mipmap.ic_eye_open);

        Drawable leftDrawable = getCompoundDrawables()[0];
        mRightDrawable = getCompoundDrawables()[2];

        if(mRightDrawable == null){
            if(funcType == TYPE_CAN_CLEAR){
                mRightDrawable = getResources().getDrawable(R.drawable.delete_selector);
            }else if(funcType == TYPE_CAN_WATCH_PWD){
                mRightDrawable = getResources().getDrawable(eyeCloseResourceId);
                mEyeOpenDrawable = getResources().getDrawable(eyeOpenResourceId);
            }
        }

        if(leftDrawable != null){
            leftWidth = typedArray.getDimensionPixelOffset(R.styleable.MultiFuncEditText_leftDrawableWidth,leftDrawable.getIntrinsicWidth());
            leftHeight = typedArray.getDimensionPixelSize(R.styleable.MultiFuncEditText_leftDrawableHeight,leftDrawable.getIntrinsicHeight());
            leftDrawable.setBounds(0,0,leftWidth, leftHeight);
        }

        if(mRightDrawable != null){
            rightWidth = typedArray.getDimensionPixelSize(R.styleable.MultiFuncEditText_rightDrawableWidth,mRightDrawable.getIntrinsicWidth());
            rightHeight = typedArray.getDimensionPixelOffset(R.styleable.MultiFuncEditText_rightDrawableHeight,mRightDrawable.getIntrinsicHeight());
            mRightDrawable.setBounds(0,0, rightWidth, rightHeight);
            if(mEyeOpenDrawable != null){
                mEyeOpenDrawable.setBounds(0,0, rightWidth,rightHeight);
            }
            if(funcType == TYPE_CAN_CLEAR){
                String content = getText().toString().trim();
                if(!TextUtils.isEmpty(content)){
                    setRightIconVisible(true);
                    setSelection(content.length());
                }else {
                    setRightIconVisible(false);
                }
            }else{
                setRightIconVisible(true);
            }
        }

        mTextWatcher = new TextWatcher() {
            /**
             * 当输入宽里面的内容发生变化时的回调方法
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(mTextChangedListener != null){
                    mTextChangedListener.beforeTextChanged(s,start,count, after);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(funcType == TYPE_CAN_CLEAR){
                    setRightIconVisible(s.length() > 0);
                }
                if(mTextChangedListener != null){
                    mTextChangedListener.onTextChanged(s,start,before, count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mTextChangedListener != null){
                    mTextChangedListener.afterTextChanged(s);
                }
            }
        };
        addTextChangedListener(mTextWatcher);

    }

    /**
     * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件
     * 当我们按下的位置在 EditText的宽度 - 图标到控件右侧的间距 - 图标的宽度 和
     * EditText的宽度 - 图标到控件右侧的间距 之间我们就算点击了图标，竖直方向没有考虑
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
            if(getCompoundDrawables()[2] != null){
                boolean isTouched = event.getX() > (getWidth() - getTotalPaddingRight())
                        && (event.getX() < (getWidth() - getPaddingLeft()));
                if(isTouched){
                    if(mRightClickListener == null){
                        if(funcType == TYPE_CAN_CLEAR){
                            this.setText("");
                        }else if(funcType == TYPE_CAN_WATCH_PWD){
                            if(eyeOpen){
                                //变为密文，TYPE_CLASS_TEXT 和 TYPE_TEXT_VARIATION_PASSWORD 必须一起使用
                                this.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                eyeOpen = false;
                            }else{
                                //变为明文
                                this.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                                eyeOpen = true;
                            }
                            switchWatchPwdIcon();
                        }
                    }else {
                        //如果没有则回调
                        mRightClickListener.onClick(this);
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置右侧图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     * @param visible 是否可见
     */
    protected void setRightIconVisible(boolean visible){
        Drawable right = visible ? mRightDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1],right,getCompoundDrawables()[3]);
    }

    /**
     * 切换查看密码的图标
     */
    private void switchWatchPwdIcon(){
        if(eyeOpen){
            setCompoundDrawables(getCompoundDrawables()[0],
                    getCompoundDrawables()[1],mEyeOpenDrawable,getCompoundDrawables()[3]);
        }else {
            setCompoundDrawables(getCompoundDrawables()[0],
                    getCompoundDrawables()[1],mRightDrawable,getCompoundDrawables()[3]);
        }
    }

    public String getTextString(){
        if(mTextChangedListener != null){
            return mTextChangedListener.getTextString(getText());
        }else {
            return getText().toString();
        }
    }

    public void removeTextWatcher(){
        removeTextChangedListener(mTextWatcher);
    }

    public void addTextWatcher(){
        addTextChangedListener(mTextWatcher);
    }

    public void setOnRightClickListener(OnRightClickListener onRightClickListener){
        this.mRightClickListener = onRightClickListener;
    }

    public void addTextChangedListener(OnMultiFuncTextWatcher onMultiFuncTextWatcher){
        this.mTextChangedListener = onMultiFuncTextWatcher;
    }

    /**
     * 设置晃动动画
     */
    public void setShakeAnimation() {
        this.setAnimation(shakeAnimation(5));
    }

    /**
     * 晃动动画
     *
     * @param counts 1秒钟晃动多少下
     * @return
     */
    public static Animation shakeAnimation(int counts) {
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(counts));
        translateAnimation.setDuration(1000);
        return translateAnimation;
    }

}
