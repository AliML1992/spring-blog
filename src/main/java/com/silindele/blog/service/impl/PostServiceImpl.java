package com.silindele.blog.service.impl;

import com.silindele.blog.dto.PostDto;
import com.silindele.blog.entity.*;
import com.silindele.blog.repository.CategoryRepository;
import com.silindele.blog.repository.PostRepository;
import com.silindele.blog.repository.TagRepository;
import com.silindele.blog.repository.UserRepository;
import com.silindele.blog.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, TagRepository tagRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public LinkedList<Post> retrieveLatest5() {
        LinkedList<Post> posts = postRepository.retrieveLatest(5);
        return posts;
    }

    @Override
    public void save(PostDto postDto,Post post, Principal principal) {
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getPostBody());
        User user = userRepository.findByUsername(principal.getName());
        post.setUser(user);
        Category category1 = categoryRepository.findByName(postDto.getCategory());
        if (category1 == null){
            Category category = new Category();
            category.setName(postDto.getCategory());
            post.setCategory(category);
        } else {
            post.setCategory(category1);
        }
        List<String> items = split(postDto.getTag());
        for (String item : items) {
            Tag tag1 = tagRepository.findByName(item);
            if (tag1 != null){
                post.addTag(tag1);
            } else {
                Tag tag2 = new Tag(item);
                post.addTag(tag2);
            }
        }
        postRepository.save(post);
    }

    @Override
    public List<Post> getNonActive() {
        List<Post> posts = postRepository.retrieveNonActive();
        return posts;
    }

    @Override
    public Page<Post> retrieveByPage(int pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1 , 3 );
        return postRepository.retrieveAll(pageable);
    }

    public List<String> split(String str){
        return Stream.of(str.split(","))
                .map (elem -> new String(elem))
                .collect(Collectors.toList());
    }


}
