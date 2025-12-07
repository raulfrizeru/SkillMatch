import data_extraction
import llm_handler
import scoring_logic

########real##########real#############real##########real#########real##########real############real

##data
interviews_list=[{'company': 'AUMOVIO', 'job_title': 'Software Intern Engineer', 'score': 0.74}]
target_company='Storyteller'
CvSource = r"C:\Users\raulr\Desktop\LICENTA\SkillMatch\ai-module\testing_files\CV.pdf"
Job_raw = f""""Job Title: Junior Web Developer
About job
🇷🇴 Up to RON 120,000 salary on a full time, permanent employment contract
🌎 Fully remote working anywhere in Romania!
🏖️ 33 Days Paid Leave and Benefits
✨ Exciting high growth product, relied on by leading global sports brands
💻 Working with the latest hardware, tech stack and tools


ABOUT US



Storyteller is a high growth B2B SaaS platform, which allows companies to integrate Stories into their owned and operated platforms. Popularized by Instagram and Snapchat, Stories are perfectly suited for boosting user engagement, audience retention, and driving advertising revenue.


Our end‑to‑end platform gives companies a best‑in‑class Stories experience in days with native iOS, Android, and Web SDKs, publishing tools, analytics, and ad support.


We work with many globally recognised clients, particularly within sport, so if you're a sporting fan this could be a great fit!


RESPONSIBILITIES



What can you expect?

As a Junior/Graduate Software Engineer, you’ll join a supportive, product‑minded team that values collaboration and clear thinking.


You’ll work remotely, learn and develop rapidly and help build and enhance our Web SDK that powers Stories used by globally recognised clients, especially in sport. If you’re a sports fan, you’ll enjoy seeing your work live with major organisations.


This role will provide you with the opportunity to:

Ship features end‑to‑end: design, build and test small, well‑scoped improvements to our products
Make things reliable and fast: fix bugs, add tests, and instrument code so we can measure performance and stability
Solve real customer problems: collaborate with Product, Design and Delivery to scope simple, pragmatic solutions that work for our clients
Improve developer experience: contribute to examples, docs and small tools that make integration with our SDKs smooth and enjoyable
Think from first principles: break problems down, reason about trade‑offs, and pick the simplest approach that works
Learn in the open: participate in code reviews (giving and receiving feedback), pair with teammates, and share what you’re learning
Own your work: plan your tasks, communicate progress, and follow through to high‑quality releases
Contribute to how we build: help refine our CI, test suites and local dev scripts as we scale


QUALIFICATIONS



What's important to us:

Some experience with TypeScript, Node.js or Python – you’ll use these in our take-home task and day-to-day work. Exposure to React, basic APIs/HTTP or simple databases (SQL/NoSQL) is also valuable
A genuine excitement for AI and AI‑assisted coding — this is a must‑have. You’re curious about LLMs and actively use (or are eager to use) code assistants and AI APIs to move faster, while verifying outputs with tests and sound reasoning
Clear problem‑solving: you can break a task down, reason from first principles, and explain trade‑offs simply
Reliability and follow‑through: you meet agreed timelines, communicate early if blocked
Openness to feedback: you’re curious, and iterate based on what you learn
Strong written and verbal communication; you can collaborate well in a remote setting
Reliable, consistent internet access for remote work


What’s nice to have:

Side projects, a small repo, open‑source contributions, or a write‑up showing how you learn
Familiarity with web SDK’s (TypeScript, React or Node.js) is a strong plus – we also ship iOS and Android SDKs
Interest in sports, media, or content tech
Backgrounds we’ve seen succeed include maths, physics, bootcamps, and self‑taught routes - a CS degree is not required


RECRUITMENT PROCESS



Firstly - Hiring Manager conversation (20–30 mins)
A short call to get to know you, share more about Storyteller, and hear what excites you about AI and AI‑assisted coding.


Secondly - 60‑minute, time‑boxed take‑home (Python or Node/TS)
A tiny, real‑world task: fix a couple of failing tests and add a small feature. You may use AI assistants - please include a brief note on how you used them and how you verified outputs (tests, reasoning). Submit even if you don’t finish; clarity and judgement matter.


Finally - Review, Pair Programming & Questions (60–75 mins)
We’ll review your submission together, pair on a small improvement or bug, and explore a simple first‑principles problem. Open docs are welcome. Bring your questions - we want you to leave with a clear picture of the work and the team.


And that's it!"""

#1. Extract text from CV PDF
Cv_raw=data_extraction.extract_text_from_pdf(CvSource)

#2. Clean CV and Job_description for LLM
Cv_cleaned_for_llm=data_extraction.clean_text_for_llm(Cv_raw)
Job_cleaned_for_llm=data_extraction.clean_text_for_llm(Job_raw)

#3. Extract structured data from CV and Job_description
CV_JSON=llm_handler.extract_structured_data(Cv_cleaned_for_llm, is_job=False)
JOB_JSON=llm_handler.extract_structured_data(Job_cleaned_for_llm, is_job=True)

#4. Semantic Analysis
SEMANTIC_SCORE=scoring_logic.calculate_semantic_score(CV_JSON, JOB_JSON)

#5. Skills Analysis
SKILLS_SCORE=scoring_logic.calculate_skill_score(CV_JSON, JOB_JSON)

#6. Domain Analysis
DOMAIN_SCORE=scoring_logic.calculate_domain_score(CV_JSON, JOB_JSON)

#7. Experience Analysis
EXPERIENCE_SCORE=scoring_logic.calculate_experience_score(CV_JSON, JOB_JSON)

#8. Soft Skills Analysis
SOFT_SKILLS_SCORE=scoring_logic.calculate_soft_skills_score(CV_JSON, JOB_JSON)

#9. Interview Analysis
INTERVIEW_SCORE=scoring_logic.calculate_interview_score(interviews_list, JOB_JSON, target_company)

#10. Final Score calculation
FINAL_SCORE=scoring_logic.calculate_final_score(SEMANTIC_SCORE, SKILLS_SCORE, DOMAIN_SCORE, EXPERIENCE_SCORE, SOFT_SKILLS_SCORE, INTERVIEW_SCORE)

####################################################################################################

print(FINAL_SCORE)

