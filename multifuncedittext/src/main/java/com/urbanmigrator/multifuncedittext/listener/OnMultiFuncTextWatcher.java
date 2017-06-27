package com.urbanmigrator.multifuncedittext.listener;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by LiuJianjun on 2017/6/17.
 */

public interface OnMultiFuncTextWatcher extends TextWatcher {

    String getTextString(Editable editable);
}
