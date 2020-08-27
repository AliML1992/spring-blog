package com.silindele.blog.service;

import com.silindele.blog.dto.PostDto;
import com.silindele.blog.entity.Post;
import org.springframework.data.domain.Page;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;

public interface PostService {

    LinkedList<Post> retrieveLatest5();
    void save(PostDto postDto, Post post, Principal principal);
    List<Post> getNonActive();
    Page<Post> retrieveByPage(int pageNo);
    Page<Post> retrieveForHomePage(int pageNo);
}
