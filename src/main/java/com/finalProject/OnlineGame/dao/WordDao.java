package com.finalProject.OnlineGame.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface WordDao {
    String[] getWords();
}
