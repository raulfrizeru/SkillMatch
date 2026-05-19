
from sentence_transformers import SentenceTransformer, util
import numpy as np

model = SentenceTransformer("all-MiniLM-L6-v2")

CV_JSON={'experience': [{'domain': 'Software Engineering', 'skill': 'C#', 'years': 2.0}, {'domain': 'Software Engineering', 'skill': 'Python', 'years': 1.5}, {'domain': 'Software Engineering', 'skill': 'Java', 'years': 2.0}, {'domain': 'Embedded', 'skill': 'VHDL', 'years': 0.5}, {'domain': 'Frontend', 'skill': 'HTML', 'years': 1.5}, {'domain': 'Frontend', 'skill': 'CSS', 'years': 1.5}, {'domain': 'Frontend', 'skill': 'JavaScript', 'years': 1.5}, {'domain': 'Backend', 'skill': 'PHP', 'years': 1.0}, {'domain': 'Database', 'skill': 'MySQL', 'years': 2.0}, {'domain': 'Database', 'skill': 'PostgreSQL', 'years': 1.5}, {'domain': 'Version Control', 'skill': 'Git', 'years': 2.0}, {'domain': 'Version Control', 'skill': 'GitHub', 'years': 2.0}, {'domain': 'Software Engineering', 'skill': 'Visual Studio', 'years': 2.0}, {'domain': 'Software Engineering', 'skill': 'IntelliJ IDEA', 'years': 1.5}, {'domain': 'Software Engineering', 'skill': 'PyCharm', 'years': 1.0}, {'domain': 'Testing', 'skill': 'JUnit', 'years': 0.5}, {'domain': 'Project Management', 'skill': 'JIRA', 'years': 0.5}, {'domain': 'Testing', 'skill': 'CANoe', 'years': 0.5}, {'domain': 'Project Management', 'skill': 'DOORS', 'years': 0.5}, {'domain': 'Backend', 'skill': '.NET', 'years': 1.0}, {'domain': 'Frontend', 'skill': 'Blazor', 'years': 1.0}, {'domain': 'Database', 'skill': 'Entity Framework', 'years': 1.0}, {'domain': 'AI/ML', 'skill': 'Google AI', 'years': 0.1}, {'domain': 'Software Engineering', 'skill': 'SOLID', 'years': 1.0}], 'soft_skills': ['teamwork', 'problem solving']}
JOB_JSON={'experience': [{'domain': 'Frontend', 'skill': 'TypeScript', 'years': 1.0}, {'domain': 'Backend', 'skill': 'Node.js', 'years': 1.0}, {'domain': 'Backend', 'skill': 'Python', 'years': 1.0}, {'domain': 'Frontend', 'skill': 'React', 'years': 0.5}, {'domain': 'Database', 'skill': 'SQL', 'years': 0.5}, {'domain': 'Database', 'skill': 'NoSQL', 'years': 0.5}, {'domain': 'Backend', 'skill': 'HTTP', 'years': 0.5}, {'domain': 'AI/ML', 'skill': 'LLM', 'years': 0.5}, {'domain': 'DevOps', 'skill': 'CI', 'years': 0.5}, {'domain': 'Mobile', 'skill': 'iOS', 'years': 0.5}, {'domain': 'Mobile', 'skill': 'Android', 'years': 0.5}], 'soft_skills': ['problem solving', 'communication', 'collaboration', 'reliability', 'adaptability']}

def generate_dense_text_from_json(data_json: dict) -> str:
    text_parts = []

    if 'experience' in data_json:
        for item in data_json['experience']:
            skill = item.get('skill', '')
            domain = item.get('domain', '')
            years = item.get('years', 0)
            if skill and years>0:
                text_parts.append(f"{skill} in {domain}")
    if 'soft_skills' in data_json:
        text_parts.append("Soft skills: " + ", ".join(data_json['soft_skills']))

    return ". ".join(text_parts)

