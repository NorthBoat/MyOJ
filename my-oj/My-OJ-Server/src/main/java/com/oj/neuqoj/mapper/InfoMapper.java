package com.oj.neuqoj.mapper;



import com.oj.neuqoj.pojo.Info;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InfoMapper {

    @Insert("insert into `info`(`account`, `name`, `register_time`) values(#{account}, #{name}, #{register_time})")
    void addInfo(Info info);

    @Select("select * from `info`")
    List<Info> getAllInfo();

    @Update("update `info` set finished=#{finished}, java_finished=#{java_finished}, c_finished=#{c_finished}, go_finished=#{go_finished}, finished_index=#{finished_index}, skillful_lang=#{skillful_lang} where `account`=#{account}")
    void updateInfo(Info info);

    @Select("select * from `info` where `account`=#{account}")
    Info getInfo(String account);
}
