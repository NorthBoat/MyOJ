package com.oj.neuqoj.api;

import com.oj.neuqoj.docker.impl.DockerRunner;
import com.oj.neuqoj.mapper.*;
import com.oj.neuqoj.pojo.*;
import com.oj.neuqoj.utils.CommandUtil;
import com.oj.neuqoj.utils.IOUtil;
import com.oj.neuqoj.utils.ResultUtil;
import com.oj.neuqoj.utils.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@RestController
@CrossOrigin
public class QuestionApi {


    private QuestionMapper questionMapper;
    @Autowired
    public void setQuestionMapper(QuestionMapper questionMapper){
        this.questionMapper = questionMapper;
    }

    private DetailMapper detailMapper;
    @Autowired
    public void setDetailMapper(DetailMapper detailMapper){
        this.detailMapper = detailMapper;
    }

    private ResultMapper resultMapper;
    @Autowired
    public void setResultMapper(ResultMapper resultMapper){
        this.resultMapper = resultMapper;
    }

    private NewsMapper newsMapper;
    @Autowired
    public void setNewsMapper(NewsMapper newsMapper){
        this.newsMapper = newsMapper;
    }

    private InfoMapper infoMapper;
    @Autowired
    public void setInfoMapper(InfoMapper infoMapper){
        this.infoMapper = infoMapper;
    }

    private CommentMapper commentMapper;
    @Autowired
    public void setCommentMapper(CommentMapper commentMapper){
        this.commentMapper = commentMapper;
    }



    

    @RequestMapping("/getRepo")
    public ResultUtil list(@RequestBody Map<String, String> params){
        String tag = params.get("tag");
        //System.out.println(tag);
        if(tag.equals("All")){
            return ResultUtil.success(questionMapper.getAllQuestions());
        }
        return ResultUtil.success(questionMapper.getQuestionByTag(tag));
    }


    @RequestMapping("/getQuestion")
    public ResultUtil getQuestion(@RequestBody Map<String, String> params){
        int num;
        try{
            num = Integer.parseInt(params.get("num"));
        }catch (NumberFormatException e){
            System.out.println("????????????????????????");
            return ResultUtil.failure(ResultCode.PARAM_IS_INVALID);
        }
        Question question = questionMapper.getQuestion(num);
        if(question == null){
            return ResultUtil.failure(ResultCode.DATA_NOT_FOUND);
        }
        return ResultUtil.success(question);
    }


    @RequestMapping("/getQuesNews")
    public ResultUtil getQuesNews(){
        Map<String, Object> res = new HashMap<>();
        List<Question> list = questionMapper.getAllQuestions();
        res.put("ques_num", list.size());
        res.put("new_ques", list.get(list.size()-1));
        //System.out.println(list.size() + " " + list.get(list.size()-1));
        return ResultUtil.success(res);
    }


    @RequestMapping("/getDetail")
    public ResultUtil getDetail(@RequestBody Map<String, String> params){
        int num;
        try{
            num = Integer.parseInt(params.get("num"));
        }catch (NumberFormatException e){
            System.out.println("????????????????????????");
            return ResultUtil.failure(ResultCode.PARAM_IS_INVALID);
        }
        Detail detail = detailMapper.getDetail(num);
        if(detail == null){
            return ResultUtil.failure(ResultCode.DATA_NOT_FOUND);
        }
        return ResultUtil.success(detail);
    }


