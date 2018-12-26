package org.wulfnoth.md;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Test {

    private static String getFileName(String originalName) {
        return originalName.substring(0, originalName.length()-2);
    }



    private void create(File dir) throws IOException {
        if (!dir.exists())
            dir.mkdirs();

        File sourceDir = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource(dir.getName())).getPath());

        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                File target = new File(dir.getPath() + File.separator + file.getName());
                if (!target.exists()) {
                    IOUtils.copy(new FileInputStream(file), new FileOutputStream(target));
                }
            }
        }
    }

    private void initResources(String targetDir) throws IOException {
        File cssDir = new File(targetDir + File.separator + "css");
        File jsDir = new File(targetDir + File.separator + "js");
        System.out.println(cssDir.getAbsolutePath());
        create(cssDir);
        create(jsDir);
    }

    public void run(String targetDir, File file, Map<String, String> config) throws IOException {
        FinalPage page = MarkdownParser.parser(file);

        page.replaceImgTag(file.getParent());

        File targetFile = new File(targetDir + File.separator + file.getName().substring(0, file.getName().length()-3) + ".html");
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        } else if (!targetFile.getParentFile().isDirectory()) {
            throw new IOException(String.format("%s is exist but not directory", targetDir));
        }
        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(page.getHTML(config).getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recursiveHandle(String rootDir, String targetDir, Map<String, String> config) throws IOException {
        File root = new File(rootDir);

        for (File subFile : Objects.requireNonNull(root.listFiles())) {
            if (subFile.isDirectory()) {
                recursiveHandle(rootDir + File.separator + subFile.getName(), targetDir + File.separator + subFile.getName(), config);
            } else if (subFile.getName().endsWith(".md")) {
                run(targetDir, subFile, config);
            }
        }
    }

    public void entireDir(String rootDir, String targetDir) throws IOException {
//
        initResources(targetDir);

        Map<String, String> config = new HashMap<>();
        config.put("js", targetDir + File.separator + "js");
        config.put("css", targetDir + File.separator + "css");

        recursiveHandle(rootDir, targetDir, config);

    }

    public static void main(String[] args) throws IOException {
//        new Test().entireDir("C:\\Users\\congj\\OneDrive\\Documents\\Feature Engineering");

        Test t = new Test();
        t.entireDir("C:\\Users\\congj\\Desktop\\notebook", "C:\\Users\\congj\\Desktop\\result");
//        t.entireDir("C:\\Users\\congj\\Desktop\\notebook\\xgboost\\debug", "C:\\Users\\congj\\Desktop\\result");

//        t.entireDir();
//        t.run(new File("C:/Users/congj/OneDrive/Documents/Feature Engineering/chapter 5/Categorical Variables.md"),
//                new File(t.destDirPath + File.separator + "chapter5test.html"));
//        new Test().tries();
    }

}