def calculate_semantic_score(cv_json: dict, job_json: dict) -> float:
    cv_text_dense = generate_dense_text_from_json(cv_json)
    job_text_dense = generate_dense_text_from_json(job_json)

    if not cv_text_dense or not job_text_dense:
        return 0.0

    emb_cv = model.encode(cv_text_dense, convert_to_tensor=True)
    emb_job = model.encode(job_text_dense, convert_to_tensor=True)

    cos_sim = util.cos_sim(emb_cv, emb_job).item()

    return float(max(0.0, min(1.0, cos_sim)))

def calculate_skill_score(cv_json: dict, job_json: dict, threshold: float = 0.3, similar_weight: float = 0.9) -> float:
    cv_skills = list(set([item['skill'].lower().strip() for item in cv_json.get('experience', [])]))
    job_skills = list(set([item['skill'].lower().strip() for item in job_json.get('experience', [])]))
    total_required = len(job_skills)

    if total_required == 0:
        return 1.0
    if not cv_skills:
        return 0.0

    total_points = 0.0

    matched_skills = set(job_skills).intersection(set(cv_skills))
    count_direct = len(matched_skills)
    total_points += count_direct * 1.0

    missing_job_skills = list(set(job_skills) - matched_skills)

    if missing_job_skills:
        missing_emb = model.encode(missing_job_skills, convert_to_tensor=True)
        cv_emb = model.encode(cv_skills, convert_to_tensor=True)

        cosine_scores = util.cos_sim(missing_emb, cv_emb)

        for i, missing_skill in enumerate(missing_job_skills):
            best_match_score = float(np.max(cosine_scores[i].cpu().numpy()))
            if best_match_score >= threshold:
                points_awarded = best_match_score * similar_weight
                total_points += points_awarded

    final_score = total_points / total_required
    return float(max(0.0, min(1.0, final_score)))

def get_weighted_domain_vector(data_json: dict):
    if 'experience' not in data_json or not data_json['experience']:
        return None

    total_years = 0.0
    weighted_sum_vector = np.zeros(model.get_sentence_embedding_dimension())

    for item in data_json['experience']:
        domain = item.get('domain', '').strip()
        years = float(item.get('years', 0.0))

        if domain and years > 0:
            domain_vector = model.encode(domain)
            weighted_sum_vector += domain_vector * years
            total_years += years

    if total_years == 0:
        return None

    average_vector = weighted_sum_vector / total_years
    return average_vector

def calculate_domain_score(cv_json: dict, job_json: dict) -> float:
    cv_profile_vec = get_weighted_domain_vector(cv_json)
    job_profile_vec = get_weighted_domain_vector(job_json)

    if cv_profile_vec is None:
        return 0.0
    elif job_profile_vec is None:
        return 1.0

    vCv = np.asarray(cv_profile_vec, dtype=np.float32).reshape(1, -1)
    vJob = np.asarray(job_profile_vec, dtype=np.float32).reshape(1, -1)

    similarity = util.cos_sim(vCv, vJob).item()
    return float(max(0.0, min(1.0, similarity)))

def get_max_years_per_domain(data_json):
    domain_map = {}
    for item in data_json.get('experience', []):
        dom = item.get('domain', '').strip()
        yrs = float(item.get('years', 0))
        if not dom:
            continue
        if dom not in domain_map or yrs > domain_map[dom]:
            domain_map[dom] = yrs
    return domain_map

def calculate_experience_score(cv_json: dict, job_json: dict, threshold: float = 0.7) -> float:
    job_domains_map = get_max_years_per_domain(job_json)
    cv_domains_map = get_max_years_per_domain(cv_json)

    if not job_domains_map:
        return 1.0
    if not cv_domains_map:
        return 0.0

    total_weighted_score = 0.0
    total_weight = 0.0

    for j_domain, j_years in job_domains_map.items():
        weight = max(0.5, j_years)

        best_match_years = 0.0
        max_similarity = 0.0

        j_emb = model.encode(j_domain, convert_to_tensor=True)

        for c_domain, c_years in cv_domains_map.items():
            c_emb = model.encode(c_domain, convert_to_tensor=True)
            sim = util.cos_sim(j_emb, c_emb).item()

            if sim > max_similarity:
                max_similarity = sim
                if sim >= threshold:
                    best_match_years = c_years
                else:
                    best_match_years = 0.0

        if j_years <= 0.1:
            domain_score = 1.0
        else:
            domain_score = min(1.0, best_match_years / j_years)

        total_weighted_score += domain_score * weight
        total_weight += weight

    if total_weight == 0:
        return 0.0
    return float(total_weighted_score / total_weight)

