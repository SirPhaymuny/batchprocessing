package com.muny.batchprocessing.config;

import com.muny.batchprocessing.entity.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class UserProcessor implements ItemProcessor<Users, Users> {
    private static final Logger log = LoggerFactory.getLogger(UserProcessor.class);
    @Override
    public Users process(Users user) throws Exception {
        final String firstName = user.getFirst_name().toUpperCase();
        final String lastName = user.getLast_name().toUpperCase();
        final Users transformUser = new Users(user.getId(),firstName,lastName, user.getUsername());
        log.info("Converting ({}) into ({})", user, transformUser);
        return transformUser;
    }
}
