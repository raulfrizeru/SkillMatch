from docling.document_converter import DocumentConverter
import os
import re
import unicodedata

os.environ["HF_TOKEN"] = "hf_"

def extract_text_from_pdf(file_path):
    file_path= "C:/Users/raulr/Desktop/LICENTA/SkillMatch/web-app/" + file_path
    converter = DocumentConverter()
    result = converter.convert(file_path)
    return result.document.export_to_markdown()

def clean_text_for_llm(text):
    text = unicodedata.normalize('NFKD', text).encode('ascii', 'ignore').decode('utf-8')
    text = re.sub(r'<!--.*?-->', '', text)
    text = re.sub(r'<[^>]+>', '', text)
    text = re.sub(r'\[.*?\]\(.*?\)', '', text)
    text = re.sub(r'!\[.*?\]\(.*?\)', '', text)
    text = re.sub(r'[\r\n\t]+', ' ', text)
    text = re.sub(r'\.+', '.', text)
    text = re.sub(r'##', ' ', text)
    text = re.sub(r'\s{2,}', ' ', text)
    text = text.strip()
    return text

