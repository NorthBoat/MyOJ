package com.oj.neuqoj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    String account;
    String name;
    String password;
    int root;
    int level;
}