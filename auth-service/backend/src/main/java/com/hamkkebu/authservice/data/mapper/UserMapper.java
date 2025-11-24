package com.hamkkebu.authservice.data.mapper;

import com.hamkkebu.authservice.data.dto.UserRequest;
import com.hamkkebu.authservice.data.dto.UserResponse;
import com.hamkkebu.authservice.data.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * User 엔티티와 DTO 간의 매핑을 처리하는 Mapper
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * User 엔티티를 UserResponse DTO로 변환
     *
     * @param user User 엔티티
     * @return UserResponse DTO
     */
    UserResponse toDto(User user);

    /**
     * UserRequest DTO를 User 엔티티로 변환
     *
     * @param request UserRequest DTO
     * @return User 엔티티
     */
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isVerified", constant = "false")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(UserRequest request);

    /**
     * UserRequest DTO로 기존 User 엔티티 업데이트
     *
     * @param request UserRequest DTO
     * @param user 업데이트할 User 엔티티
     */
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UserRequest request, @MappingTarget User user);
}
