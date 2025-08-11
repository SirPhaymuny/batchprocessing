package com.muny.batchprocessing.entity;

import com.muny.batchprocessing.dto.UserInput;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface UserMapper {
    Users usersInputToUsers(UserInput userInputToUsers);
}
