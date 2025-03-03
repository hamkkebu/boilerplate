package com.howwasit.taste.sampleservice.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_sample_taste")
public class Sample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taste_sample_num", nullable = false)
    private Long sampleNum;

    @Column(name = "taste_sample_id", nullable = false)
    private String sampleId;

    @Column(name = "taste_sample_fname")
    private String sampleFname;

    @Column(name = "taste_sample_lname")
    private String sampleLname;
}
