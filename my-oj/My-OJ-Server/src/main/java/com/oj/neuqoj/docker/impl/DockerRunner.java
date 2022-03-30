package com.oj.neuqoj.docker.impl;

import com.oj.neuqoj.docker.Runner;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

// 10730 gcc:7.3 |  20800 openjdk:8 | 21100 openjdk:11 | 30114 golang:1.14
public class DockerRunner implements Runner, Callable<Map<String, Object>> {

    public static final String DOCKER_CONTAINER_WORK_DIR = "/usr/codeRun";
    private static Map<Integer, String> imageMap = new HashMap<>();
    private DockerClient docker;
    private String id;
    //private List<Image> Images = new ArrayList<>();

    //准备判题的具体题目、用户数据
    private String name;
    private String[][] commandLine;
    private int imageType;
    private long memoryLimit;

    static{
        imageMap.put(25695, "hello-world:latest");
        imageMap.put(10730, "gcc:7.3");
        imageMap.put(20800, "openjdk:8");
        imageMap.put(21100, "openjdk:11");
        imageMap.put(30114, "golang:1.14");
    }
    //初始化容器
    public long init(int type) {

        long startTime = System.currentTimeMillis();
        System.out.println("开始初始化docker");

        try{
            //初始化docker代理
            docker = DefaultDockerClient.builder()
                    .uri(URI.create("https://39.106.160.174:2375"))
                    .dockerCertificates(new DockerCertificates(Paths.get("E:\\JavaWeb\\springboot\\NEUQ-OJ\\NEUQ-OJ-Server\\src\\main\\resources\\dockercerts")))
                    .build();
            System.out.println("docker_client初始化成功");

            //记录已有镜像信息
            /*
            System.out.println("开始记录docker_client_images");
            Images = docker.listImages();
            Iterator<Image> i = Images.listIterator();
            while(i.hasNext()){
                System.out.println(i.next());
            }
            System.out.println("images记录完毕");
            */

            //开始创建容器
            System.out.println("开始创建docker_container");
            //让容器持续开启
            //添加卷
            //设置docker工作卷
            ContainerConfig containerConfig = ContainerConfig.builder()
                    //让容器持续开启
                    .openStdin(true)
                    //添加卷
                    .addVolume(DOCKER_CONTAINER_WORK_DIR)
                    //设置docker工作卷
                    .workingDir(DOCKER_CONTAINER_WORK_DIR)
                    .image(imageMap.get(type))
                    .build();
            ContainerCreation creation = docker.createContainer(containerConfig);

            //记录容器id
            id = creation.id();

            // 获取容器信息
            final ContainerInfo info = docker.inspectContainer(id);
            System.out.println(info.toString());


            System.out.println("docker_container创建完毕");
            System.out.println("docker初始化成功");
        }catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    //停止容器：记录停止时间
    public long kill(){
        long startTime = System.currentTimeMillis();
        try{
            //停止容器
            docker.stopContainer(id, 0);
            System.out.println("停止容器成功");
            //移除容器
            docker.removeContainer(id);
            System.out.println("已移除容器");
            //关闭docker代理
            docker.close();
            System.out.println("docker代理已关闭");
        }catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("本次判题结束，正在返回结果...");
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    //准备判题数据
    public void ready(String name, String[][] commandLine, int imageType, long memoryLimit){
        this.name = name;
        this.commandLine = commandLine;
        this.imageType = imageType;
        this.memoryLimit = memoryLimit;
    }

    @Override
    public Map<String, Object> call() throws Exception {
        return judge();
    }

    public Map<String, Object> judge() throws InterruptedException, IOException, DockerException{
        Map<String, Object> res = new HashMap<>();
        res.put("initTime", init(imageType) + "ms");

        //连接container
        System.out.println("连接容器");
        docker.startContainer(id);

        //将本地文件夹共享至容器内部
        docker.copyToContainer(new File
                //相对路径：src/resources/myCode
                //Linux绝对路径：/home/MyOJResources/codeResources/
                ("E:\\JavaWeb\\springboot\\NEUQ-OJ\\Code-Src\\" + name).toPath(), id, "/usr/codeRun/");


        //开始在容器内部执行命令执行
        //编译java文件
        System.out.println("开始编译...");
        ExecCreation execCompile = docker.execCreate(
                id, commandLine[0], DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());

        ExecState compileState = docker.execInspect(execCompile.id());
        //执行编译命令
        LogStream l = docker.execStart(execCompile.id());
        while(compileState.running()){};
        String compileOutput = l.readFully();

        if(compileOutput.equals("")){
            System.out.println("编译成功");
        } else {//编译错误
            System.out.println(compileOutput);
            res.put("status", 2);
            //截取编译信息，去掉头部java文件名
            res.put("compileInfo", compileOutput.substring(compileOutput.indexOf(":")+1));
            res.put("destroyTime", kill() + "ms");
            res.put("passNum", 0);
            return res;
        }


        //编译完成，执行class文件
        ExecCreation execCreation = docker.execCreate(
                id, commandLine[1], DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());


        //获取命令的运行结果
        LogStream output = null;
        if(!compileState.running()){
            output = docker.execStart(execCreation.id());
        }
        String execOutput = output != null ? output.readFully() : "未知错误";



        //获取运行状态
        ExecState state = docker.execInspect(execCreation.id());


        //等待运行完成
        System.out.println("即将运行程序..");

        while(state.running()){};
        System.out.println("运行结束");

        //从打印信息execOutput种获取结果
        String[] info = execOutput.split("\n");
        int status = Integer.parseInt(info[0].trim());

        //解答错误
        if(status == 3){
            res.put("status", status);
            res.put("passNum", info[1]);
            res.put("srcData", info[2]);
            res.put("realAns", info[3]);
            res.put("curAns", info[4]);
            res.put("destroyTime", kill() + "ms");
            return res;
        } else if(status == 4) {//超时
            res.put("status", status);
            res.put("passNum", info[1]);
            res.put("srcData", info[2]);
            res.put("timeLimit", info[3]);
            res.put("duration", info[4]);
            res.put("destroyTime", kill() + "ms");
            return res;
        } else {
            res.put("passNum", info[1]);
            res.put("averageTime", info[2]);
        }


        //在容器外，即服务器主机上执行shell命令 docker stats --no-stream --format "memory:{{.MemUsage}}" + 容器id，获取容器内存占用
        Process pro = Runtime.getRuntime().exec("sh -c docker stats --no-stream --format \"memory:{{.MemUsage}}\"" + id);
        BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream()));
        StringBuilder mem = new StringBuilder();
        String str;
        while ((str = buf.readLine()) != null) {
            mem.append(str);
        }
        String memory = "0MiB";
        if(mem.length()!=0){
            memory = mem.substring(mem.indexOf(":"), mem.indexOf("/"));
        }


        //超出内存限制
        if(Integer.parseInt(memory.substring(0, memory.indexOf("M"))) > memoryLimit){
            res.put("status", 5);
            res.put("memoryLimit", memoryLimit);
            res.put("memoryUsage", memory);
            res.put("destroyTime", kill() + "ms");
        }else{//通过
            res.put("status", 1);
            res.put("memoryUsage", memory);
            res.put("destroyTime", kill() + "ms");
        }

        return res;
    }




