package com.timel.bus;

import android.os.Message;

/**
 * 标题：
 * 描述：
 * 作者：黄好杨
 * 创建时间：2019-12-08 13:09
 **/
public interface Event {

    void call(Message msg);
}
