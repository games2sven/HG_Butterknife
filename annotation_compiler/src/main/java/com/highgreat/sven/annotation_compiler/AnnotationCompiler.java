package com.highgreat.sven.annotation_compiler;

import com.google.auto.service.AutoService;
import com.highgreat.sven.annotations.BindView;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

/**
 * 自定义注解处理器，在java代码编译成class文件的过程中生效。扫描文件中的注解相关内容
 */
@AutoService(Processor.class)
public class AnnotationCompiler extends AbstractProcessor {

    //定义一个用于生成文件的对象(编译过程中动态生成.java文件（机器生成）)
    Filer filer;
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    /**
     * 确定当前处理所有模块中哪些注解
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    /**
     * 支持的sdk版本
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 扫描的结果返回，可以在这个方法里面实现操作逻辑
     * @param annotations
     * @param roundEnv
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //得到程序中所有写了BindView注解的元素的集合
        //类元素（TypeElement)
        //可执行元素(ExecutableElement)
        //属性元素（VariableElement）
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        //定义一个Map用来分类(每一个activity对应一个字段注解集合)
        Map<String, List<VariableElement>> map = new HashMap<>();

        //开始分类存入map中
        for (Element element : elements) {
            VariableElement variableElement=(VariableElement)element;
            //获取activity的名字
            String activityName = variableElement.getEnclosingElement().getSimpleName().toString();
            List<VariableElement> elementList = map.get(activityName);
            if(elementList == null){
                elementList = new ArrayList<>();
                map.put(activityName,elementList);
            }
            elementList.add(variableElement);
        }
        //运行到这就完成了分类工作
        if(map.size()>0){
            //开始写入文件
            Writer writer = null;
            //每一个activity都要生成一个对应的java文件
            Iterator<String> iterator = map.keySet().iterator();
            while(iterator.hasNext()){
                String activityName = iterator.next();
                List<VariableElement> elementList = map.get(activityName);
                //获取包名
                TypeElement enclosingElement = (TypeElement) elementList.get(0).getEnclosingElement();
                PackageElement packageName = processingEnv.getElementUtils().getPackageOf(enclosingElement);

                //开始生成文件
                try{
                    //包名.MainActivity_ViewBinding
                    JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + activityName + "_ViewBinding");
                    writer = sourceFile.openWriter();
                    //        package com.example.hg_butterknife;
                    writer.write("package "+packageName+";\n");
                    //import com.highgreat.sven.hg_butterknife.IBinder;
                    writer.write("import "+packageName+".IBinder;\n");
                    //        public class MainActivity_ViewBinding implements IBinder<com.highgreat.sven.hg_butterknife.MainActivity>{
                    writer.write("public class "+activityName+"_ViewBinding implements IBinder<"
                            +packageName+"."+activityName+">{\n");
                    //            @Override
                    writer.write("@Override\n");
                    //            public void bind(com.highgreat.sven.hg_butterknife.MainActivity target) {
                    writer.write("public void bind("+packageName+"."+activityName+" target){\n");
                    // target.tvText=(android.widget.TextView)target.findViewById(2131165325);
                    for (VariableElement variableElement : elementList) {
                        //获取控件的名字
                        String variableName = variableElement.getSimpleName().toString();
                        //获取ID
                        int id = variableElement.getAnnotation(BindView.class).value();
                        //获取控件的类型
                        TypeMirror typeMirror = variableElement.asType();
                        writer.write("target."+variableName+"=("+typeMirror+")target.findViewById("+id+");\n");
                    }
                    writer.write("\n}}");
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(writer != null){
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return false;
    }
}
