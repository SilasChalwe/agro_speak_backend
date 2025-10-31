from flask import Flask, request, jsonify, Response, send_from_directory, url_for
from flask_cors import CORS
from flask_wtf import CSRFProtect
from gradio_client import Client, handle_file
import tempfile
import os
import uuid
import logging

# Initialize logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(message)s')

# Initialize Flask app
app = Flask(__name__)
app.config['SECRET_KEY'] = 'f1a9b5e412f9d62cde7421f99f73f50b802ee4e3d29c52b40e2dcd1c81a1f313'

# Folder to store generated audio files
AUDIO_FOLDER = os.path.join(os.getcwd(), "generated_audio")
os.makedirs(AUDIO_FOLDER, exist_ok=True)
app.config['AUDIO_FOLDER'] = AUDIO_FOLDER

# Enable CORS & CSRF
CORS(app)
csrf = CSRFProtect(app)

# Initialize Gradio clients
asr_client = Client("asr-africa/ASR_AFRICAN_LANGUAGES")
translator_client = Client("victorsconcious/Bemba_English_Translator")
tts_client = Client("victorsconcious/bemba_narrator")

# Map source/target to Gradio direction for translation
LANG_DIRECTION_MAP = {
    ("English", "Bemba"): "English → Bemba",
    ("Bemba", "English"): "Bemba → English"
}

@app.route('/')
def home():
    return jsonify({
        "message": "Welcome to AgroSpeak Backend API",
        "endpoints": {
            "transcribe": "POST /api/transcribe",
            "translate": "POST /api/translate",
            "speak": "POST /api/speak"
        }
    })

@app.route('/api/transcribe', methods=['POST'])
@csrf.exempt
def transcribe_audio():
    if 'audio' not in request.files or 'language' not in request.form:
        return jsonify({"error": "Missing audio file or language"}), 400

    audio_file = request.files['audio']
    language = request.form['language']
    logging.info(f"Received audio for transcription, language: {language}")

    with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as temp_audio:
        audio_file.save(temp_audio.name)
        temp_audio_path = temp_audio.name

    try:
        result = asr_client.predict(
            audio=handle_file(temp_audio_path),
            language=language,
            api_name="/transcribe"
        )
        logging.info(f"Transcription result: {result}")
        os.remove(temp_audio_path)
        return jsonify({"language": language, "transcription": result})

    except Exception as e:
        if os.path.exists(temp_audio_path):
            os.remove(temp_audio_path)
        logging.error(f"Transcription error: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/translate", methods=["POST"])
@csrf.exempt
def translate():
    data = request.json
    text = data.get("text")
    source = data.get("sourceLang")
    target = data.get("targetLang")
    logging.info(f"Received translation request: {text} ({source} -> {target})")

    if not text or not source or not target:
        return jsonify({"error": "Fields 'text', 'sourceLang', and 'targetLang' are required"}), 400

    direction = LANG_DIRECTION_MAP.get((source, target))
    if not direction:
        return jsonify({"error": f"Translation from {source} to {target} not supported"}), 400

    try:
        translated = translator_client.predict(direction=direction, text=text, api_name="/translate")
        logging.info(f"Translation result: {translated}")
        return jsonify({
            "originalText": text,
            "sourceLang": source,
            "targetLang": target,
            "translation": translated
        })
    except Exception as e:
        logging.error(f"Translation error: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/speak", methods=["POST"])
@csrf.exempt
def speak():
    data = request.get_json()
    if not data or "text" not in data:
        return jsonify({"error": "Missing 'text' parameter"}), 400

    text_input = data["text"].strip()
    logging.info(f"Received speak request: {text_input}")

    if not text_input:
        return jsonify({"error": "Text cannot be empty"}), 400

    try:
        result = tts_client.predict(text=text_input, api_name="/predict")
        logging.info(f"TTS generation completed, file: {result}")

        file_id = str(uuid.uuid4()) + ".wav"
        save_path = os.path.join(AUDIO_FOLDER, file_id)

        with open(result, 'rb') as src, open(save_path, 'wb') as dst:
            dst.write(src.read())

        file_url = url_for('serve_audio', file_id=file_id, _external=True)
        logging.info(f"Playable audio URL: {file_url}")

        return jsonify({
            "text": text_input,
            "audioUrl": file_url,
            "id": file_id
        })

    except Exception as e:
        logging.error(f"TTS error: {str(e)}")
        return jsonify({"error": f"Audio generation failed: {str(e)}"}), 500

@app.route('/audio/<file_id>')
def serve_audio(file_id):
    return send_from_directory(AUDIO_FOLDER, file_id)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
