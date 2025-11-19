package com.hamkkebu.boilerplate.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hamkkebu.boilerplate.common.entity.BaseEntity;
import com.hamkkebu.boilerplate.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;

/**
 * Sample 엔티티
 *
 * <p>사용자(회원) 정보를 저장하는 엔티티입니다.</p>
 * <p>BaseEntity를 상속받아 생성/수정 일시 및 Soft Delete 기능을 지원합니다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_boilerplate_sample")
public class Sample extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_num", nullable = false)
    private Long sampleNum;

    @Column(name = "sample_id", nullable = false)
    private String sampleId;

    @Column(name = "sample_first_name")
    private String sampleFirstName;

    @Column(name = "sample_last_name")
    private String sampleLastName;

    @Column(name = "sample_nickname")
    private String sampleNickname;

    @Column(name = "sample_email")
    private String sampleEmail;

    @Column(name = "sample_phone")
    private String samplePhone;

    @JsonIgnore
    @Column(name = "sample_password", nullable = false)
    private String samplePassword;

    @Column(name = "sample_country")
    private String sampleCountry;

    @Column(name = "sample_city")
    private String sampleCity;

    @Column(name = "sample_state")
    private String sampleState;

    @Column(name = "sample_street1")
    private String sampleStreet1;

    @Column(name = "sample_street2")
    private String sampleStreet2;

    @Column(name = "sample_zip")
    private String sampleZip;

    /**
     * 사용자 권한
     * RBAC (Role-Based Access Control) 구현
     *
     * <p>기본값: USER</p>
     * <p>권한 종류: USER, ADMIN, DEVELOPER</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sample_role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    /**
     * 비밀번호 업데이트
     *
     * <p>암호화된 비밀번호로 업데이트합니다.</p>
     *
     * @param encodedPassword 암호화된 비밀번호
     */
    public void updatePassword(String encodedPassword) {
        this.samplePassword = encodedPassword;
    }
}
