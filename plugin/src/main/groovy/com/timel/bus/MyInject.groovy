package com.timel.bus

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.JarClassPath
import javassist.bytecode.DuplicateMemberException
import org.gradle.api.Project

import java.lang.annotation.Annotation


public class MyInject{

    static def classPathList = new ArrayList<JarClassPath>()

    static void removeClassPath(Project project){
        if(classPathList != null){
            def pool = ClassPool.getDefault()
            classPathList.each {
                try{
                    pool.removeClassPath(it)
                }catch(Exception e){
                    project.logger.error(e.getMessage())
                }
            }
        }
    }

    /**
     * jar中的文件加入到ClassPool中
     * @param path
     * @param packageName
     * @param project
     */
    public static void injectJar(String path, String packageName, Project project){

        ClassPool pool = ClassPool.getDefault()
        def classPath = new JarClassPath(path)
        classPathList.add(classPath)
        pool.appendClassPath(classPath)
        // project.android.bootClasspath 加入android.jar 否则找不到Android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString())
        Utils.importBaseClass(pool)

    }


    /**
     * 文件中的class添加到ClassPool
     * @param path
     * @param packageName
     * @param project
     */
    public static void injectDir(String path,String packageName,Project project){

        ClassPool pool = ClassPool.getDefault()
        pool.appendClassPath(path)
        // project.android.bootClasspath 加入android.jar 否则找不到Android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString())
        Utils.importBaseClass(pool)
        File dir = new File(path)
        if(!dir.isDirectory()){
            return
        }

        dir.eachFileRecurse {
            File file ->
                String filePath = file.absolutePath
                //确保当前文件是class文件，并且不是系统自动生成的class文件
                if(filePath.endsWith(".class") && !filePath.contains('R$') && !filePath.contains('$')
                    && !filePath.contains('R.class') && !filePath.contains("BuildConfig.class")
                ){

                    //判断当前目录是否在我们的应用包里面
                    int index = filePath.indexOf(packageName)
                    boolean isMyPackage = index != -1
                    if(isMyPackage){
                        String className = Utils.getClassName(index,filePath)
                        CtClass c = pool.getCtClass(className)
                        if(c.isFrozen()) c.defrost()
                        BusInfo busInfo = new BusInfo()
                        busInfo.setProject(project)
                        busInfo.setClazz(c)

                        if(c.getName().endsWith("Activity") || c.getSuperclass().getName().endsWith("Activity")){
                            busInfo.setIsActivity(true)
                        }
                        boolean isAnnotationByBus = false
                        //getDeclaredMethods获取自己申明的方法，c.getMethods()会把所有父类的方法都加上
                        for(CtMethod ctmethod : c.getDeclaredMethods()){
                            String methodName = Utils.getSimpleName(ctmethod)
                            if(BusHelper.ON_CREATE.contains(methodName)) busInfo.setOnCreateMethod(ctmethod)
                            if(BusHelper.ON_DESTROY.contains(methodName)) busInfo.setOnDestroyMethod(ctmethod)
                            for(Annotation annotation : ctmethod.getAnnotations()){
                                if(annotation.annotationType().canonicalName.equals(BusHelper.TimelBusRegisterAnnotation))
                                    busInfo.setBusRegisterMethod(ctmethod)
                                if(annotation.annotationType().canonicalName.equals(BusHelper.TimelBusUnRegisterAnnotation))
                                    busInfo.setBusUnRegisterMethod(ctmethod)
                                if(annotation.annotationType().canonicalName.equals(BusHelper.TimelBusAnnotation)){
                                    project.logger.error " method:" +c.getName() +"_" + ctmethod.getName()
                                    busInfo.methods.add(ctmethod)
                                    busInfo.annotations.add(annotation)
                                    if(!isAnnotationByBus) isAnnotationByBus = true
                                }
                            }
                        }

                        if(((busInfo.busRegisterMethod != null && busInfo.busUnRegisterMethod == null
                            || busInfo.busRegisterMethod == null && busInfo.busUnRegisterMethod != null)))
                            assert false : Utils.getBusErr()
                        if(busInfo != null && isAnnotationByBus){
                            try{
                                BusHelper.initBus(busInfo,path)
                            }catch(DuplicateMemberException e){

                            }
                        }
                        c.detach()//清除pool
                    }
                }
        }
    }

}