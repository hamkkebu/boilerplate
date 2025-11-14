package com.hamkkebu.boilerplate.data.entity;

import com.hamkkebu.boilerplate.common.entity.BaseEntity;
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

    @Column(name = "sample_fname")
    private String sampleFname;

    @Column(name = "sample_lname")
    private String sampleLname;

    @Column(name = "sample_nickname")
    private String sampleNickname;

    @Column(name = "sample_email")
    private String sampleEmail;

    @Column(name = "sample_phone")
    private String samplePhone;

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
}
