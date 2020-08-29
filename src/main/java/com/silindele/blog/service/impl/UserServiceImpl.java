package com.silindele.blog.service.impl;

import com.silindele.blog.dto.ProfileDto;
import com.silindele.blog.dto.UserDto;
import com.silindele.blog.entity.User;
import com.silindele.blog.entity.UserProfile;
import com.silindele.blog.entity.VerificationToken;
import com.silindele.blog.error.UserAlreadyExistException;
import com.silindele.blog.repository.ProfileRepository;
import com.silindele.blog.repository.UserRepository;
import com.silindele.blog.repository.VerificationTokenRepository;
import com.silindele.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final ProfileRepository profileRepository;

    @Autowired
    public UserServiceImpl(BCryptPasswordEncoder passwordEncoder, UserRepository userRepository, VerificationTokenRepository tokenRepository, ProfileRepository profileRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public User registerNewUser(UserDto userDto) {
        if (usernameExists(userDto.getUsername())){
            throw new UserAlreadyExistException("There is an account with the username: " + userDto.getUsername());
        }
        if (userEmailExists(userDto.getEmail())){
            throw new UserAlreadyExistException("There is an account with the email: " + userDto.getEmail());
        }

        final User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    @Override
    public User getUser(String verificationToken) {
        return null;
    }

    @Override
    public void saveRegisteredUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void createVerificationTokenForUser(final User user, final String token) {
        final VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
    }

    @Override
    public VerificationToken getVerificationToken(String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

    @Override
    public void saveProfile(ProfileDto profileDto, User user) {
        UserProfile profile = new UserProfile();
        profile.setFirstname(profileDto.getFirstname());
        profile.setLastname(profileDto.getLastname());
        profile.setBio(profileDto.getBio());
        profile.setDescription(profileDto.getDescription());
        profile.setTwitterLink(profileDto.getTwitterLink());
        profile.setFacebookLink(profileDto.getFacebookLink());
        profile.setInstagramLink(profileDto.getInstagramLink());
        profile.setUser(user);
        profileRepository.save(profile);
    }


    private boolean usernameExists(String name){
        return userRepository.existsByUsername(name);
    }

    private boolean userEmailExists(String email){
        return userRepository.existsByEmail(email);
    }
}
