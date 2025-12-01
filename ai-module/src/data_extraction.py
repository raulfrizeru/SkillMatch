from docling.document_converter import DocumentConverter
import os

os.environ["HF_TOKEN"] = "hf_"

def extract_text_from_pdf(file_path):
    converter = DocumentConverter()
    result = converter.convert(file_path)
    return result.document.export_to_markdown()

