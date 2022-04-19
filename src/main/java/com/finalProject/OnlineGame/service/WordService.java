package com.finalProject.OnlineGame.service;

import com.finalProject.OnlineGame.dao.UserDao;
import com.finalProject.OnlineGame.dao.WordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WordService {

    @Autowired
    private WordDao wordDao;


    public String[] getWords() {
        return wordDao.getWords();
    }


}
