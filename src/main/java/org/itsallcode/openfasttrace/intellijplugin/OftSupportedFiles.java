package org.itsallcode.openfasttrace.intellijplugin;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.Locale;
import java.util.Set;

public final class OftSupportedFiles {
    private static final Set<String> SPECIFICATION_EXTENSIONS = Set.of("md", "markdown", "rst");
    private static final Set<String> TAG_IMPORTER_EXTENSIONS = Set.of(
            "ads", "adb",
            "c", "h",
            "cpp", "c++", "cc", "hpp", "h++", "hh",
            "c#", "cs",
            "sql", "pls",
            "cfg", "conf", "ini",
            "go",
            "groovy",
            "java",
            "js", "ejs", "cjs", "mjs",
            "ts",
            "lua",
            "m", "mm",
            "pl", "pm",
            "php",
            "py",
            "r",
            "rs",
            "sh", "bash", "zsh",
            "swift",
            "tf", "tfvars",
            "bat",
            "json",
            "toml",
            "html", "htm", "xhtml",
            "yaml", "yml",
            "pu", "puml", "plantuml",
            "feature"
    );

    private OftSupportedFiles() {
    }

    public static boolean isSpecificationFile(final VirtualFile file) {
        return file != null && isSpecificationFileName(file.getName());
    }

    public static boolean isSpecificationFileName(final String fileName) {
        return SPECIFICATION_EXTENSIONS.contains(extensionOf(fileName));
    }

    public static boolean isCoverageTagFile(final VirtualFile file) {
        return file != null && isCoverageTagFileName(file.getName());
    }

    public static boolean isCoverageTagFileName(final String fileName) {
        return TAG_IMPORTER_EXTENSIONS.contains(extensionOf(fileName));
    }

    private static String extensionOf(final String fileName) {
        final int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}
