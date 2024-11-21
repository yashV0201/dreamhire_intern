//package com.testService.ai_open.model;
//
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.hibernate.annotations.CollectionIdJdbcTypeCode;
//import org.yaml.snakeyaml.tokens.ScalarToken;
//
//@Entity
//@Table(name="candidates")
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class Candidates {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String name;
//
//    @OneToOne
//    @JoinColumn(referencedColumnName = "file_name")
//    private TestResume resume;
//
//    @ManyToOne
//    @JoinColumn(referencedColumnName = "name", name = "profile")
//    JobProfile jobProfile;
//
//}
