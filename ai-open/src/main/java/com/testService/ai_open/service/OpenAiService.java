package com.testService.ai_open.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testService.ai_open.dto.CandidateDto;
import com.testService.ai_open.dto.TestResumeDto;
import com.testService.ai_open.feign.CalibratedTableInterface;
import com.testService.ai_open.model.postgres.IdealResume;
import com.testService.ai_open.model.postgres.JobProfile;
import com.testService.ai_open.model.postgres.TestResume;
import com.testService.ai_open.repository.postgres.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log
@RequiredArgsConstructor
public class OpenAiService {
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final ProfileRepository profileRepository;

    private final CalibratedTableInterface calibratedTableInterface;

    private final String calibrationPrompt =
                    "Review this job description to identify key requirements and responsibilities.\n" +
                    "Evaluate the provided ideal CVs against these requirements to determine how well they align.\n" +
                    "Identify key factors from the ideal CVs that match the job description.\n"+
                    "Based on the analysis of the ideal CVs, list the parameters that are critical for the job.\n" +
                    "Create parameters only on hard skills requires for job according to job description, don't analyse soft skills of the candidates,\n" +
                    "Assign weights to each parameter according to its importance in the job description.\n" +
                    "Create a table with weighted parameters in one column, weight of each parameter(out of 100% in total) in other and " +
                    "normalize the scores of the ideal CVs to 10 in their separate columns.\n" +
                            "for education parameter, correctly analyze if the candidate has required degree/diploma as mentioned in " +
                            "the job description strictly and score the candidate accordingly, give high marks if fulfilled, reduce marks if not.\n" +

                    "For experience parameter, clearly mention the required experience years in the parameter strictly, give marks in range of 1-10, full marks if " +
                            "the years of experience in the relevant field/position/hard skills given in the resume is greater or equal to the required amount " +
                            "of experience according to job description, if a little less than required amount then decrease the marks accordingly" +
                            " also if the frequency of job changes is high or there is gap in the resume reduce few marks./n" +
                    " CLearly mention total marks out of 10 for each CV"+
                    "\n" +
                    "Return only the table in Markdown Table Format and no other comments.";

    final String ratingPrompt =
            "Rank only the New CVs based on the given parameters given in  weighted table strictly\n" +
                    "Check if the given CV is relevant for given Job Profile and score accordingly according to relevance" +
                    "For experience parameter, clearly mention the required experience years in the parameter strictly, give marks in range of 1-10, full marks if " +
                    "the years of experience in the relevant field/position/hard skills given in the resume is greater or equal to the required amount " +
                    "of experience according to table, if a little less than required amount then decrease the marks accordingly" +
                    " also if the frequency of job changes is high or there is gap in the resume reduce the marks.\n" +
                    "for education parameter, correctly analyze if the candidate has required degree/diploma as mentioned in " +
                    "the table strictly and score the candidate accordingly, give high marks if fulfilled, reduce marks if not."+
            "return only a table with ranks of New CVs in one column, names of the files of the New CVs in one column and their " +
            "respective scores(OUT OF 10) in each parameter in separate columns, and a total score in last column and don't show Ideal CVs in the table\n" +
            "table should strictly follow the given format " +
                    "| Parameters | Weight | File Name |\n" +
                    "|---|---|---|\n" +
                    "| Parameter 1 | weightage 1 | score |\n" +
                    "| Parameter 2 |weightage 2 | score |\n" +
                    "| Parameter 3 | weightage 3 | score |\n" +
                    "| Parameter 4 | weightage 4 | score |\n" +
                    "." +
                    "." +
                    "." +
                    "| Total Score | 100% | Score |"+
            "return only table and no other comments";


