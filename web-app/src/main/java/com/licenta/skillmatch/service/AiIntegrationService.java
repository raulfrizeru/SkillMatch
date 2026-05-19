package com.licenta.skillmatch.service;

import com.licenta.skillmatch.entity.Candidate;
import com.licenta.skillmatch.entity.JobApplication;
import com.licenta.skillmatch.entity.JobPost;
import com.licenta.skillmatch.entity.PastInterview;
import com.licenta.skillmatch.repository.CandidateRepository;
import com.licenta.skillmatch.repository.JobApplicationRepository;
import com.licenta.skillmatch.repository.JobPostRepository;
import com.licenta.skillmatch.repository.PastInterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class AiIntegrationService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    PastInterviewRepository pastInterviewRepository;

    @Autowired
    CandidateRepository candidateRepository;

    @Autowired
    JobPostRepository jobPostRepository;

    @Autowired
    JobApplicationRepository jobApplicationRepository;

    public String runAiScoring(Candidate candidate, JobPost job, String selectedLlmModel) {
        try {

            Map<String, Object> pythonPayload = new HashMap<>();

            pythonPayload.put("llm_model", selectedLlmModel);
            pythonPayload.put("target_company", job.getEmployer().getCompanyName());

            //CV optimization
            if (candidate.getExtractedCvJson() != null) {
                pythonPayload.put("cv_json", candidate.getExtractedCvJson());
                pythonPayload.put("action_needed_cv", "SKIP_EXTRACTION");
            } else if (candidate.getExtractedCvOcr() != null) {
                pythonPayload.put("cv_text", candidate.getExtractedCvOcr());
                pythonPayload.put("action_needed_cv", "EXTRACT_JSON_ONLY");
            } else {

                pythonPayload.put("cv_path", candidate.getCvFilePath());
                pythonPayload.put("action_needed_cv", "FULL_PROCESS");
            }

            // Job optimizatiom:
            if (job.getExtractedJobJson() != null) {
                pythonPayload.put("job_json", job.getExtractedJobJson());
                pythonPayload.put("action_needed_job", "SKIP_EXTRACTION");
            } else {
                pythonPayload.put("job_text", job.getDescription());
                pythonPayload.put("action_needed_job", "FULL_PROCESS");
            }

            // add interviews
            List<PastInterview> interviewsDb = pastInterviewRepository.findByCandidateId(candidate.getId());


            List<Map<String, Object>> interviewsForPython = new ArrayList<>();

            for (PastInterview interview : interviewsDb) {
                Map<String, Object> map = new HashMap<>();
                map.put("company", interview.getCompany());
                map.put("job_title", interview.getJobTitle());
                map.put("score", interview.getScore());
                interviewsForPython.add(map);
            }

            pythonPayload.put("interviews", interviewsForPython);


            String jsonPayload = objectMapper.writeValueAsString(pythonPayload);
            String base64Payload = Base64.getEncoder().encodeToString(jsonPayload.getBytes("UTF-8"));

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\raulr\\Desktop\\LICENTA\\SkillMatch\\ai-module\\.venv\\Scripts\\python.exe",
                    "C:\\Users\\raulr\\Desktop\\LICENTA\\SkillMatch\\ai-module\\main.py",
                    base64Payload
            );

            processBuilder.redirectErrorStream(false);
            Process process = processBuilder.start();


            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script failed: " + exitCode + ". Output: " + output.toString());
            }
            // 8. AI module response
            Map<String, Object> pythonResponse = objectMapper.readValue(output.toString(), Map.class);

            // update CV JSON
            if (pythonResponse.containsKey("extracted_cv_json")) {
                String extractedCvJson = objectMapper.writeValueAsString(pythonResponse.get("extracted_cv_json"));
                candidate.setExtractedCvJson(extractedCvJson);
            }

            // update CV ocr
            if (pythonResponse.containsKey("cv_ocr_text")) {
                String cvOcrText = (String) pythonResponse.get("cv_ocr_text");
                candidate.setExtractedCvOcr(cvOcrText);
            }

            if (pythonResponse.containsKey("cv_ocr_text") || pythonResponse.containsKey("extracted_cv_json")) {
                candidateRepository.save(candidate);
            }

            // update Job JSON
            if (pythonResponse.containsKey("extracted_job_json")) {
                String extractedJobJson = objectMapper.writeValueAsString(pythonResponse.get("extracted_job_json"));
                job.setExtractedJobJson(extractedJobJson);
                jobPostRepository.save(job);
            }

            if (pythonResponse.containsKey("scores")) {
                Map<String, Object> scoresMap = (Map<String, Object>) pythonResponse.get("scores");


                Map<String, Object> details = (Map<String, Object>) scoresMap.get("details");


                JobApplication jobApplication = jobApplicationRepository
                        .findByCandidateIdAndJobPostId(candidate.getId(), job.getId());

                if (jobApplication != null) {
                    jobApplication.setFinalScore((Double)(scoresMap.get("final_score")));


                    if (details != null) {
                        jobApplication.setSemanticScore((Double)(details.get("semantic_score")));
                        jobApplication.setSkillsScore((Double)(details.get("skills_score")));
                        jobApplication.setDomainScore((Double)(details.get("domain_score")));
                        jobApplication.setExperienceScore((Double)(details.get("experience_score")));
                        jobApplication.setSoftSkillsScore((Double)(details.get("soft_skills_score")));
                        jobApplication.setInterviewScore((Double)(details.get("interview_score")));
                    }

                    jobApplicationRepository.save(jobApplication);
                }
            }


            return objectMapper.writeValueAsString(pythonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
