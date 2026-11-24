package ru.prodflow.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.prodflow.dto.UpdateUserDto;
import ru.prodflow.dto.request.UserRequestDto;
import ru.prodflow.enums.UserRole;
import ru.prodflow.enums.UserStatus;
import ru.prodflow.exception.BadCredentialsException;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.exception.NotFoundException;
import ru.prodflow.model.User;
import ru.prodflow.repository.UserRepository;
import ru.prodflow.service.interfaces.UserService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(UserRequestDto userRequestDto) {
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует.");
        }
        User user = new User();
        user.setName(userRequestDto.getName());
        user.setEmail(userRequestDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userRequestDto.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.PENDING);

        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateUser(Long id, UpdateUserDto updateUserDto) {
        User user = findUserById(id);

        if (updateUserDto.getName() != null) user.setName(updateUserDto.getName());
        if (updateUserDto.getEmail() != null && !Objects.equals(updateUserDto.getEmail(), user.getEmail())) {
            if (userRepository.existsByEmail(updateUserDto.getEmail())) {
                throw new ConflictException("Пользователь с таким email уже существует.");
            }
            user.setEmail(updateUserDto.getEmail());
        }
    }

    @Transactional
    @Override
    public void promoteUsersToAdmins(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            User user = findUserById(id);
            if (user.getStatus() != UserStatus.APPROVED) {
                throw new ConflictException("Пользователь с id: " + id + " не одобрен.");
            }
            if (user.getRole() == UserRole.ADMIN) continue;

            if (user.getRole() != UserRole.CUSTOMER) {
                throw new ConflictException("Нельзя изменить роль данного пользователя.");
            }

            user.setRole(UserRole.ADMIN);
        }
    }

    @Transactional
    @Override
    public void deactivateUsers(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            User user = findUserById(id);
            if (user.getRole() != UserRole.CUSTOMER) {
                throw new ConflictException("Пользователь с id: " + id + " не заказчик.");
            }
            if (user.getStatus() == UserStatus.DEACTIVATED) continue;
            if (user.getStatus() != UserStatus.APPROVED) {
                throw new ConflictException("Пользователь с id: " + id + " не одобрен.");
            }
            user.setStatus(UserStatus.DEACTIVATED);
        }
    }

    @Transactional
    @Override
    public void reactivateUsers(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            User user = findUserById(id);
            if (user.getRole() != UserRole.CUSTOMER) {
                throw new ConflictException("Пользователь с id: " + id + " не заказчик.");
            }
            if (user.getStatus() == UserStatus.PENDING || user.getStatus() == UserStatus.REJECTED) {
                throw new ConflictException("Пользователь с id: " + id + " не может быть активирован.");
            }
            if (user.getStatus() == UserStatus.APPROVED) continue;
            user.setStatus(UserStatus.APPROVED);
        }
    }

    @Transactional
    @Override
    public void deactivateAdmins(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            User user = findUserById(id);
            if (user.getRole() != UserRole.ADMIN) {
                throw new ConflictException("Пользователь с id: " + id + " не администратор.");
            }
            if (user.getStatus() == UserStatus.DEACTIVATED) continue;
            if (user.getStatus() != UserStatus.APPROVED) {
                throw new ConflictException("Пользователь с id: " + id + " не одобрен.");
            }
            user.setStatus(UserStatus.DEACTIVATED);
        }
    }

    @Transactional
    @Override
    public void reactivateAdmins(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            User user = findUserById(id);
            if (user.getRole() != UserRole.ADMIN) {
                throw new ConflictException("Пользователь с id: " + id + " не администратор.");
            }
            if (user.getStatus() == UserStatus.PENDING || user.getStatus() == UserStatus.REJECTED) {
                throw new ConflictException("Пользователь с id: " + id + " не может быть активирован.");
            }
            if (user.getStatus() == UserStatus.APPROVED) continue;
            user.setStatus(UserStatus.APPROVED);
        }
    }

    @Transactional
    @Override
    public void approveUsers(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            User user = findUserById(id);
            if (user.getStatus() != UserStatus.PENDING) {
                throw new ConflictException("Пользователь с id: " + id + " не может быть подтвержден.");
            }
            user.setStatus(UserStatus.APPROVED);
        }
    }

    @Transactional
    @Override
    public void rejectUsers(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            User user = findUserById(id);
            if (user.getStatus() != UserStatus.PENDING) {
                throw new ConflictException("Пользователь с id: " + id + " не может быть отклонен.");
            }
            user.setStatus(UserStatus.REJECTED);
        }
    }

    @Transactional
    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 8) {
            throw new IllegalArgumentException("Новый пароль должен содержать минимум 8 символов.");
        }
        User user = findUserById(id);
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Неверный пароль.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id: " + id + " не найден."));
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new NotFoundException("Пользователь с email: " + email + " не найден."));
    }

    @Override
    public List<User> findUsersByStatus(UserStatus userStatus) {
        return userRepository.findByStatus(userStatus);
    }

    @Override
    public List<User> findUsersByRole(UserRole userRole) {
        return userRepository.findByRole(userRole);
    }
}
