package com.oj.neuqoj.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class IOUtil {

    public boolean writeAns(String answer, String name, String username, int lang){
        //若为java，加上包名
        if(lang == 20800 || lang == 21100){
            answer = "package workDir;\n" + answer;
        }

        FileOutputStream out = null;
        //绝对路径：E:\JavaWeb\MyOJ\src\main\resources\myCode\
        //相对路径：src/main/resources/myCode/
        //linux绝对路径：/home/MyOJResources/codeResources/
        try {
            out = new FileOutputStream("E:\\JavaWeb\\springboot\\NEUQ-OJ\\Code-Src\\" + name + "\\" + username + ".java");
            byte[] b = answer.getBytes();
            out.write(b);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO错误");
            return false;
        }
        return true;
    }
}
