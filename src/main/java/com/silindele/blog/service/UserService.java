package com.silindele.blog.service;

import com.silindele.blog.dto.ProfileDto;
import com.silindele.blog.dto.UserDto;
import com.silindele.blog.entity.User;
import com.silindele.blog.entity.VerificationToken;
import com.silindele.blog.error.UserAlreadyExistException;


public interface UserService {

    User registerNewUser(UserDto userDto) throws UserAlreadyExistException;

    User getUser(String verificationToken);

    void saveRegisteredUser(User user);

    void createVerificationTokenForUser(User user, String token);

    VerificationToken getVerificationToken(String VerificationToken);

    void saveProfile(ProfileDto profileDto, User user);

}