    @Transactional
    public JobProfile saveProfile(String name, String jobDescription, List<MultipartFile> fileList){
        JobProfile profile = new JobProfile();
        profile.setName(name);
        profile.setJobDescription(jobDescription);
        profile.setWeightedTable(null);

        List<IdealResume> idealResume = fileList.stream().map(multipartFile -> {
            try {
                return getIdealResume(multipartFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        profile.setIdealProfiles(idealResume);


        return profileRepository.save(profile);
    }

    public IdealResume getIdealResume(MultipartFile file) throws IOException {
        String data = extractTextFromPDF(file);
        return new IdealResume(file.getOriginalFilename(), data);
    }

    public TestResume getTestResume(MultipartFile file) throws IOException {
        String data = extractTextFromPDF(file);
        return new TestResume(file.getOriginalFilename(), data);

    }

    @Transactional
    public Optional<JobProfile> getProfile(String name){

        return profileRepository.findById(name);
    }



    public ResponseEntity<String> saveCalibratedTable(String profileName) throws IOException {
        Optional<JobProfile> profile = this.getProfile(profileName);

        if(profile.isPresent()){
            String prompt = profile.get().getJobDescription()+"\n\n"+ calibrationPrompt;
            JobProfile jobProfile = profile.get();
            StringBuilder idealCvs = new StringBuilder();
            for(IdealResume file: jobProfile.getIdealProfiles()){
                idealCvs.append("File Name: "+file.getFilename()+"\n"+"Content: "+file.getTextData()+"\n"+"\n");
            }

            //Getting the table from API callback
            String weightedTab = this.processTextWithOpenAI(prompt,idealCvs.toString());

            //Adding the table to Profile
            jobProfile.setWeightedTable(weightedTab);
            //Updating the profile
            profileRepository.save(jobProfile);

            calibratedTableInterface.saveCalibratedTable(profileName, weightedTab);



            return ResponseEntity.ok(weightedTab);
        }


        return ResponseEntity.notFound().build();

    }

    @Transactional
    public String uploadTestCV(String profileName, List<MultipartFile> fileList){
        Optional<JobProfile> profileOptional = profileRepository.findById(profileName);

        if(profileOptional.isPresent()){
            JobProfile profile = profileOptional.get();

            List<TestResume> testResumeList = fileList.stream().map(multipartFile -> {
                try {
                    return getTestResume(multipartFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());;

            profile.setTestProfiles(testResumeList);

            profileRepository.save(profile);
            return "saved";
        }

        return "not Saved";
    }

    //convert-markdown table to list of list
    public List<List<String>> parseTableResponse(String tableResponse) {
        List<List<String>> table = new ArrayList<>();

        // Split the markdown table into lines
        String[] lines = tableResponse.split("\n");

        // Iterate over the lines starting from the second line to skip the header separator
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Skip the header separator line
            if (line.contains("---")) continue;

            // Remove leading and trailing pipes and split by pipe
            String[] columns = line.substring(1, line.length() - 1).split("\\|");

            // Trim whitespace and add to row
            List<String> row = new ArrayList<>();
            for (String column : columns) {
                row.add(column.trim());
            }

            // Add row to the table
            table.add(row);
        }

        return table;
    }

    //converts individual score tables into mapped values  (to be saves to MongoDB later)
    public String parseAssessedTable(String tableResponse) {
        List<List<String>> table = new ArrayList<>();
        Map<String,Object> response = new HashMap<>();
        Map<String,Integer> score = new HashMap<>();
        // Split the markdown table into lines
        String[] lines = tableResponse.split("\n");

        // Iterate over the lines starting from the second line to skip the header separator
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Skip the header separator line
            if (line.contains("---")) continue;

            // Remove leading and trailing pipes and split by pipe
            String[] columns = line.substring(1, line.length() - 1).split("\\|");
            if(i==0 || i==lines.length-1){
                if(i==0){
                    response.put("name",columns[2].trim());
                }else{
                    response.put("total",columns[2].trim());
                }
                continue;
            }

            // Trim whitespace and add to row
            List<String> row = new ArrayList<>();
            score.put(columns[0].trim()+" ("+columns[1].trim()+")", Integer.parseInt(columns[2].trim()));
//            for (String column : columns) {
//                row.add(column.trim());
//            }

            // Add row to the table
            table.add(row);
        }
        response.put("scores", score);

        return response.toString();
    }


//    public OpenAiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
//        this.restTemplate = restTemplate;
//        this.objectMapper = objectMapper;
//    }

    public String extractTextFromPDF(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String extractedText = pdfStripper.getText(document);

            // Sanitize the extracted text
            String sanitizedText = sanitizeText(extractedText);
            return sanitizedText;
        }
    }

    private String sanitizeText(String text) {
        // Remove null bytes
        text = text.replace("\u0000", "");

        // Ensure the text is valid UTF-8
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    public String processTextWithOpenAI(String prompt,String text) throws IOException {
        String url = "https://api.openai.com/v1/chat/completions";
        // Construct the JSON body
        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("role", "user");
        messageContent.put("content", text);

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content",prompt );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", new Object[]{systemMessage, messageContent});
        requestBody.put("temperature",0);



        // Convert request body to JSON
        String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

        // Log the JSON request body for debugging
        System.out.println("JSON Request Body: " + jsonRequestBody);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonRequestBody, headers);

        // Send request and get response
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            // Parse the response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.path("choices").get(0).path("message").path("content").asText();
        } catch (HttpClientErrorException e) {
            // Log the response body for debugging
            System.out.println("Error Response Body: " + e.getResponseBodyAsString());
            throw new IOException("Error processing file: " + e.getMessage());
        }
    }

    public ResponseEntity<String> rateTestResumes(String profileName) throws IOException {
        Optional<JobProfile> profileOptional= profileRepository.findById(profileName);
        if(profileOptional.isPresent()){
            JobProfile profile = profileOptional.get();
            if(profile.getWeightedTable() != null){
                String prompt = profile.getWeightedTable()+"\n"+ ratingPrompt;

                StringBuilder testCvs = new StringBuilder();
                for(TestResume file: profile.getTestProfiles()){
                    testCvs.append("File Name: "+file.getFileName()+"\n"+"Content: "+file.getTextData()+"\n"+"\n");
                }

                String weightedTab = this.processTextWithOpenAI(prompt,testCvs.toString());

                profile.setScoreTable(weightedTab);

                profileRepository.save(profile);

                return ResponseEntity.ok(weightedTab);

            }else{
                return ResponseEntity.notFound().build();
            }
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    public CandidateDto getCandidateDetails(String profileName, String fileName) throws IOException {

        Optional<JobProfile> profile = this.getProfile(profileName);
        log.info(fileName);
        String resumeData = "";
        for(TestResume file: profile.get().getTestProfiles()){
            log.info(file.getFileName());
            if(file.getFileName().equals(fileName)){
                resumeData = file.getTextData();
                break;
            }
        }

        String extractionPrompt = "Extract the 'name', 'email' and 'contact number' from the " +
                                  "following resume content and return as " +
                                  "a JSON object only and no other comments:\n\n";

            String response = this.processTextWithOpenAI(extractionPrompt,resumeData);

            log.info(response);
            JsonNode jsonObj = objectMapper.readTree(response);
            String name = jsonObj.path("name").asText();
            List<String> emails = new ArrayList<>();
            JsonNode emailNode = jsonObj.path("email");
            if (emailNode.isArray()) {
                for (JsonNode email : emailNode) {
                    emails.add(email.asText());
                }
            } else {
                emails.add(emailNode.asText());
            }


//            String email = jsonObj.path("email").toString();
            String phone = jsonObj.path("contact number").asText();
            log.info(emails.toString());

        return CandidateDto.builder()
                .name(name)
                .email(emails)
                .phone(phone)
                .build();

    }

    public List<JobProfile> listAllProfiles() {
        return profileRepository.findAll();
    }

    public String getQuestions( String profileName, String fileName, String topic) throws IOException {
        Optional<JobProfile> profile = this.getProfile(profileName);

        log.info(fileName);
        String resumeData = "";
        for(TestResume file: profile.get().getTestProfiles()){
            log.info(file.getFileName());
            if(file.getFileName().equals(fileName)){
                resumeData = file.getTextData();
                break;
            }
        }

//        List<List<String>> scores = new ArrayList<>();
//        scores.add(scoreTable.get(0));
//        scores.add(scoreTable.get(index));


        // logic for getting the ambiguous topics
//        List<String> parameters = new ArrayList<>();
//        for(int i = 1; i<scoreTable.get(index).size();i++){
//            List<String> temp = scoreTable.get(index);
//
//            String score = temp.get(i);
//            log.info(score+"");
////            if(a-'0'<=7){
////                parameters.add(scores.get(0).get(i));
////            }
//        }

//        parameters.remove(parameters.size()-1);

        String prompt = "Generate 10 questions from the given topics based on the given CV";
        String text = "topics: "+ topic+"\n"+ resumeData;
        String response = this.processTextWithOpenAI(prompt,text);

        return response;

    }

    public String getHrQuestions(String profileName, int index) throws IOException {
        Optional<JobProfile> profile = this.getProfile(profileName);

        List<List<String>> scoreTable = this.parseTableResponse(profile.get().getScoreTable());
        String fileName = scoreTable.get(index).get(1);
        log.info(fileName);
        String resumeData = "";
        for(TestResume file: profile.get().getTestProfiles()){
            log.info(file.getFileName());
            if(file.getFileName().equals(fileName)){
                resumeData = file.getTextData();
                break;
            }
        }

        String prompt = "Generate basic HR questions for the given CV. Ask questions on : " +
                        "Basic Information [about oneself]" +
                        "(School and College life related questions) Why did they choose the mentioned stream in college" +
                        "Ask them to explain the timelines mentioned in the CV" +
                        "Work experience based questions (why leaving/left previous work?, Explain the works you did)" +
                        "Ask them what are they looking for (company type & roles) and their expectations, targets and why these expectations and targets" +
                        "Ask them workplace related situation based questions like, what would you do if _____? " +
                        "Availability related questions";

        String response = this.processTextWithOpenAI(prompt, resumeData);

        return response;

    }


    public ResponseEntity<List<TestResumeDto>> getTestResumeFromProfile(String profileName) {
        List<TestResumeDto> responseList = new ArrayList<>();
        Optional<JobProfile> profileOptional = profileRepository.findById(profileName);
        JobProfile profile = profileOptional.get();
        List<TestResume> testResumeList = profile.getTestProfiles();
        for(TestResume file : testResumeList){
            responseList.add(TestResumeDto.builder().fileName(file.getFileName()).data(file.getTextData()).build());
        }

        return ResponseEntity.ok(responseList);
    }

    public ResponseEntity<String> getWeightedTableFromProfile(String profileName) {
        Optional<JobProfile> profileOptional = profileRepository.findById(profileName);
        JobProfile profile = profileOptional.get();
        calibratedTableInterface.saveCalibratedTable(profileName, profile.getWeightedTable());
        return ResponseEntity.ok(profile.getWeightedTable());
    }

    public ResponseEntity<String> getResults(String fileName, String data, String table, String profileName) throws IOException {
        String prompt = "Job Profile: "+profileName+"\n"+table+"\n"+ ratingPrompt;
        String text = "File Name: "+fileName+"\n"+"Content: "+data;
        String response = this.processTextWithOpenAI(prompt,text);
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<String> saveUpdatedTable(String profileName, String updatedTable) {
        Optional<JobProfile> jobProfileOptional = this.getProfile(profileName);
        JobProfile jobProfile = jobProfileOptional.get();
        jobProfile.setWeightedTable(updatedTable);
        profileRepository.save(jobProfile);
        return ResponseEntity.ok(updatedTable);
    }
}
