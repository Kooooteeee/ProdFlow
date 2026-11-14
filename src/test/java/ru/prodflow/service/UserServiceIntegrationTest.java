package ru.prodflow.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.prodflow.dto.request.UserRequestDto;
import ru.prodflow.enums.UserRole;
import ru.prodflow.enums.UserStatus;
import ru.prodflow.exception.BadCredentialsException;
import ru.prodflow.model.User;
import ru.prodflow.service.interfaces.UserService;
import ru.prodflow.dto.UpdateUserDto;
import ru.prodflow.exception.ConflictException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Test
    public void createUser_WhenValid_CreatesUser() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("test2@example.com");
        userRequestDto.setName("testName");
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);

        entityManager.flush();
        entityManager.clear();

        User foundUser = userService.findUserById(savedUser.getId());

        assertEquals("testName", foundUser.getName());
        assertEquals("test2@example.com", foundUser.getEmail());
        assertEquals(UserRole.CUSTOMER, foundUser.getRole());
        assertEquals(UserStatus.PENDING, foundUser.getStatus());
        assertTrue(passwordEncoder.matches("testPassword", foundUser.getPasswordHash()));
    }

    @Transactional
    @Test
    public void changePassword_WhenValid_ChangesPassword() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("test2@example.com");
        userRequestDto.setName("testName");
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);

        userService.changePassword(savedUser.getId(), "testPassword", "newTestPassword");

        entityManager.flush();
        entityManager.clear();

        User foundUser = userService.findUserById(savedUser.getId());

        assertTrue(passwordEncoder.matches("newTestPassword", foundUser.getPasswordHash()));
    }

    @Transactional
    @Test
    public void changePassword_WhenIncorrectPassword_ThrowsException() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("test2@example.com");
        userRequestDto.setName("testName");
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);

        String originalHash = savedUser.getPasswordHash();

        assertThrows(BadCredentialsException.class, () -> userService.
                changePassword(savedUser.getId(), "incorrectTestPassword", "newTestPassword"));

        entityManager.flush();
        entityManager.clear();

        User foundUser = userService.findUserById(savedUser.getId());

        assertEquals(originalHash, foundUser.getPasswordHash());
    }

    @Transactional
    @Test
    public void createUser_WhenEmailExists_ThrowsException() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("duplicate-user@example.com");
        userRequestDto.setName("testName");
        userRequestDto.setPassword("testPassword");

        userService.createUser(userRequestDto);

        entityManager.flush();

        assertThrows(ConflictException.class, () -> userService.createUser(userRequestDto));
    }

    @Transactional
    @Test
    public void updateUser_WhenEmailExists_ThrowsException() {
        UserRequestDto firstUserDto = new UserRequestDto();
        firstUserDto.setEmail("existing-user@example.com");
        firstUserDto.setName("firstUser");
        firstUserDto.setPassword("testPassword");

        userService.createUser(firstUserDto);

        UserRequestDto secondUserDto = new UserRequestDto();
        secondUserDto.setEmail("updating-user@example.com");
        secondUserDto.setName("secondUser");
        secondUserDto.setPassword("testPassword");

        User savedUser = userService.createUser(secondUserDto);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setEmail("existing-user@example.com");

        entityManager.flush();

        assertThrows(ConflictException.class, () ->
                userService.updateUser(savedUser.getId(), updateUserDto));

        entityManager.clear();

        User foundUser = userService.findUserById(savedUser.getId());

        assertEquals("updating-user@example.com", foundUser.getEmail());
    }

    @Transactional
    @Test
    public void promoteUsersToAdmins_WhenValid_PromotesUser() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("promoting-user@example.com");
        userRequestDto.setName("testName");
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);

        userService.approveUsers(List.of(savedUser.getId()));
        userService.promoteUsersToAdmins(List.of(savedUser.getId()));

        entityManager.flush();
        entityManager.clear();

        User foundUser = userService.findUserById(savedUser.getId());

        assertEquals(UserRole.ADMIN, foundUser.getRole());
        assertEquals(UserStatus.APPROVED, foundUser.getStatus());
    }

    @Transactional
    @Test
    public void promoteUsersToAdmins_WhenSuperAdmin_ThrowsException() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("super-admin-test@example.com");
        userRequestDto.setName("testSuperAdmin");
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);

        userService.approveUsers(List.of(savedUser.getId()));

        savedUser.setRole(UserRole.SUPER_ADMIN);

        entityManager.flush();
        entityManager.clear();

        assertThrows(ConflictException.class, () ->
                userService.promoteUsersToAdmins(List.of(savedUser.getId())));

        User foundUser = userService.findUserById(savedUser.getId());

        assertEquals(UserRole.SUPER_ADMIN, foundUser.getRole());
    }

    @Transactional
    @Test
    public void approveAndRejectUsers_WhenPending_UpdatesStatuses() {
        UserRequestDto firstUserDto = new UserRequestDto();
        firstUserDto.setEmail("approving-user@example.com");
        firstUserDto.setName("firstUser");
        firstUserDto.setPassword("testPassword");

        User approvedUser = userService.createUser(firstUserDto);

        UserRequestDto secondUserDto = new UserRequestDto();
        secondUserDto.setEmail("rejecting-user@example.com");
        secondUserDto.setName("secondUser");
        secondUserDto.setPassword("testPassword");

        User rejectedUser = userService.createUser(secondUserDto);

        userService.approveUsers(List.of(approvedUser.getId()));
        userService.rejectUsers(List.of(rejectedUser.getId()));

        entityManager.flush();
        entityManager.clear();

        User foundApprovedUser = userService.findUserById(approvedUser.getId());
        User foundRejectedUser = userService.findUserById(rejectedUser.getId());

        assertEquals(UserStatus.APPROVED, foundApprovedUser.getStatus());
        assertEquals(UserStatus.REJECTED, foundRejectedUser.getStatus());
    }

    @Transactional
    @Test
    public void deactivateAndReactivateUsers_WhenValid_UpdatesStatus() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("deactivating-customer@example.com");
        userRequestDto.setName("testCustomer");
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);

        userService.approveUsers(List.of(savedUser.getId()));
        userService.deactivateUsers(List.of(savedUser.getId()));

        entityManager.flush();
        entityManager.clear();

        User deactivatedUser = userService.findUserById(savedUser.getId());

        assertEquals(UserRole.CUSTOMER, deactivatedUser.getRole());
        assertEquals(UserStatus.DEACTIVATED, deactivatedUser.getStatus());

        userService.reactivateUsers(List.of(savedUser.getId()));

        entityManager.flush();
        entityManager.clear();

        User reactivatedUser = userService.findUserById(savedUser.getId());

        assertEquals(UserStatus.APPROVED, reactivatedUser.getStatus());
    }

    @Transactional
    @Test
    public void deactivateAndReactivateAdmins_WhenValid_UpdatesStatus() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("deactivating-admin@example.com");
        userRequestDto.setName("testAdmin");
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);

        userService.approveUsers(List.of(savedUser.getId()));
        userService.promoteUsersToAdmins(List.of(savedUser.getId()));
        userService.deactivateAdmins(List.of(savedUser.getId()));

        entityManager.flush();
        entityManager.clear();

        User deactivatedAdmin = userService.findUserById(savedUser.getId());

        assertEquals(UserRole.ADMIN, deactivatedAdmin.getRole());
        assertEquals(UserStatus.DEACTIVATED, deactivatedAdmin.getStatus());

        userService.reactivateAdmins(List.of(savedUser.getId()));

        entityManager.flush();
        entityManager.clear();

        User reactivatedAdmin = userService.findUserById(savedUser.getId());

        assertEquals(UserRole.ADMIN, reactivatedAdmin.getRole());
        assertEquals(UserStatus.APPROVED, reactivatedAdmin.getStatus());
    }
}
