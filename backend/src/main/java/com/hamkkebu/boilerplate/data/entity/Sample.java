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
}
