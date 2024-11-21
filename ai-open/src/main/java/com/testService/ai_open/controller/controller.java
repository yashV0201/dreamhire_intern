package com.testService.ai_open.controller;

import com.testService.ai_open.dto.CandidateDto;
import com.testService.ai_open.dto.RequestData;
import com.testService.ai_open.dto.RequestDto;
import com.testService.ai_open.dto.TestResumeDto;
import com.testService.ai_open.model.mongodb.CandidateScores;
import com.testService.ai_open.model.postgres.IdealResume;
import com.testService.ai_open.model.postgres.JobProfile;

import com.testService.ai_open.service.CandidateScoreService;
import com.testService.ai_open.service.OpenAiService;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Log
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class controller {

    private OpenAiService openAiService;

    private CandidateScoreService candidateScoreService;

//    private ModelRepository modelRepository;

    private Map<String, List<String>> entries = new HashMap<>();

    @Autowired
    public controller(OpenAiService openAiService, CandidateScoreService candidateScoreService) {
        this.openAiService = openAiService;
        this.candidateScoreService = candidateScoreService;
//        this.modelRepository =modelRepository;
    }

    @PostMapping("response")
    public ResponseEntity<String> getResults(@RequestBody RequestData requestData) throws IOException {
        return openAiService.getResults(requestData.getFileName(), requestData.getData(), requestData.getTable(), requestData.getProfileName());
    }


    @PostMapping("/createProfile")
    @ResponseStatus(HttpStatus.CREATED)
    public String createProfile(@RequestParam("jobDescription") MultipartFile jobDescription, @RequestPart("name") String name, @RequestParam("file") List<MultipartFile> fileList) throws IOException {
        String jobDesc = openAiService.extractTextFromPDF(jobDescription);
        openAiService.saveProfile(name,jobDesc, fileList);
        return "profile "+name+" created successfully" ;
    }

    @PostMapping("api/getresponse")
    public String getResponseFromOpenAi(@RequestBody RequestDto requestDto) throws IOException {
        return openAiService.processTextWithOpenAI(requestDto.getPrompt(), requestDto.getText());
    }


    @GetMapping("/index")
    public ResponseEntity<List<JobProfile>> listAllProfiles(){
        List<JobProfile> profiles = openAiService.listAllProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{profileName}")
    public ResponseEntity<JobProfile> getProfile(@PathVariable String profileName){
        return ResponseEntity.ok(openAiService.getProfile(profileName).get());
    }

    @GetMapping("/download/{profileName}/{fileName}")
    public ResponseEntity<String> downloadFile(@PathVariable String profileName, @PathVariable String fileName) throws UnsupportedEncodingException {
        String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        Optional<JobProfile> profile = openAiService.getProfile(profileName);
        if(profile.isPresent()){
            JobProfile jobProfile = profile.get();
            for(IdealResume file: jobProfile.getIdealProfiles()){
                if(file.getFilename().equals(decodedFileName)){
                    return ResponseEntity.ok(file.getFilename()+"\n"+file.getTextData());
//                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()))
//                            .contentType(MediaType.APPLICATION_PDF)
//                            .body(file.getTextData());
                }
            }
        }return ResponseEntity.notFound().build();
    }



    @PostMapping("/{profileName}/save")   //saves calibrated table in the DB
    public ResponseEntity<String> saveCalibratedTable(@PathVariable String profileName) throws IOException {
        String decodedProfileName = URLDecoder.decode(profileName, StandardCharsets.UTF_8);
        return openAiService.saveCalibratedTable(decodedProfileName);
    }

    @PostMapping("/{profileName}/upload-test-cv")  //upload test CVs
    public ResponseEntity<String> startRating(@PathVariable String profileName, @RequestParam("file") List<MultipartFile> fileList){
        String decodedProfileName = URLDecoder.decode(profileName, StandardCharsets.UTF_8);
        openAiService.uploadTestCV(decodedProfileName, fileList);

        return ResponseEntity.ok("saved test resumes");
    }

    @PostMapping("/{profileName}/start")   //Start the assessment of the CVs.
    public RedirectView rateTestResumes(@PathVariable("profileName") String profileName) throws IOException {
        String decodedProfileName = URLDecoder.decode(profileName,StandardCharsets.UTF_8);
        ResponseEntity<String> responseTable = openAiService.rateTestResumes(decodedProfileName);
        return new RedirectView("http://localhost:8080/"+profileName+"/display-table");
    }


//    @GetMapping("/{profileName}/display-table")
//    public ResponseEntity<String> displayTable(@PathVariable("profileName") String profileName){
//        Optional<JobProfile> profileOptional = openAiService.getProfile(profileName);
//        if(profileOptional.isPresent()){
//            JobProfile profile = profileOptional.get();
//            String table = profile.getScoreTable();
//            List<List<String>> tableList = openAiService.parseTableResponse(table);
//
//            log.info(tableList.toString());
//
//            return ResponseEntity.ok(table);
//        }
//        else return ResponseEntity.notFound().build();
//    }

    @GetMapping("/{profileName}/getDetails/{fileName}")
    public CandidateDto getCandidateDetails(@PathVariable String profileName, @PathVariable String fileName) throws IOException {
        Optional<JobProfile> profileOptional = openAiService.getProfile(profileName);
        if(profileOptional.isPresent()){
            JobProfile profile = profileOptional.get();
            CandidateDto candidate = openAiService.getCandidateDetails(profileName, fileName);
            return candidate;

        }
        else return new CandidateDto();
    }

    @GetMapping("/{profileName}/display-table/{fileName}/questions")
    public ResponseEntity<String> getQuestionsOnTopics(@PathVariable String profileName, @PathVariable String fileName,@RequestParam String topic) throws IOException {
        return ResponseEntity.ok(openAiService.getQuestions(profileName,fileName,topic));

    }

    @GetMapping("/{profileName}/display-table/{index}/questions-hr")
    public ResponseEntity<String> getHrQuestions(@PathVariable String profileName, @PathVariable int index) throws IOException {
        return ResponseEntity.ok(openAiService.getHrQuestions(profileName,index));
    }

    @PostMapping("/{profileName}/start-assessment")
    public  ResponseEntity<List<CandidateScores>> assessCandidates(@PathVariable String profileName) throws IOException {
        return candidateScoreService.assessCandidates(profileName);
    }

    @GetMapping("/{profileName}/getresume")
    public ResponseEntity<List<TestResumeDto>> getTestResumesFromProfile(@PathVariable String profileName){
        return openAiService.getTestResumeFromProfile(profileName);
    }

    @GetMapping("/{profileName}/getCalibratedTable")
    public ResponseEntity<String> getWeightedTable(@PathVariable String profileName){
        return openAiService.getWeightedTableFromProfile(profileName);
    }

    @PostMapping("/{profileName}/save-updated-table")
    public ResponseEntity<String> saveUpdatedTable(@PathVariable String profileName, @RequestParam String updatedTable){
        return openAiService.saveUpdatedTable(profileName, updatedTable);
    }

}