    @RequestMapping("/judge")
    public ResultUtil judge(@RequestBody Map<String, String> params){
        //??????????????????
        String answer = params.get("answer");
        //????????????????????????????????????????????????
        String username = params.get("username");
        //??????????????????
        int lang = Integer.parseInt(params.get("lang"));
        //?????????????????????????????????????????????????????????????????????
        String name = params.get("name");
        //???????????????????????????
        String level = params.get("level");

        //?????????????????????false??????io?????????????????????????????????
        if(!new IOUtil().writeAns(answer, name, username, lang)){
            return ResultUtil.failure(ResultCode.JUDGE_IO_ERROR);
        }

        //?????????????????????0????????????
        long memoryLimit = Long.parseLong(params.get("memoryLimit"));
        //????????????
        String[][] commandLine = new CommandUtil().createCommand(lang, username);


        //??????docker??????
        DockerRunner dockerRunner = new DockerRunner();
        //????????????
        dockerRunner.offer(name, commandLine, lang, memoryLimit);


        //??????????????????docker????????????
        FutureTask<Map<String, Object>> task = new FutureTask<>(dockerRunner);
        new Thread(task).start();
        if(!task.isDone()){ System.out.println("The task is running"); }
        //?????????????????????
        Map<String, Object> res = null;
        try {
            res = task.get();
        }catch (java.lang.NumberFormatException e){
            return ResultUtil.failure(ResultCode.JUDGE_RUNTIME_EXCEPTION);
        }catch (InterruptedException e) {
            return ResultUtil.failure(ResultCode.JUDGE_SERVER_ERROR);
        } catch (ExecutionException e) {
            return ResultUtil.failure(ResultCode.JUDGE_IO_ERROR);
        }


        //??????????????????
//        if(lang == 20800 || lang == 21100){
//            newsMapper.javaUsingUp(NewsApi.version);
//        } else if(lang == 10730){
//            newsMapper.cUsingUp(NewsApi.version);
//        } else if(lang == 10520){
//            newsMapper.pythonUsingUp(NewsApi.version);
//        }


        int num = Integer.parseInt(params.get("num"));
        Info info = infoMapper.getInfo(username);
        //??????????????????
        if((Integer)res.get("status") == 1 && !info.hasPassed(num)){
            info.pass(num, lang, level);
            info.goodAt();
        }
        infoMapper.updateInfo(info);

        return ResultUtil.success(res);

    }


    @RequestMapping("/storeRes")
    public ResultUtil storeResult(@RequestBody Map<String, String> params){
        String account = params.get("account");
        int num = Integer.parseInt(params.get("num"));
        String title = params.get("title");
        String key1 = params.get("key1");
        String key2 = params.get("key2");
        String key3 = params.get("key3");
        String key4 = params.get("key4");
        String val1 = params.get("val1");
        String val2 = params.get("val2");
        String val3 = params.get("val3");
        String val4 = params.get("val4");
        String code = params.get("code");
        Result result = new Result(account, num, title, key1, key2, key3, key4, val1, val2, val3, val4, code);

        if(result.getCode().length() > 520){
            result.setCode(result.getCode().substring(0, 520));
        }
        if(result.getVal2().length() > 200){
            result.setCode(result.getVal2().substring(0, 200));
        }

        if(resultMapper.getResult(account, num) != null){
            resultMapper.updateResult(result);
        } else {
            resultMapper.setResult(result);
        }
        return ResultUtil.success();
    }


    @RequestMapping("/getRes")
    public ResultUtil getRes(@RequestBody Map<String, String> params){
        //System.out.println(params.get("account"));
        String account = params.get("account");
        int num = Integer.parseInt(params.get("num"));
        Result res = resultMapper.getResult(account, num);
        if(res == null){
            return ResultUtil.failure(ResultCode.DATA_NOT_FOUND);
        }
        return ResultUtil.success(res);
    }


    @RequestMapping("/getComments")
    public ResultUtil getComments(@RequestBody Map<String, String> params){
        int question = Integer.parseInt(params.get("question"));
        List<Comment> comments = commentMapper.getComments(question);
        for(Comment c: comments){
            c.setComments(comments);
        }
        List<Comment> res = new ArrayList<>();
        for(Comment c: comments){
            if(c.getTo() == 0){
                res.add(c);
            }
        }
        return ResultUtil.success(res);
    }


    @RequestMapping("/comment")
    public ResultUtil comment(@RequestBody Map<String, Object> params){
        int question = Integer.parseInt(params.get("question").toString());
        String from = params.get("from").toString();
        int to = Integer.parseInt(params.get("to").toString());
        String content = params.get("content").toString();

        if(content.length() > 200){
            return ResultUtil.failure(ResultCode.PARAM_IS_INVALID);
        }
        commentMapper.comment(new Comment(question, from, to, content));
        return ResultUtil.success();
    }
}
