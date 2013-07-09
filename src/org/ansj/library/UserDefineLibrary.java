package org.ansj.library;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import love.cq.domain.Forest;
import love.cq.domain.Value;
import love.cq.library.Library;
import love.cq.util.IOUtil;
import love.cq.util.StringUtil;

import org.ansj.util.MyStaticValue;

/**
 * 用户自定义词典操作类
 * 
 * @author ansj
 */
public class UserDefineLibrary {

    public static final String DEFAULT_NATURE = "userDefine";

    public static final Integer DEFAULT_FREQ = 1000;

    public static final String DEFAULT_FREQ_STR = "1000";

    public static Forest FOREST = null;

    public static Forest ambiguityForest = null;

    private static final HashMap<String, Forest> userForestMap = new HashMap<String, Forest>();

    static {
        initUserLibrary();
        initAmbiguityLibrary();
    }

    /**
     * 关键词增加
     * 
     * @param keyWord
     *            所要增加的关键词
     * @param nature
     *            关键词的词性
     * @param freq
     *            关键词的词频
     */
    public static void insertWord(String keyword, String nature, int freq) {
        String[] paramers = new String[2];
        paramers[0] = nature;
        paramers[1] = String.valueOf(freq);
        Value value = new Value(keyword, paramers);
        Library.insertWord(FOREST, value);
    }

    /**
     * 加载纠正词典
     */
    private static void initAmbiguityLibrary() {
        // TODO Auto-generated method stub
        String ambiguityLibrary = MyStaticValue.rb.getString("ambiguityLibrary");
        if (StringUtil.isBlank(ambiguityLibrary)) {
            System.err.println("init ambiguity  error :" + ambiguityLibrary
                + " because : not find that file or can not to read !");
            return;
        }
        File file = new File(ambiguityLibrary);
        if (file.isFile() && file.canRead()) {
            try {
                ambiguityForest = Library.makeForest(ambiguityLibrary);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.err.println("init ambiguity  error :" + ambiguityLibrary
                                   + " because : not find that file or can not to read !");
                e.printStackTrace();
            }
            System.out.println("init redressLibrary ok!");
        }else{
            System.err.println("init ambiguity  error :" + ambiguityLibrary
                + " because : not find that file or can not to read !");
        }
    }

    /**
     * 加载用户自定义词典和补充词典
     */
    private static void initUserLibrary() {
        // TODO Auto-generated method stub
        try {
            long start = System.currentTimeMillis();
            FOREST = new Forest();
            // 先加载系统内置补充词典
            initSystemLibrary(FOREST);
            loadLibrary(FOREST, MyStaticValue.userDefinePath);
            System.out.println("init user library ok use time :"
                               + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void initSystemLibrary(Forest FOREST) {
        // TODO Auto-generated method stub
        String temp = null;
        BufferedReader br = null;

        br = MyStaticValue.getSystemLibraryReader();

        try {
            while ((temp = br.readLine()) != null) {
                if (StringUtil.isBlank(temp)) {
                    continue;
                } else {
                    Library.insertWord(FOREST, temp);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            IOUtil.close(br);
        }
    }

    // 单个文件加载词典
    public static void loadFile(Forest forest, File file) {
        // TODO Auto-generated method stub
        if (!file.canRead()) {
            System.err.println("file in path " + file.getAbsolutePath() + " can not to read!");
            return;
        }
        String temp = null;
        BufferedReader br = null;
        String[] strs = null;
        Value value = null;
        try {
            br = IOUtil.getReader(new FileInputStream(file), "UTF-8");
            while ((temp = br.readLine()) != null) {
                if (StringUtil.isBlank(temp)) {
                    continue;
                } else {
                    strs = temp.split("\t");
                    if (strs.length != 3) {
                        value = new Value(strs[0], DEFAULT_NATURE, DEFAULT_FREQ_STR);
                    } else {
                        value = new Value(strs[0], strs[1], strs[2]);
                    }
                    Library.insertWord(forest, value);
                }
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            IOUtil.close(br);
            br = null;
        }
    }

    /**
     * 用户自定义自己的词典,生成
     * @param isSystem 是否加载系统词典
     * @param libraryPaths 词典路径,可以是目录,也可以是具体的文件.如果是目录.只加载后缀为dic的文件
     * @return 返回的词典结构.
     */
    public static Forest makeUserDefineForest(boolean isSystem, String... libraryPaths) {
        Forest forest = new Forest();
        if (isSystem) {
            initSystemLibrary(forest);
        }
        for (String path : libraryPaths) {
            loadLibrary(forest, path);
        }
        return forest;
    }

    /**
     * 加载词典,传入一本词典的路径.或者目录.词典后缀必须为.dic
     */
    public static void loadLibrary(Forest forest, String path) {
        // 加载用户自定义词典
        File file = null;
        if ((path != null || (path = MyStaticValue.rb.getString("userLibrary")) != null)) {
            file = new File(path);
            if (!file.canRead() || file.isHidden()) {
                return;
            }
            if (file.isFile()) {
                loadFile(forest, file);
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().trim().endsWith(".dic")) {
                        loadFile(forest, files[i]);
                    }
                }
            } else {
                System.err.println("init user library  error :" + path
                                   + " because : not find that file !");
            }
        }
    }

    /**
     * 删除关键词
     */
    public static void removeWord(String word) {
        Library.removeWord(FOREST, word);
    }

    /**
     * 将用户自定义词典清空
     */
    public static void clear() {
        FOREST.clear();
    }

    public static HashMap<String, Forest> getUserForestMap() {
        return userForestMap;
    }

}
