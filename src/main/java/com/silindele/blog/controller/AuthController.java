package com.silindele.blog.controller;

import com.silindele.blog.dto.UserDto;
import com.silindele.blog.entity.User;
import com.silindele.blog.entity.VerificationToken;
import com.silindele.blog.error.UserAlreadyExistException;
import com.silindele.blog.event.OnRegistrationCompleteEvent;
import com.silindele.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Locale;

@Controller
public class AuthController {


    private final UserService userService;
    private final MessageSource messages;
    private final JavaMailSender mailSender;
    private final ApplicationEventPublisher eventPublisher;
    private final UserDetailsService userDetailsService;
    private final Environment env;

    @Autowired
    public AuthController(UserService userService, @Qualifier("messageSource") MessageSource messages, JavaMailSender mailSender, ApplicationEventPublisher eventPublisher, UserDetailsService userDetailsService, Environment env) {
        this.userService = userService;
        this.messages = messages;
        this.mailSender = mailSender;
        this.eventPublisher = eventPublisher;
        this.userDetailsService = userDetailsService;
        this.env = env;
    }


    @GetMapping("/register")
    public String signup(Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("userDto", userDto);
        return "signup";
    }

    @PostMapping("/register")
    public ModelAndView userSave(@ModelAttribute("userDto") @Valid UserDto userDto,
                                 HttpServletRequest request,
                                 Errors errors) {
        try {
            final User user = userService.registerNewUser(userDto);
            final String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl));
        } catch (UserAlreadyExistException e) {
            ModelAndView mav = new ModelAndView("signup", "userDto", userDto);
            String errMsg = messages.getMessage("message.regError", null, request.getLocale());
            mav.addObject("message", errMsg);
            return mav;
        } catch (final RuntimeException ex) {
            return new ModelAndView("emailError", "userDto", userDto);
        }
        ModelAndView modelAndView = new ModelAndView("successRegister", "userDto", userDto);
        modelAndView.addObject("email", userDto.getEmail());
        return modelAndView;
    }

    @GetMapping("/registrationConfirm")
    public String confirmRegistration(final HttpServletRequest request, final Model model, @RequestParam("token") final String token) {
        final Locale locale = request.getLocale();

        final VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            final String message = messages.getMessage("auth.message.invalidToken", null, locale);
            model.addAttribute("message", message);
            return "redirect:/badUser";
        }

        final User user = verificationToken.getUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            model.addAttribute("message", messages.getMessage("auth.message.expired", null, locale));
            model.addAttribute("expired", true);
            model.addAttribute("token", token);
            return "redirect:/badUser";
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        model.addAttribute("message", messages.getMessage("message.accountVerified", null, locale));
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String customLogin() {
        return "login";
    }

    @GetMapping("/badUser")
    public String badUser() {
        return "badUser";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(HttpServletRequest request){
        if (request.isUserInRole("ROLE_USER")){
            return "redirect:/user";
        } else if (request.isUserInRole("ROLE_ADMIN")){
            return "redirect:/admin/home";
        } return "redirect:/badUser";
    }


}


