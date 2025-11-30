import data_extraction
import data_cleaner
import llm_handler

sourceCV= r"C:\Users\raulr\Desktop\LICENTA\SkillMatch\ai-module\testing_files\CV.pdf"
CV_raw = data_extraction.extract_text_from_pdf(sourceCV)
CV_cleaned_for_llm = data_cleaner.clean_text_for_llm(CV_raw)
CV_cleaned_for_semantic = data_cleaner.clean_text_for_semantic_analysis(CV_cleaned_for_llm)

CV_JSON=llm_handler.extract_structured_data(CV_cleaned_for_llm, is_job=False)

print("Extracted JSON from CV:")
print(CV_JSON)