from flask import Flask, request, jsonify
from flask_cors import CORS
from flask_wtf import CSRFProtect
from gradio_client import Client, handle_file
import tempfile
import os

# Initialize Flask app
app = Flask(__name__)
app.config['SECRET_KEY'] = 'f1a9b5e412f9d62cde7421f99f73f50b802ee4e3d29c52b40e2dcd1c81a1f313'  # for CSRF protection

# Enable CORS & CSRF
CORS(app)
csrf = CSRFProtect(app)

# Initialize the ASR model client
client = Client("asr-africa/ASR_AFRICAN_LANGUAGES")

@app.route('/')
def home():
    return jsonify({
        "message": "Welcome to African ASR API",
        "usage": "POST /api/transcribe with form-data: {audio: file, language: string}"
    })

@app.route('/api/transcribe', methods=['POST'])
@csrf.exempt  # Exempted if you want easier testing; remove if you’re using CSRF tokens
def transcribe_audio():
    if 'audio' not in request.files or 'language' not in request.form:
        return jsonify({"error": "Missing audio file or language"}), 400

    audio_file = request.files['audio']
    language = request.form['language']

    # Save the uploaded file temporarily
    with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as temp_audio:
        audio_file.save(temp_audio.name)
        temp_audio_path = temp_audio.name

    try:
        # Run prediction using Gradio Client
        result = client.predict(
            audio=handle_file(temp_audio_path),
            language=language,
            api_name="/transcribe"
        )

        # Clean up temp file
        os.remove(temp_audio_path)

        return jsonify({
            "language": language,
            "transcription": result
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True)
