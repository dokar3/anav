package com.dokar.anav.compiler;

import com.dokar.anav.annotation.Navigable;
import com.dokar.anav.compiler.NavGenerator.CodeType;
import com.dokar.anav.compiler.code.SourceFile;
import com.google.auto.service.AutoService;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
public final class AnavProcessor extends AbstractProcessor {

    private static final String TAG = "AnavProcessor";

    private static final String BUILD_GEN_PATH = "generated/anav/out/";

    private Messager messager;
    private Filer filer;
    private Elements elementUtils;

    private ProcessorOptions options;

    private File baseBuildDir;
    private File baseSourceDir;

    private boolean isKapt;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        messager = env.getMessager();
        filer = env.getFiler();
        elementUtils = env.getElementUtils();

        final Map<String, String> optionMap = env.getOptions();

        this.options = ProcessorOptions.from(optionMap);

        for (Map.Entry<String, String> option : optionMap.entrySet()) {
            logd("option: [" + option.getKey() + ", " + option.getValue() + "]");
        }

        isKapt = optionMap.containsKey("kapt.kotlin.generated");

        if (options.buildDir != null) {
            baseBuildDir = new File(options.buildDir, BUILD_GEN_PATH);
        }

        if (options.sourceDir != null) {
            baseSourceDir = new File(options.sourceDir);
        }

        logd("applicationId: " + options.sourcePackageName
                + ", buildDir: " + options.buildDir);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Navigable.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return ProcessorOptions.optionKeys();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        long start = System.currentTimeMillis();

        Set<? extends Element> navigableElements =
                env.getElementsAnnotatedWith(Navigable.class);

        if (navigableElements.isEmpty()) {
            return false;
        }

        try {
            parseNavigableAnnotations(navigableElements);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        logd("done, cost: " + (end - start) + " ms");

        return true;
    }

    private void parseNavigableAnnotations(Set<? extends Element> navigableElements) {
        final CodeType codeType;
        if (isKapt) {
            codeType = CodeType.Kotlin;
        } else {
            codeType = CodeType.Java;
        }

        final NavGenerator navGenerator = createNavMap(codeType);

        for (Element element : navigableElements) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }

            // get name from annotated class
            final String classSimpleName = element.getSimpleName().toString();
            String destName;
            if (options.removeActivitySuffix) {
                destName = Utils.removeActivitySuffix(classSimpleName);
            } else {
                destName = classSimpleName;
            }

            // Skip none-Activity classes
            if (!isActivityType(element.asType())) {
                String annotation = Navigable.class.getSimpleName();
                logw("Skip, @" + annotation + " on a none-Activity class: " +
                        classSimpleName);
                continue;
            }
            // get Activity package name
            final String pkgName = elementUtils.getPackageOf(element).toString();

            // clean cached destinations
            try {
                navGenerator.cleanDestinations(pkgName, classSimpleName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // get annotation value
            Navigable annotation = element.getAnnotation(Navigable.class);

            final String groupName = annotation.group();
            // add a destination
            navGenerator.addDestination(destName, pkgName, classSimpleName, groupName);

            final String[] args = annotation.args();

            final String[] argTypes =
                    getClassesFromAnnotation(element, "argTypes");

            // clean cached arguments
            navGenerator.cleanArgs(pkgName, classSimpleName);

            final String argGroupName = destName;
            for (int i = 0; i < args.length; i++) {
                String argName = args[i];
                String argType = null;
                if (i < argTypes.length) {
                    argType = argTypes[i];
                }
                // add a argument
                navGenerator.addArgument(pkgName,
                        classSimpleName,
                        argName,
                        argGroupName,
                        argType);
            }
        }

        logd("Generating source files...");

        generateNavMapSource(navGenerator);
        generateNavArgsSource(navGenerator);

        if (baseBuildDir != null) {
            logd("Generating json files...");
            generateNavMapJson(navGenerator);
            generateNavArgsJson(navGenerator);
        }
    }

