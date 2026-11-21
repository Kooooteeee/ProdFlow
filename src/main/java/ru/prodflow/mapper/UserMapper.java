package ru.prodflow.mapper;

import ru.prodflow.dto.response.UserResponseDto;
import ru.prodflow.model.User;

public class UserMapper {

    public static UserResponseDto userResponseDto(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();

        userResponseDto.setId(user.getId());
        userResponseDto.setName(user.getName());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setRole(user.getRole());
        userResponseDto.setStatus(user.getStatus());
        userResponseDto.setCreatedAt(user.getCreatedAt());

        return userResponseDto;
    }
}