    //终极版本
    //让代码运行于docker并返回结果
    public Map<String, Object> Run(String workPath, String inputFilePath, String[][] commandLine,
                                   int imageType, int[] timeLimit, int[] memoryLimit) {
        Map<String, Object> res = new HashMap<String, Object>();
        init(imageType);
        return res;
    }

    //测试运行
    public Map<String, Object> runTest(int imageType){
        init(imageType);
        Map<String, Object> res = new HashMap<String, Object>();
        try{
            //启动container
            docker.startContainer(id);


            //开始在容器内部执行命令执行
            System.out.println("正在执行命令...");

            //将文件拷贝至容器内部
            docker.copyToContainer(new java.io.File("E:\\Java\\DockerRunner\\src\\main\\resources\\code in docker\\").toPath(), id, "/usr/codeRun/");


            //执行命令
            final String[] command1 = {"javac", "HelloWorld.java"};
            ExecCreation execCreation1 = docker.execCreate(
                    id, command1, DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr());

            final String[] command2 = {"java", "HelloWorld"};
            ExecCreation execCreation2 = docker.execCreate(
                    id, command2, DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr());

            //获取命令的运行结果
            final LogStream output1 = docker.execStart(execCreation1.id());
            final String execOutput1 = output1.readFully();
            final LogStream output2 = docker.execStart(execCreation2.id());
            final String execOutput2 = output2.readFully();

            //获取运行状态
            final ExecState state1 = docker.execInspect(execCreation1.id());
            final ExecState state2 = docker.execInspect(execCreation2.id());

            //等待运行完成
            System.out.println("正在运行...");
            while(state1.running()){};
            while(state2.running()){};

            String ans = execOutput2.substring(0, execOutput2.indexOf('_'));
            String time = execOutput2.substring(ans.length()+1);

            //将运行结果存于res（map）中返回
            res.put("第一条命令的运行结果", execOutput1);
            res.put("第一条命令的返回值", state1.exitCode());
            res.put("第二条命令的运行结果", execOutput2);
            res.put("第二条命令的返回值", state2.exitCode());

            System.out.println("执行结束");

        }catch(Exception e) {
            e.printStackTrace();
        }finally {
            kill();
        }
        return res;
    }



    public static void main(String[] args) {

        DockerRunner docker = new DockerRunner();
        String[] command1 = {"java", "HelloWorld"};
        String[] command2 = {"gcc", "main.c", "-o", "main", "main"};
        String[] command3 = {"java", "test/Part3"};

        Map<String, Object> res = docker.runTest(20800);
        //测试创建、运行、停止时间
        /*
        long initTime = Integer.parseInt(res.get("创建容器时间").toString());
        long stopTime = Integer.parseInt(res.get("停止容器时间").toString());
        long runningTime = Integer.parseInt(res.get("运行时间").toString());

        for (int i = 0; i < 499; i++) {
            HashMap<String, Object> temp = docker.run(command1, 20800, 0, 252);
            initTime += Integer.parseInt(temp.get("创建容器时间").toString());
            stopTime += Integer.parseInt(temp.get("停止容器时间").toString());
            runningTime += Integer.parseInt(temp.get("运行时间").toString());
            System.out.println(i+2);
        }
        System.out.println("总创建容器时间：" + initTime + "   总运行时间：" + runningTime + "    总停止容器时间：" + stopTime);
        System.out.println();
        */
        System.out.println();
        for(String str: res.keySet()){
            if(str.equals("运行结果")){
                System.out.println("\n运行结果：\n" + res.get(str));
            }else{
                System.out.print(str + ":" + res.get(str) + "    ");
            }
        }
        System.out.println("finished!");

    }


}


