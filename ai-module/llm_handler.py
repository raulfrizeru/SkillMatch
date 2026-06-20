from typing import List
from pydantic import BaseModel, Field, ValidationError
import json
from ollama import Client, ResponseError, generate
import os
from dotenv import load_dotenv

class ExperienceItem(BaseModel):
    skill: str = Field(..., description="The exact name of the skill, framework or tool.")
    years: float = Field(..., description="Estimated number of years of experience. If not specified, infer a value (minimum 0.5) based on context.")
    domain: str = Field(..., description="Main technical domain (e.g. 'Backend', 'Frontend', 'Database', 'DevOps').")
    model_config = {"extra": "ignore"}

class ExtractedData(BaseModel):
    experience: List[ExperienceItem] = Field(..., description="List of extracted skills with years of experience and domain associated.")
    soft_skills: List[str] = Field(..., description="List of deduced soft skills (e.g. 'communication', 'problem solving').")
    model_config = {"extra": "ignore"}

load_dotenv()
SCHEMA_JSON = ExtractedData.model_json_schema()

OLLAMA_API_KEY = os.getenv("API_KEY")
CLIENT = Client(host="https://ollama.com", headers={'Authorization': 'Bearer '+OLLAMA_API_KEY}, timeout=180)

def generate_prompt(text: str, is_job: bool) -> str:

    if is_job:
        context = "job description"
        verb = "required"
    else:
        context = "candidate CV"
        verb = "mentioned"


    prompt_template = f"""
You are a recruitment expert who analyzes the {context}. Your mission is to extract technical skills, domains, years of experience, and soft skills {verb} in the text below. You MUST return ONLY a JSON object strictly following the structure provided at the end.

--------------------
INSTRUCTIONS (MANDATORY)
--------------------

### 1. Skill Extraction ("skill")
- Extract ONLY the core name of each skill, framework, technology or tool.
- KEEP SKILLS SHORT, ATOMIC, AND CONCRETE.
- DO NOT output descriptions, sentences, or long explanations.
- If a phrase contains multiple skills, split them into separate items.
  Examples:
    - "Learning the .NET framework (Blazor)" → ".NET" AND "Blazor"
    - "Java WebStart and Java SE" → "Java"
    - "Python Programming Language and Google AI Essentials Course" → "Python" AND "Google AI"
    - ".NET framework(Blazor), Entity Framework" → ".NET", "Blazor", "Entity Framework"

### 2. Domain Inference ("domain")
- The domain must be SHORT and GENERIC (ONE OR TWO WORDS).
- DO NOT include explanations or full sentences.
- Examples of VALID domains:
    "Frontend", "Backend", "Full-Stack", "Database", "DevOps",
    "Testing", "Version Control", "AI/ML", "Cloud", "Embedded",
    "Project Management", "System Programming", "Software Engineering"

### 3. Years of Experience ("years")
- If explicit years aren't provided, infer a REASONABLE estimate based on context.
- The MINIMUM value is 0.1 years.

### 4. Avoid Repetition
- Do NOT repeat the same skill more than once.
- Keep only one entry per skill.

### 5. Soft Skills ("soft_skills")
- Extract ONLY soft skills (e.g. "communication", "teamwork", "problem solving").
- Correct grammar when needed.

### 6. Output Format (STRICT)
You MUST output a JSON object following exactly this structure:

{{
  "experience": [
    {{
      "skill": "string",
      "years": 1.0,
      "domain": "string"
    }}
  ],
  "soft_skills": ["string"]
}}

Every item in "experience" MUST contain:
- exactly ONE skill
- its inferred years
- its inferred domain

NO explanations, NO extra text. ONLY the JSON object.

--------------------
TEXT TO ANALYZE:
---
{text}
---

RETURN ONLY THE JSON OBJECT(NO markdown, NO code blocks, NO extra text. Output must be valid JSON parseable by json.loads().
).
"""

    return prompt_template.strip()

def extract_structured_data(text_cleaned: str, is_job: bool, model_name: str="llama3.2:1b") -> dict:

    prompt = generate_prompt(text_cleaned, is_job)
    empty_data = ExtractedData(experience=[], soft_skills=[]).model_dump()

    try:
        if "cloud" in model_name.lower():
            messages = [
                {
                    'role': 'user',
                    'content': prompt,
                },
            ]
            response_obj=CLIENT.chat(model=model_name, messages=messages, stream=False, format=ExtractedData.model_json_schema())
            response = response_obj['message']['content']
        else:
            response_obj = generate(model=model_name, prompt=prompt, stream=False, format=ExtractedData.model_json_schema())
            response = response_obj.response


        data_dict = json.loads(response)
        validated = ExtractedData.model_validate(data_dict).model_dump()
        return validated

    except ResponseError as e:
        print(f"Ollama API Error: {e}. Check if Ollama is running.")
        return empty_data
    except json.JSONDecodeError as e:
        print(f"JSON Decode Error: {e}")
        return empty_data
    except ValidationError as ve:
        print(f"Validation Error: {ve}. Check if the model followed the schema.")
        return empty_data
    except Exception as e:
        print(f"Unexpected error in LLM handler: {e}")
        return empty_data

