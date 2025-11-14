package com.hamkkebu.boilerplate.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_boilerplate_sample")
public class Sample {

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
