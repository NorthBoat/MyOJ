package com.oj.neuqoj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class Topic {
    int num;
    String title;
    String desc;
    String content;
    String tag;
    String from;
    String contact;
}
