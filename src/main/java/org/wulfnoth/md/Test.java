package org.wulfnoth.md;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Objects;

public class Test {

    private String destDirPath = "D:/test/new";

    private static String getFileName(String originalName) {
        return originalName.substring(0, originalName.length()-2);
    }



    private void create(File dir) throws IOException {
        if (dir.exists())
            dir.mkdir();
        else if (!dir.isDirectory()) {
            dir.delete();
            dir.mkdir();
        }

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

    private void initResources() throws IOException {
        File cssDir = new File(destDirPath + File.separator + "css");
        File jsDir = new File(destDirPath + File.separator + "js");

        create(cssDir);
        create(jsDir);
    }

    public void run(File srcMd, File destHtml) throws IOException {
//        File file = new File("C:\\Users\\congj\\OneDrive\\Documents\\Feature Engineering\\chapter 3\\Text_Data_Flattening_Filtering_Chunking.md");

        FinalPage page = MarkdownParser.parser(srcMd);

        page.replaceImgTag(srcMd.getParent());

//        File out = new File(destDirPath + File.separator + getFileName(srcMd.getName()) + "html");

        try (FileOutputStream outputStream = new FileOutputStream(destHtml)) {
            outputStream.write(page.getHTML().getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void entireDir(String rootDir) throws IOException {
        initResources();

        File file = new File(rootDir);
        File[] subDirs = file.listFiles(File::isDirectory);

        if (subDirs != null) {
            for (File subDir : subDirs) {
                String chapterName = subDir.getName();
                File[] subFiles = subDir.listFiles(f -> f.getName().endsWith("md"));
                if (subFiles != null) {
                    for (File f : subFiles) {
                        run(f, new File(destDirPath + File.separator + chapterName + ".html"));
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
//        new Test().entireDir("C:\\Users\\congj\\OneDrive\\Documents\\Feature Engineering");
        
        Test t = new Test();
        t.run(new File("C:/Users/congj/OneDrive/Documents/Feature Engineering/chapter 5/Categorical Variables.md"),
                new File(t.destDirPath + File.separator + "chapter5test.html"));
//        new Test().tries();
    }

}
