package com.timel.bus

import javassist.ClassPool
import javassist.CtMethod


public class Utils{

    static String BusErr = "大哥注意哦，非Activity和Fragment中使用@BusRegister必须和@BusUnRegister一起使用，才能自动生成注册和反注册代码"

    /**
     * 转载相关的类
     * @param pool
     */
    public static void importBaseClass(ClassPool pool){

        pool.importPackage(LogTimeHelper.LogTimeAnnotation)
        pool.importPackage(BusHelper.TimelBusAnnotation)
        pool.importPackage(BusHelper.TimelBusRegisterAnnotation)
        pool.importPackage(BusHelper.TimelBusUnRegisterAnnotation)
        pool.importPackage("android.os.Bundle")
        pool.importPackage("com.timel.bus.TimelBus")
        pool.importPackage("com.timel.bus.Event")
        pool.importPackage("android.os.Message")
    }



    public static String getSimpleName(CtMethod ctMethod){
        def methodName = ctMethod.getName()
        return methodName.substring(methodName.lastIndexOf('.')+1, methodName.length())
    }


    public static String getClassName(int index,String filePath){
        int end = filePath.length() -6 //.class = 6
        return filePath.substring(index,end).replace('\\','.').replace('/', '.')
    }
}