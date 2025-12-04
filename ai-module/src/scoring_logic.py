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
            if skill:
                text_parts.append(f"{skill} in {domain}")
    if 'soft_skills' in data_json:
        text_parts.extend(data_json['soft_skills'])

    return ". ".join(text_parts)

def calculate_semantic_score(cv_json: dict, job_json: dict) -> float:
    cv_text_dense = generate_dense_text_from_json(cv_json)
    print(cv_text_dense)
    job_text_dense = generate_dense_text_from_json(job_json)
    print(job_text_dense)

    if not cv_text_dense or not job_text_dense:
        return 0.0

    emb_cv = model.encode(cv_text_dense, convert_to_tensor=True)
    print(emb_cv)
    emb_job = model.encode(job_text_dense, convert_to_tensor=True)
    print(emb_job)

    cos_sim = util.cos_sim(emb_cv, emb_job).item()

    return float(max(0.0, min(1.0, cos_sim)))

def calculate_skill_score(cv_json: dict, job_json: dict, threshold: float = 0.3, similar_weight: float = 0.8) -> float:
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

px=calculate_skill_score(CV_JSON, JOB_JSON)
print(px)