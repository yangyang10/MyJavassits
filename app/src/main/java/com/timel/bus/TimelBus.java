package com.timel.bus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.timel.bus.annotation.Bus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 标题：
 * 描述：
 * 作者：黄好杨
 * 创建时间：2019-12-08 13:11
 **/
public class TimelBus {

    //存储所有事件ID以及回调
    private ConcurrentHashMap<Integer, List<SparseArray<Event>>> mEventList = new ConcurrentHashMap<>();
    //存储粘连事件ID以及其数据
    private ConcurrentHashMap<Integer, Object> mStickyEventList = new ConcurrentHashMap<>();

    private ScheduledExecutorService mPool = Executors.newScheduledThreadPool(5);
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private TimelBus() {
    }


    private static class Holder {
        public static TimelBus eb = new TimelBus();
    }

    public static TimelBus getInstance() {
        return Holder.eb;
    }

    public TimelBus register(int tag, Event ev) {
        return register(tag, ev, Bus.DEFAULT);
    }

    public TimelBus register(int tag, final Event ev, int thread) {
        SparseArray<Event> mEvent = new SparseArray<Event>();
        mEvent.put(thread, ev);
        if (mEventList.get(tag) != null) {
            mEventList.get(tag).add(mEvent);
        } else {
            List<SparseArray<Event>> list = new ArrayList<>();
            list.add(mEvent);
            mEventList.put(tag, list);
        }
        Log.e("Bus register", tag + " : " + mEventList.get(tag).size());
        if (mStickyEventList.get(tag) != null) {
            final Message msg = new Message();
            msg.obj = mStickyEventList.get(tag);
            msg.what = tag;
            callEvent(msg, ev, thread);
            Log.e("mStickyEvent register", tag + " :" + mEventList.get(tag).size());
        }
        return this;
    }


    private void callEvent(Message msg, Event ev, int thread) {
        switch (thread) {
            case Bus.DEFAULT:
                ev.call(msg);
                break;
            case Bus.UI:
                mHandler.post(()->ev.call(msg));
                break;
            case Bus.BG:
                mPool.execute(()->ev.call(msg));
                break;
        }
    }

    public TimelBus unRegister(int tag){
        if(mEventList.get(tag) != null){
            mEventList.remove(tag);
        }
        return this;
    }

    public TimelBus onEvent(int tag,Object data){
        Message msg = new Message();
        msg.obj = data;
        msg.what = tag;
        if(mEventList.get(tag) != null){
            Log.e("Bus onEvent",tag+" :"+mEventList.get(tag).size());
            for(SparseArray<Event> ev :mEventList.get(tag)){
                callEvent(msg,ev.valueAt(0),ev.keyAt(0));
            }
        }
        return this;
    }

    public TimelBus onEvent(int tag){
        return onEvent(tag,null);
    }

    public TimelBus onStickyEvent(int tag,Object data){
        Log.e("Bus onStickyEvent",tag+ "");
        mStickyEventList.put(tag,(data == null ? tag : data));
        onEvent(tag,data);
        return this;
    }

    public TimelBus onStickyEvent(int tag){
        onStickyEvent(tag,null);
        return this;
    }

}
