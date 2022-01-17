package com.finalProject.OnlineGame.dao;

import com.finalProject.OnlineGame.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserDao {

    User getUserById(Integer userId);

    void addUser(User user);
}
