//package com.testService.ai_open.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.util.List;
//
//@Entity
//@Table(name="score_table")
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class ScoreTable {
//
//    @Id
//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "table_id", referencedColumnName = "name")
//    JobProfile jobProfile;
//
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    List<Candidates> tableData;
//
//}