def calculate_soft_skills_score(cv_json: dict, job_json: dict, threshold_high: float = 0.80, threshold_mid: float = 0.4) -> float:
    cv_soft = [s.lower().strip() for s in cv_json.get('soft_skills', []) if s]
    job_soft = [s.lower().strip() for s in job_json.get('soft_skills', []) if s]

    if not job_soft:
        return 1.0
    if not cv_soft:
        return 0.0

    cv_emb = model.encode(cv_soft, convert_to_tensor=True)
    job_emb = model.encode(job_soft, convert_to_tensor=True)

    total_candidate_points = 0.0

    cosine_scores = util.cos_sim(cv_emb, job_emb)

    for i in range(len(cv_soft)):
        best_match_with_job = float(np.max(cosine_scores[i].cpu().numpy()))
        points = 0.0

        if best_match_with_job >= threshold_high:
            points = best_match_with_job * 10.0
        elif best_match_with_job >= threshold_mid:
            points = best_match_with_job * 7.0
        else:
            points = 1.0

        total_candidate_points += points

    max_potential_score = len(job_soft) * 10.0
    final_score = total_candidate_points / max_potential_score
    return float(max(0.0, min(1.0, final_score)))

def calculate_interview_score(interviews_list: list, job_json: dict, target_company: str) -> float:
    if not interviews_list:
        return 0.0

    current_job_domains = list(set([item.get('domain', '').lower() for item in job_json.get('experience', []) if item.get('domain')]))
    if not current_job_domains:
        return 0.0
    job_domain_embs = model.encode(current_job_domains, convert_to_tensor=True)
    best_weighted_score = 0.0
    target_company = target_company.lower().strip()

    for interview in interviews_list:
        raw_score = float(interview.get('score', 0.0))
        interview_company = interview.get('company', '').lower().strip()
        interview_title = interview.get('job_title', '').lower().strip()

        title_emb = model.encode(interview_title, convert_to_tensor=True)
        cosine_scores = util.cos_sim(title_emb, job_domain_embs)[0]
        max_sim = float(np.max(cosine_scores.cpu().numpy()))
        is_same_domain = max_sim >= 0.7
        is_same_company = interview_company == target_company

        relevance_factor = 0.0

        if is_same_company and is_same_domain:
            relevance_factor = 1.0
        elif is_same_company and not is_same_domain:
            relevance_factor = 0.4
        elif not is_same_company and is_same_domain:
            relevance_factor = 0.8
        else:
            relevance_factor = 0.2

        final_score_for_this_interview = raw_score * relevance_factor

        if final_score_for_this_interview > best_weighted_score:
                best_weighted_score = final_score_for_this_interview
    return float(best_weighted_score)

def calculate_final_score(semantic_score: float, skills_score: float,  domain_score: float, experience_score: float, soft_skills_score: float, interview_score: float) -> dict:
    W_SKILLS = 0.25
    W_SEMANTIC = 0.15
    W_DOMAIN = 0.15
    W_EXPERIENCE = 0.35
    W_SOFT = 0.05
    W_INTERVIEW = 0.05

    final_score = (
            (skills_score * W_SKILLS) +
            (semantic_score * W_SEMANTIC) +
            (domain_score * W_DOMAIN) +
            (experience_score * W_EXPERIENCE) +
            (soft_skills_score * W_SOFT) +
            (interview_score * W_INTERVIEW)
    )

    return {
        "final_score": round(final_score, 4),
        "details": {
            "semantic_score": round(semantic_score, 4),
            "skills_score": round(skills_score, 4),
            "experience_score": round(experience_score, 4),
            "domain_score": round(domain_score, 4),
            "soft_skills_score": round(soft_skills_score, 4),
            "interview_score": round(interview_score, 4)
        }
    }