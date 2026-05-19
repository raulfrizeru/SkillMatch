import json
import sys
import base64
import data_extraction
import llm_handler
import scoring_logic


def main(payload: dict):
    selected_model = payload.get("llm_model", "llama3.2:1b")

    addCvJson = False
    addJobJson = False
    addCvOcr = False

    Cv_raw = None
    CV_JSON = None
    # CV processing
    if payload.get("action_needed_cv") == "SKIP_EXTRACTION":
        CV_JSON = payload.get("cv_json")
        if isinstance(CV_JSON, str):
            CV_JSON = json.loads(CV_JSON)
    elif payload.get("action_needed_cv") == "EXTRACT_JSON_ONLY":
        Cv_raw = payload.get("cv_text")
        Cv_cleaned_for_llm = data_extraction.clean_text_for_llm(Cv_raw)
        CV_JSON = llm_handler.extract_structured_data(Cv_cleaned_for_llm, is_job=False, model_name=selected_model)
        addCvJson = True
    else:
        CvSource = payload.get("cv_path")
        Cv_raw = data_extraction.extract_text_from_pdf(CvSource)
        Cv_cleaned_for_llm = data_extraction.clean_text_for_llm(Cv_raw)
        CV_JSON = llm_handler.extract_structured_data(Cv_cleaned_for_llm, is_job=False, model_name=selected_model)
        addCvJson = True
        addCvOcr = True

    JOB_JSON = None
    # JOB DESCRIPTION processing
    if payload.get("action_needed_job") == "SKIP_EXTRACTION":
        JOB_JSON = payload.get("job_json")
        if isinstance(JOB_JSON, str):
            JOB_JSON = json.loads(JOB_JSON)
    else:
        Job_raw = payload.get("job_text")
        Job_cleaned_for_llm = data_extraction.clean_text_for_llm(Job_raw)
        JOB_JSON = llm_handler.extract_structured_data(Job_cleaned_for_llm, is_job=True, model_name=selected_model)
        addJobJson = True

    target_company = payload.get("target_company")
    past_interviews = payload.get("interviews", [])

    SEMANTIC_SCORE = scoring_logic.calculate_semantic_score(CV_JSON, JOB_JSON)
    SKILLS_SCORE = scoring_logic.calculate_skill_score(CV_JSON, JOB_JSON)
    DOMAIN_SCORE = scoring_logic.calculate_domain_score(CV_JSON, JOB_JSON)
    EXPERIENCE_SCORE = scoring_logic.calculate_experience_score(CV_JSON, JOB_JSON)
    SOFT_SKILLS_SCORE = scoring_logic.calculate_soft_skills_score(CV_JSON, JOB_JSON)
    INTERVIEW_SCORE = scoring_logic.calculate_interview_score(past_interviews, JOB_JSON, target_company)

    FINAL_SCORE = scoring_logic.calculate_final_score(SEMANTIC_SCORE, SKILLS_SCORE, DOMAIN_SCORE, EXPERIENCE_SCORE,
                                                      SOFT_SKILLS_SCORE, INTERVIEW_SCORE)

    result = {
        "scores": FINAL_SCORE
    }
    if addCvJson:
        result["extracted_cv_json"] = CV_JSON
    if addJobJson:
        result["extracted_job_json"] = JOB_JSON
    if addCvOcr:
        result["cv_ocr_text"] = Cv_raw

    print(json.dumps(result))


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No payload provided"}))
        sys.exit(1)
    try:
        encoded_payload = sys.argv[1]
        decoded_bytes = base64.b64decode(encoded_payload)
        payload_str = decoded_bytes.decode('utf-8')
        payload_dict = json.loads(payload_str)

        main(payload_dict)
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)
