package com.timel.bus

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.utils.FileUtils
import com.google.common.collect.Sets
import javassist.ClassPool
import org.gradle.api.Project


public class JavassistTransform extends Transform{

    Project project

    public JavassistTransform(Project project){
        this.project = project
    }

    @Override
    String getName() {
        //设置Transform对应的Task名称
        return "MyJavassistTrans"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        /**
         * 处理的文件类型
         * <p>
         *     CLASSES:表示要处理编译后的字节码，可能是jar包也可能是目录
         *     RESOURCES:表示处理标准的Java资源
         * </p>
         */

        return Sets.immutableEnumSet(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        /**
         * Transform的作用域
         * <p>
         *     PROJECT:只处理当前的文件
         *     SUB_PROJECTS:只处理子项目
         *     EXTERNAL_LIBRARIES:只处理外部的依赖
         *     TESTED_CODE：测试代码
         *     PROVIDED_ONLY:只处理本地或远程以provided形式引入的依赖库
         *     PROJECT_LOCAL_DEPS：只处理当前项目的本地依赖，例如jar、aar
         *     SUB_PROJECTS_LOCAL_DEPS:只处理子项目的本地依赖
         * </p>
         */
        return Sets.immutableEnumSet(
                QualifiedContent.Scope.PROJECT,QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS,QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES
        )

    }

    @Override
    boolean isIncremental() {
        /**
         * 是否支持增量更新
         */
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        /**
         * 对文件或者jar进行处理，进行代码的插入
         * <p>
         *     TransformInput:对输入的class文件转变成目标字节码文件
         *     referencedInputs:是文件的抽象，包含DirectoryInput集合与JarInput集合
         *     DirectoryInput:源码方式参与项目编译的所有目录结构以及目录下的源文件
         *     JarInput:Jar包方式参与项目编译的所有本地jar或远程的jar包
         *     TransformOutProvider:通过这个类来获取输出路径
         *
         * </p>
         *
         */
        def startTime = System.currentTimeMillis()

        //Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each {
            TransformInput input ->


                //遍历jar包
                try{
                    input.jarInputs.each{

                        MyInject.injectJar(it.file.getAbsolutePath(),"com",project)
                        //重命名输出文件
                        String outputFileName = it.name.replace(".jar","")+"-"+it.file.path.hashCode()
                        def output = outputProvider.getContentLocation(outputFileName,it.contentTypes,it.scopes,
                                Format.JAR)
                        FileUtils.copyFile(it.file,output)
                    }
                }catch(Exception e){
                    project.logger.error(" jar文件报异常 ")
                    project.logger.error(e.getMessage())
                }

                //对类型为"文件夹"的input遍历
                input.directoryInputs.each {
                    DirectoryInput directoryInput ->
                        project.logger.error("文件遍历 path ="+directoryInput.file.absolutePath)
                        MyInject.injectDir(directoryInput.file.absolutePath,"com",project)

                        //获取output目录
                        def dest = outputProvider.getContentLocation(directoryInput.name,
                                    directoryInput.contentTypes,directoryInput.scopes,Format.DIRECTORY)

                        //将input的目录复制到output指定目录
                        FileUtils.copyFile(directoryInput.file,dest)
                }
                //关闭classPath,否则会一直存在饮用
                MyInject.removeClassPath(project)

        }


        ClassPool.getDefault().clearImportedPackages()
        project.logger.error("JavassistTransform cast :" +(System.currentTimeMillis() - startTime)/1000 + " secs")
    }
}