package com.timel.bus

import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

import java.lang.annotation.Annotation


/**
 * 事件信息
 */
public class BusInfo{

    Project project
    CtClass clazz
    List<CtMethod> methods = new ArrayList<>()
    List<Annotation> annotations = new ArrayList<>()
    List<Integer> eventIds = new ArrayList<>()
    boolean isActivity = false //是否是在Activity
    CtMethod onCreateMethod  //Activity或Fragment的初始化方法
    CtMethod onDestroyMethod //Activity或Fragment的销毁方法
    CtMethod busRegisterMethod //被Register注解的初始化方法
    CtMethod busUnRegisterMethod //被UnRegister注解的销毁方法

}