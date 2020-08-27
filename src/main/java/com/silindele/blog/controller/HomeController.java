package com.silindele.blog.controller;

import com.silindele.blog.entity.Post;
import com.silindele.blog.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class HomeController {

    private final PostService postService;

    public HomeController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String home(Model model) {
        return homeByPage(model, 1);
    }

    @GetMapping("/page/{pageNo}")
    public String homeByPage(Model model, @PathVariable(value = "pageNo") int currentPage) {
        Page<Post> page = postService.retrieveForHomePage(currentPage);
        int totalPages = page.getTotalPages();
        List<Post> postList = page.getContent();
        if (postList.isEmpty()) {
            model.addAttribute("noActivePosts", "There are no active posts yet");
            return "index";
        }
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("postList", postList);
        return "index";
    }

    @GetMapping("/post/{postId}")
    public String showSinglePost(@PathVariable("postId") Long postId,Model model){

    }

}