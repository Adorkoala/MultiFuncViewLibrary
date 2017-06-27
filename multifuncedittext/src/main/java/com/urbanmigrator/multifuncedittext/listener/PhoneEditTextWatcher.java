package com.urbanmigrator.multifuncedittext.listener;

import android.text.Editable;

import com.urbanmigrator.multifuncedittext.MultiFuncEditText;


/**
 * Created by LiuJianjun on 2017/6/17.
 */

public class PhoneEditTextWatcher implements OnMultiFuncTextWatcher {

    private MultiFuncEditText mEditText;
    private String mDivider =" ";
    private int lastLength = 0;
    private int dividerLength = 0;

    public PhoneEditTextWatcher(MultiFuncEditText editText, String divider){
        this.mEditText = editText;
        this.mDivider = divider;
        dividerLength = mDivider.length();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.length() < lastLength){
            lastLength = s.length();
            return;
        }
        //防止进入死循环
        mEditText.removeTextWatcher();
        String text = mEditText.getText().toString();
        if(text.contains(mDivider)){
            //删除已添加的分隔符
            String[] ses = text.split(mDivider);
            for(int i = 0; i < ses.length - 1; i++){
                int length = 0;
                for(int j = 0; j <= i; j ++){
                    length = length + ses[j].length();
                }
                s.delete(length,length + dividerLength);
            }

        }
        if(s.length() > 3){
            s.insert(3,mDivider);
        }
        if(s.length() > (7 + dividerLength)){
            s.insert((7+ dividerLength),mDivider);
        }
        if(s.length() > (11 + dividerLength * 2)){
            s.delete((11 + dividerLength * 2),mEditText.getText().length());
        }
        lastLength = s.length();
        mEditText.addTextWatcher();
    }

    @Override
    public String getTextString(Editable editable) {
        return editable.toString().replaceAll(mDivider,"");
    }
}
