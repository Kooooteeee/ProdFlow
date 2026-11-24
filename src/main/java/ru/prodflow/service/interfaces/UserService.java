package ru.prodflow.service.interfaces;

import ru.prodflow.dto.UpdateUserDto;
import ru.prodflow.dto.request.UserRequestDto;
import ru.prodflow.enums.UserRole;
import ru.prodflow.enums.UserStatus;
import ru.prodflow.model.User;

import java.util.List;

public interface UserService {

    User createUser(UserRequestDto userRequestDto);

    void updateUser(Long id, UpdateUserDto updateUserDto);

    void promoteUsersToAdmins(List<Long> ids);

    void deactivateUsers(List<Long> ids);

    void reactivateUsers(List<Long> ids);

    void deactivateAdmins(List<Long> ids);

    void reactivateAdmins(List<Long> ids);

    void approveUsers(List<Long> ids);

    void rejectUsers(List<Long> ids);

    void changePassword(Long id, String oldPassword, String newPassword);

    User findUserById(Long id);

    User findUserByEmail(String email);

    List<User> findUsersByStatus(UserStatus userStatus);

    List<User> findUsersByRole(UserRole userRole);
}
