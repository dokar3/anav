package com.dokar.anav.compiler.code;

import java.io.File;
import java.io.IOException;

import javax.annotation.processing.Filer;

public interface SourceFile {

    void writeTo(File file) throws IOException;

    void writeTo(Filer filer) throws IOException;
}
