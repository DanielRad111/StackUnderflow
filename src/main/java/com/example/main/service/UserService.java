package com.example.main.service;

import com.example.main.dto.UserDto;
import com.example.main.model.User;
import com.example.main.repository.UserRepository;
import com.example.main.utils.PasswordHashingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordHashingService passwordHashingService;
    @Autowired
    private NotificationService notificationService;

    public List<UserDto> getAllUsers(){
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<UserDto> getUserById(Long id){
        return this.userRepository.findById(id).map(this::convertToDto);
    }

    public Optional<UserDto> getUserByEmail(String email){
        return this.userRepository.findByEmail(email).map(this::convertToDto);
    }

    public Optional<UserDto> getUserByUsername(String username){
        return this.userRepository.findByUsername(username).map(this::convertToDto);
    }

    public UserDto createUser(String username, String email, String password, String phoneNumber){
        String hashedPassword = passwordHashingService.hashPassword(password);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setPhoneNumber(phoneNumber);
        user.setScore(0);
        user.setBanned(false);
        user.setModerator(false);

        this.userRepository.save(user);
        return convertToDto(user);
    }

    public Optional<UserDto> updateUser(Long id, String username, String email, String phoneNumber) {
        return userRepository.findById(id)
                .map(user -> {
                    if (username != null) {
                        user.setUsername(username);
                    }
                    if (email != null) {
                        user.setEmail(email);
                    }
                    if (phoneNumber != null) {
                        user.setPhoneNumber(phoneNumber);
                    }
                    return convertToDto(userRepository.save(user));
                });
    }

    public Optional<UserDto> banUser(Long id, boolean banned, String reason, Long moderatorId){
        Optional<User> moderatorOpt = userRepository.findById(moderatorId);
        if(moderatorOpt.isEmpty() || !moderatorOpt.get().isModerator()){
            return Optional.empty(); //you have to be a moderator to ban users
        }

        return userRepository.findById(id)
                .map(user -> {
                    if(user.isModerator()){
                        return convertToDto(user);//don't ban moderators
                    }

                    boolean wasBanned = user.isBanned();

                    user.setBanned(banned);
                    if(banned){
                        user.setBanReason(reason);
                        if(!wasBanned){
                            notificationService.sendBanNotification(user,reason);
                        }
                    }else{
                        user.setBanReason(null);
                    }

                    return convertToDto(userRepository.save(user));
                });
    }

    public Optional<UserDto> setModerator(Long id, boolean isModerator, Long moderatorId){
        boolean hasModerators = userRepository.findAll().stream().anyMatch(User::isModerator);
        if(hasModerators) {
            Optional<User> adminOpt = userRepository.findById(moderatorId);
            if (adminOpt.isEmpty() || !adminOpt.get().isModerator()) {
                return Optional.empty(); //you have to be a moderator to make other users moderators
            }
        }
        return userRepository.findById(id)
                .map(user -> {
                    user.setModerator(isModerator);
                    return convertToDto(userRepository.save(user));
                });
    }

    public Optional<UserDto> updateScore(Long id, float scoreChange){
        return userRepository.findById(id)
                .map(user -> {
                    user.setScore(user.getScore() + scoreChange);
                    return convertToDto(userRepository.save(user));
                });
    }

    public boolean deleteUser(Long id, Long moderatorId){
        Optional<User> moderatorOpt = this.userRepository.findById(moderatorId);
        if(moderatorOpt.isEmpty() || !moderatorOpt.get().isModerator()){
            return false; //you have to be a moderator to delete users
        }
        if(userRepository.existsById(id)){
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean authenticate(String username, String password){
        User user = userRepository.findByUsername(username).orElse(null);
        if(user == null){
            return false;
        }
        return passwordHashingService.verifyPassword(password, user.getPasswordHash());
    }

    public boolean isBanned(String username){
        return userRepository.findByUsername(username)
                .map(User::isBanned)
                .orElse(true);
    }

    public String getBanReason(String username){
        return userRepository.findByUsername(username)
                .map(User::getBanReason)
                .orElse(null);
    }

    public boolean isModerator(String username){
        return userRepository.findByUsername(username)
                .map(User::isModerator)
                .orElse(false);
    }

    private UserDto convertToDto(User user){
        UserDto dto = new UserDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setModerator(user.isModerator());
        dto.setScore(user.getScore());
        dto.setBanned(user.isBanned());
        dto.setBanReason(user.getBanReason());
        return dto;
    }

    public User findUserEntityById(Long id){
        return userRepository.findById(id).orElse(null);
    }
}
