package com.dokar.anav.compiler.code.kotlin;

import com.squareup.kotlinpoet.AnnotationSpec;
import com.squareup.kotlinpoet.ClassName;
import com.squareup.kotlinpoet.TypeName;
import com.squareup.kotlinpoet.TypeNames;
import com.squareup.kotlinpoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

public class KotlinPoetHelper {

    public static final String SUPPRESS_REDUNDANT_MODIFIER
            = "RedundantVisibilityModifier";

    public static TypeSpec.Builder addSuppressAnnotation(
            TypeSpec.Builder builder,
            String suppress) {
        ClassName className = new ClassName(
                "kotlin", "Suppress");
        AnnotationSpec annotationSpec = AnnotationSpec.builder(className)
                .addMember("%S", suppress)
                .build();
        return builder.addAnnotation(annotationSpec);
    }

    public static Map<String, TypeName> JAVA_TO_KT_TYPE_MAP =
            new HashMap<String, TypeName>() {
                {
                    put("byte", TypeNames.BYTE);
                    put("byte[]", TypeNames.BYTE_ARRAY);

                    put("short", TypeNames.SHORT);
                    put("short[]", TypeNames.SHORT_ARRAY);

                    put("char", TypeNames.CHAR);
                    put("char[]", TypeNames.CHAR_ARRAY);

                    put("boolean", TypeNames.BOOLEAN);
                    put("boolean[]", TypeNames.BOOLEAN_ARRAY);

                    put("int", TypeNames.INT);
                    put("int[]", TypeNames.INT_ARRAY);

                    put("long", TypeNames.LONG);
                    put("long[]", TypeNames.LONG_ARRAY);

                    put("float", TypeNames.FLOAT);
                    put("float[]", TypeNames.FLOAT_ARRAY);

                    put("double", TypeNames.DOUBLE);
                    put("double[]", TypeNames.DOUBLE_ARRAY);

                    put("java.lang.String", TypeNames.STRING);

                    put("java.lang.CharSequence", TypeNames.CHAR_SEQUENCE);
                }
            };
}
