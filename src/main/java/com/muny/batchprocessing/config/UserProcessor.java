package com.muny.batchprocessing.config;

import com.muny.batchprocessing.dto.UserInput;
import com.muny.batchprocessing.entity.UserMapper;
import com.muny.batchprocessing.entity.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class UserProcessor implements ItemProcessor<UserInput, Users> {
    private static final Logger log = LoggerFactory.getLogger(UserProcessor.class);

    @Autowired
    private UserMapper userMapper;

    @Override
    public Users process(UserInput user) throws Exception {
        log.info("processing items: {}",user);
        Users users = userMapper.usersInputToUsers(user);
        users.setUsername(user.first_name()+"."+user.last_name());
        return users;
    }
}
