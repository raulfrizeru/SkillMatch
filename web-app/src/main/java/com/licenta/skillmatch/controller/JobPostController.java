package com.licenta.skillmatch.controller;

import com.licenta.skillmatch.entity.JobPost;
import com.licenta.skillmatch.service.JobPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job-posts")
@CrossOrigin(origins="*")
public class JobPostController {
    @Autowired
    private JobPostService jobPostService;

    @GetMapping
    public List<JobPost> getAll() {
        return jobPostService.findAllJobPosts();
    }

    @PostMapping
    public JobPost create(@RequestBody JobPost job) {
        return jobPostService.createJobPost(job);
    }
}
