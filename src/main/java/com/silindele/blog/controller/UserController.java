package com.silindele.blog.controller;

import com.silindele.blog.dto.PostDto;
import com.silindele.blog.entity.Post;
import com.silindele.blog.repository.PostRepository;
import com.silindele.blog.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.Principal;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("/user")
public class UserController {

    private final PostService postService;
    private final PostRepository postRepository;

    public UserController(PostService postService, PostRepository postRepository) {
        this.postService = postService;
        this.postRepository = postRepository;
    }

    @GetMapping("/post/add")
    public String addPost(Model model){
        PostDto post = new PostDto();
        model.addAttribute("post", post);
        return "user/add";
    }



    @PostMapping("/post/add")
    public String addPost(@ModelAttribute("post") @Valid PostDto postDto,
                          BindingResult bindingResult,
                          HttpServletRequest request,
                          Model model,
                          RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()){
            model.addAttribute("post", new PostDto());
            return "user/add";
        }
        Post post = new Post();
        Principal principal = request.getUserPrincipal();
        postService.save(postDto,post,principal);
        MultipartFile postImage = postDto.getPostImage();

        if (postImage != null){
            try {
                byte[] bytes = postImage.getBytes();
                String name = post.getId() + ".png";
                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(new File("src/main/resources/static/images/post/" + name)));
                stream.write(bytes);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", "New Post has been saved successfully");
        return "redirect:/user";
    }

    @GetMapping
    public String home(Model model){
        List<Post> posts = postService.getNonActive();
        if (posts == null){
            model.addAttribute("nonActiveMessage", "There are no non-active posts");
            return "/user/home";
        }
        model.addAttribute("posts", posts);
        return "/user/home";
    }

    @GetMapping("/post/{id}/preview")
    public String previewPost(@PathVariable(name = "id") Long postId, Model model){
        Post post = postRepository.findById(postId).orElseThrow(null);
        if ( post == null ){
            model.addAttribute("postNotFound", "Post Not Found");
            return "error";
        }
        model.addAttribute("post", post);
        return "/user/single-post";
    }

    @GetMapping("/post/{id}/edit")
    public String editPost(@PathVariable(name = "id") Long postId, Model model){
        Post post = postRepository.findById(postId).orElseThrow(null);
        if ( post == null ){
            model.addAttribute("postNotFound", "Post Not Found");
            return "error";
        }
        PostDto postDto = new PostDto();
        postDto.setId(postId);
        postDto.setTitle(post.getTitle());
        postDto.setPostBody(post.getContent());
        model.addAttribute("postDto", postDto);
        return "/user/edit";
    }

    @PostMapping("/post/{id}/edit")
    @Transactional
    public String saveEditedPost(@PathVariable(name = "id") Long postId,
                                 @ModelAttribute("postDto") PostDto postDto,
                                 RedirectAttributes redirectAttributes){
        String title = postDto.getTitle();
        String content = postDto.getPostBody();
        Date date = new Date();
        postRepository.updatePostById(title,content,date, postId);
        redirectAttributes.addFlashAttribute("updatedMessage", "Post with id: " + postId + " has been updated successfully ");
        return "redirect:/user";
    }

    @PostMapping("/post/{id}/publish")
    @Transactional
    public String publishPost(@PathVariable(name = "id") Long postId, RedirectAttributes redirectAttributes){
        postRepository.publishPostById(true, postId);
        redirectAttributes.addFlashAttribute("publishedMessage", "Post with id: " + postId + " has been published successfully ");
        return "redirect:/user";
    }

    @GetMapping("/post/all")
    public String showPosts(Model model){
        return showPostsByPage(model, 1);
    }

    @GetMapping("/post/all/{pageNo}")
    public String showPostsByPage(Model model, @PathVariable(value = "pageNo") int currentPage){
        Page<Post> page = postService.retrieveByPage(currentPage);
        long totalItems = page.getTotalElements();
        int totalPages = page.getTotalPages();
        List<Post> postList = page.getContent();
        if (postList.isEmpty()) {
            model.addAttribute("noActivePosts", "There are no active posts yet");
            return "user/posts";
        }
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("postList", postList);
        return "user/posts";

    }

    @GetMapping("/post/{id}/delete")
    public String deletePostById(@PathVariable("id") Long postId, RedirectAttributes model){
        postRepository.deleteById(postId);
        model.addFlashAttribute("deletedMessage","Post with id: " + postId + "has been successfully deleted");
        return "redirect:/user/post/all";
    }
}