    private NavGenerator createNavMap(CodeType codeType) {
        NavGenerator navGenerator = null;

        if (baseBuildDir != null) {
            File mapJsonFile = NavGenerator.getMapJsonFile(baseBuildDir);
            File argsJsonFile = NavGenerator.getArgsJsonFile(baseBuildDir);

            navGenerator = NavGenerator.fromFile(codeType, mapJsonFile, argsJsonFile,
                    options.sourcePackageName, options.navMapClassName,
                    options.navArgsClassName);
        }

        if (navGenerator == null) {
            navGenerator = NavGenerator.create(codeType, options.sourcePackageName,
                    options.navMapClassName, options.navArgsClassName);
        }

        return navGenerator;
    }

    private void generateNavMapSource(NavGenerator navGenerator) {
        SourceFile mapSourceFile = navGenerator.genMapSourceFile();
        try {
            if (baseSourceDir != null) {
                mapSourceFile.writeTo(baseSourceDir);
            } else {
                mapSourceFile.writeTo(filer);
            }
        } catch (IOException e) {
            String code = mapSourceFile.toString();
            loge("Cannot generate java file, error: " + e + ", " +
                    "code: \n" + code);
        }
    }

    private void generateNavMapJson(NavGenerator navGenerator) {
        String json = navGenerator.getNavMapJson();
        if (json == null) {
            logd("NavMap json is null, skip");
            return;
        }

        File jsonFile = NavGenerator.getMapJsonFile(baseBuildDir);
        if (!Utils.writeText(jsonFile, json)) {
            loge("Cannot write json to file: " + jsonFile.getAbsolutePath());
        }
    }

    private void generateNavArgsSource(NavGenerator navGenerator) {
        // generate args
        SourceFile argsSourceFile = navGenerator.genArgsSourceFile();
        try {
            if (baseSourceDir != null) {
                argsSourceFile.writeTo(baseSourceDir);
            } else {
                argsSourceFile.writeTo(filer);
            }
        } catch (IOException e) {
            loge("Cannot generate java file: error: " + e + ", " +
                    "code: \b" + argsSourceFile);
        }
    }

    private void generateNavArgsJson(NavGenerator navGenerator) {
        String json = navGenerator.getNavArgsJson();
        if (json == null) {
            logd("NavArgs json is null, skip");
            return;
        }

        File jsonFile = NavGenerator.getArgsJsonFile(baseBuildDir);
        if (!Utils.writeText(jsonFile, json)) {
            loge("Cannot write json to file: " + jsonFile.getAbsolutePath());
        }
    }

    private boolean isActivityType(TypeMirror typeMirror) {
        return isSubtypeOf(typeMirror, Utils.TYPE_ACTIVITY);
    }

    private boolean isSubtypeOf(TypeMirror typeMirror, String superType) {
        if (typeMirror.toString().equals(superType)) {
            return true;
        }

        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }

        DeclaredType declaredType = (DeclaredType) typeMirror;

        Element element = declaredType.asElement();

        if (!(element instanceof TypeElement)) {
            return false;
        }

        TypeElement typeElement = (TypeElement) element;

        if (isSubtypeOf(typeElement.getSuperclass(), superType)) {
            return true;
        }

        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOf(interfaceType, superType)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private String[] getClassesFromAnnotation(Element element, String memberName) {
        String classesString = "";
        List<? extends AnnotationMirror> annotationMirrors
                = element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                    annotationMirror.getElementValues();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                    elementValues.entrySet()) {
                String keyName = entry.getKey().getSimpleName().toString();
                if (memberName.equals(keyName)) {
                    String value = entry.getValue().getValue().toString();
                    if (!value.contains(".class")) {
                        // not a Class or Class[] member
                        return new String[0];
                    }
                    classesString = value;
                    break;
                }
            }
        }

        return classesString.split(",");
    }

    private void loge(CharSequence text) {
        String msg = getLogMessage(text);
        messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void logd(CharSequence text) {
        if (!options.debug) return;
        String msg = getLogMessage(text);
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }


    private void logw(CharSequence text) {
        String msg = getLogMessage(text);
        messager.printMessage(Diagnostic.Kind.WARNING, msg);
    }

    private String getLogMessage(CharSequence text) {
        return (TAG + ": " + text + '\n')
                .replace("\n", System.lineSeparator());
    }
}
