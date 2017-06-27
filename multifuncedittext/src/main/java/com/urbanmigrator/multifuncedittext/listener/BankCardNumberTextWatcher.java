package com.urbanmigrator.multifuncedittext.listener;

import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;

import com.urbanmigrator.multifuncedittext.MultiFuncEditText;


/**
 * Created by LiuJianjun on 2017/6/17.
 */

public class BankCardNumberTextWatcher implements OnMultiFuncTextWatcher{

    private MultiFuncEditText mEditText;

    private char mDivider;

    int beforeTextLength = 0;
    int onTextLength = 0;
    boolean isChanged = false;

    int location = 0;
    private char[] tempChar;
    private StringBuffer buffer = new StringBuffer();
    int dividerNumber = 0;

    public BankCardNumberTextWatcher(MultiFuncEditText editText, char divider){
        this.mEditText = editText;
        this.mDivider = divider;
    }

    @Override
    public String getTextString(Editable editable) {
        String text = editable.toString();
        if(TextUtils.isEmpty(text)){
            return "";
        }else {
            return text.replace(String.valueOf(mDivider),"");
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        beforeTextLength = s.length();
        if(buffer.length() > 0){
            buffer.delete(0,buffer.length());
        }
        dividerNumber = 0;
        for(int i = 0; i < s.length(); i ++){
            if(s.charAt(i) == mDivider){
                dividerNumber++;
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        onTextLength = s.length();
        buffer.append(s.toString());
        if(onTextLength == beforeTextLength || onTextLength <= 3 || isChanged){
            isChanged = false;
            return;
        }
        isChanged = true;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(isChanged){
            location = mEditText.getSelectionEnd();
            int index = 0;
            while (index < buffer.length()){
                if(buffer.charAt(index) == mDivider){
                    buffer.deleteCharAt(index);
                }else {
                    index++;
                }
            }
            index = 0;
            int spaceNumberCount = 0;
            while (index < buffer.length()){
                if((index == 4 || index == 9 || index == 14 || index == 19 || index == 24 || index == 29)){
                    buffer.insert(index, mDivider);
                    spaceNumberCount++;
                }
                index++;
            }

            if(spaceNumberCount > dividerNumber){
                location += (spaceNumberCount - dividerNumber);
            }

            tempChar = new char[buffer.length()];
            buffer.getChars(0, buffer.length(), tempChar, 0);
            String str = buffer.toString();
            if(location > str.length()){
                location = str.length();
            }else if(location < 0){
                location = 0;
            }
            mEditText.setText(str);
            Editable editable = mEditText.getText();
            Selection.setSelection(editable,location);
            isChanged = false;
        }
    }
}
