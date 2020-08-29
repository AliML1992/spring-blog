package com.silindele.blog.controller;

import com.silindele.blog.dto.PostDto;
import com.silindele.blog.dto.ProfileDto;
import com.silindele.blog.entity.Post;
import com.silindele.blog.entity.User;
import com.silindele.blog.entity.UserProfile;
import com.silindele.blog.repository.PostRepository;
import com.silindele.blog.repository.ProfileRepository;
import com.silindele.blog.repository.UserRepository;
import com.silindele.blog.service.PostService;
import com.silindele.blog.service.UserService;
import org.springframework.data.domain.Page;
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
    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(PostService postService,
                          PostRepository postRepository,
                          UserRepository userRepository, UserService userService) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userService = userService;
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

    @GetMapping("/profile")
    public String showProfile(Model model,HttpServletRequest request){
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        UserProfile userProfile = user.getUserProfile();
        model.addAttribute("user", user);
        model.addAttribute("userProfile", userProfile);
        return "user/profile";
    }

    @GetMapping("/updateProfile")
    public String updateProfile( Model model){
        ProfileDto profileDto = new ProfileDto();
        model.addAttribute("profile", profileDto);
        return "user/profileForm";
    }

    @PostMapping("/updateProfile")
    public String saveProfile(@Valid @ModelAttribute("profile") ProfileDto profileDto,
                              BindingResult bindingResult,
                              HttpServletRequest request,
                              Model model){
        String username = request.getUserPrincipal().getName();
        User  user = userRepository.findByUsername(username);
        if(bindingResult.hasErrors()){
            model.addAttribute("profile", new ProfileDto());
            return "user/profileForm";
        }
        userService.saveProfile(profileDto,user);
        MultipartFile userImage = profileDto.getImage();

        if (userImage != null){
            try {
                byte[] bytes = userImage.getBytes();
                String name = user.getId() + ".png";
                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(new File("src/main/resources/static/images/writer/" + name)));
                stream.write(bytes);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "redirect:/user/profile";
    }
}
