from fastapi import FastAPI
from pydantic import BaseModel
import json

import data_extraction
import llm_handler
import scoring_logic


app = FastAPI()


# =========================
# MODELE DE INPUT
# =========================

class InterviewItem(BaseModel):
    company: str
    job_title: str
    score: float


class ScoreRequest(BaseModel):
    cv_path: str
    job_description: str
    interviews: list[InterviewItem]


# =========================
# ENDPOINT
# =========================

@app.post("/score")
def calculate_score(request: ScoreRequest):

    target_company = request.job_description.split('\n', 1)[0]

    # 1. Extract text
    Cv_raw = data_extraction.extract_text_from_pdf(request.cv_path)

    # 2. Clean text
    Cv_cleaned = data_extraction.clean_text_for_llm(Cv_raw)
    Job_cleaned = data_extraction.clean_text_for_llm(request.job_description)

    # 3. Structured data
    CV_JSON = llm_handler.extract_structured_data(Cv_cleaned, is_job=False)
    JOB_JSON = llm_handler.extract_structured_data(Job_cleaned, is_job=True)

    # 4. Scores
    semantic = scoring_logic.calculate_semantic_score(CV_JSON, JOB_JSON)
    skills = scoring_logic.calculate_skill_score(CV_JSON, JOB_JSON)
    domain = scoring_logic.calculate_domain_score(CV_JSON, JOB_JSON)
    experience = scoring_logic.calculate_experience_score(CV_JSON, JOB_JSON)
    soft = scoring_logic.calculate_soft_skills_score(CV_JSON, JOB_JSON)
    interview = scoring_logic.calculate_interview_score(
        [i.dict() for i in request.interviews],
        JOB_JSON,
        target_company
    )

    final = scoring_logic.calculate_final_score(
        semantic,
        skills,
        domain,
        experience,
        soft,
        interview
    )

    return final