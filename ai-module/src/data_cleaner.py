import re
import unicodedata

def clean_text_for_llm(text):
    text = unicodedata.normalize('NFKD', text).encode('ascii', 'ignore').decode('utf-8')
    text= re.sub(r'[\r\n\t]+', ' ', text)
    text= re.sub(r'\.+', '.', text)
    text = re.sub(r'##', ' ', text)
    text = re.sub(r'\s{2,}', ' ', text)
    text = text.strip()
    return text
