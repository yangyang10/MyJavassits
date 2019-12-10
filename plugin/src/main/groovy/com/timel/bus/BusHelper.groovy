package com.timel.bus

import javassist.CtClass
import javassist.CtMethod
import javassist.CtNewMethod
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.MethodInfo
import javassist.bytecode.annotation.IntegerMemberValue

import java.lang.annotation.Annotation

public class BusHelper{

    final static String TimelBusAnnotation = "com.timel.bus.annotation.Bus"
    final static String TimelBusRegisterAnnotation = "com.timel.bus.annotation.BusRegister"
    final static String TimelBusUnRegisterAnnotation = "com.timel.bus.annotation.BusUnRegister"

    static def ON_CREATE = ['onCreate','onActivityCreated'] as String[]
    static def ON_DESTROY = 'onDestroy'

    static def Activity_OnCreate = "\n" +
            "   protected void onCreate(Bundle saveInstanceState) {\n" +
            "       super.onCreate(saveInstanceState);\n"

    static def Fragment_OnCreate = " public void onActivityCreated(Bundle saveInstanceState) {\n" +
                "   super.onActivityCreated(saveInstanceState);"

    static def Pre_Switch_Str = " public void call(Message msg) {\n"+
            "switch (msg.what){\n"

    static def Pre_OnDestroy = "    \n" +
            "      protected void onDestroy() {\n" +
            "           super.onDestroy();\n"


    /**
     *
     * @param busInfo
     * @param path
     */
    public static void initBus(BusInfo busInfo,String path){

        busInfo.project.logger.error "is comming"

        if(busInfo.clazz.isFrozen()) busInfo.clazz.defrost() //解冻
        if(busInfo.busRegisterMethod != null){//有被busRegister注解的方法
            busInfo.project.logger.error "busRegisterMethod not null"
            busInfo.busRegisterMethod.insertAfter(getRegisterEventMethodStr(busInfo))
        }else if(busInfo.getOnCreateMethod() == null){//没有OnCreateMethod,创建并添加新代码
            busInfo.project.logger.error "getOnCreateMethod null " + busInfo.isActivity
            String pre_create_ster = busInfo.isActivity ? Activity_OnCreate : Fragment_OnCreate
            String m = pre_create_ster + getRegisterEventMethodStr(busInfo)+"}"
            busInfo.project.logger.error m
            CtMethod initEventMethod = CtNewMethod.make(m,busInfo.clazz)
            busInfo.clazz.addMethod(initEventMethod)

        }else{
            //有OnCreateMethod 直接插入新代码
            busInfo.project.logger.error "OnCreateMethod not null"
            busInfo.onCreateMethod.insertAfter(getRegisterEventMethodStr(busInfo))
        }

        if(busInfo.busUnRegisterMethod != null){//有被busUnRegister注解方法
            busInfo.project.logger.error "busUnRegisterMethod not null"
            busInfo.busUnRegisterMethod.insertAfter(getUnRegisterEventMethodStr(busInfo))
        }else if(busInfo.onDestroyMethod == null){
            busInfo.project.logger.error "OnDestroyMethod null"
            String m = Pre_OnDestroy + getUnRegisterEventMethodStr(busInfo) +"}"
            busInfo.project.logger.error m
            CtMethod destroyMethod = CtNewMethod.make(m,busInfo.clazz)
            busInfo.clazz.addMethod(destroyMethod)
        }else{
            busInfo.project.logger.error "OnDestroyMethod not null"
            busInfo.onDestroyMethod.insertAfter(getUnRegisterEventMethodStr(busInfo))
        }
        busInfo.clazz.writeFile(path)
    }



    static String getRegisterEventMethodStr(BusInfo busInfo){
        String createStr = ""
        //为当前的类添加时间处理接口
        busInfo.clazz.addInterface(busInfo.clazz.classPool.get("com.timel.bus.Event"))
        for(int i =0;i<busInfo.getMethods().size();i++){
            MethodInfo methodInfo = busInfo.getMethods().get(i).getMethodInfo()
            Annotation mAnnotation = busInfo.getAnnotations().get(i)
            AnnotationsAttribute attribute = methodInfo.getAttribute(AnnotationsAttribute.visibleTag)
            //获取注解属性
            javassist.bytecode.annotation.Annotation annotation = attribute.getAnnotation(mAnnotation.annotationType().canonicalName)
            //获取注解
            int id = ((IntegerMemberValue)annotation.getMemberValue("value")).getValue()
            int thread = -1
            if(annotation.getMemberValue("thread") != null)
                thread = ((IntegerMemberValue)annotation.getMemberValue("thread")).getValue()
            busInfo.eventIds.add(thread)
            createStr += "TimelBus.getInstance().register("+id + ",(Event)this,"+thread+");\n"
        }
        initEventDispatch(busInfo)
        return createStr
    }


    /**
     * 生成event事件分发的逻辑代码
     * @param busInfo
     */
    static void initEventDispatch(BusInfo busInfo){
        String switchStr = Pre_Switch_Str
        for(int i = 0;i<busInfo.eventIds.size();i++){
            CtMethod method = busInfo.getMethods().get(i)
            CtClass[] parameterTypes = method.getParameterTypes()
            assert parameterTypes.length <= 1
            boolean one = parameterTypes.length == 1
            boolean isBaseType = false
            String packageName = ""
            if(one){
                String parameterType = parameterTypes[0].name
                switch(parameterType){

                    //Primitive Type (原始型) Reference Types(Wrapper Class) (应用型，包装)
                    case "boolean" : parameterType = "Boolean"; isBaseType = true; break
                    case "byte" : parameterType = "Byte"; isBaseType = true;break
                    case "char" : parameterType = "Character"; isBaseType = true;break
                    case "float": parameterType = "Float";isBaseType = true;break
                    case "int" : parameterType = "Integer";isBaseType = true;break
                    case "long" : parameterType = "Long";isBaseType = true;break
                    case "short" : parameterType = "Short";isBaseType = true;break
                    case "double" : parameterType = "Double";isBaseType = true;break

                }
                busInfo.project.logger.error "name:"+parameterType
                packageName = isBaseType ? "java.lang."+parameterType :parameterType
                busInfo.clazz.classPool.importPackage(packageName)
            }
            //如果是基本数据类型，需要手动拆箱，否则会报错
            String paramStr = isBaseType ? ("((" + packageName +")msg.obj)."+
                    parameterTypes[0].name + "Value()") :("("+packageName +")msg.obj")

            switchStr += "case " + busInfo.eventIds.get(i) + ":" + method.getName()+
                    "(" + (one ? paramStr : "") +");\n break; \n"

        }
        String m = switchStr + "}\n}"
        busInfo.project.logger.error m
        CtMethod dispatchEventMethod = CtMethod.make(m,busInfo.clazz)
        busInfo.clazz.addMethod(dispatchEventMethod)
    }


    /**
     * 生成取消事件注册的代码
     * @param busInfo
     * @return
     */
    static String getUnRegisterEventMethodStr(BusInfo busInfo){
        String dis_str = ""
        busInfo.eventIds.each {
            id -> dis_str += "TimelBus.getInstance().unRegister("+id+");\n"
        }
        return dis_str
    }

}