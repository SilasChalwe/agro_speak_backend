# app.py
from flask import Flask, request, send_file, jsonify
from transformers import pipeline
import io

app = Flask(__name__)

# Load the TTS model (facebook/mms-tts-bem)
print("Loading TTS model... This may take a while the first time.")
tts = pipeline("text-to-speech", model="facebook/mms-tts-bem")

@app.route("/tts", methods=["POST"])
def text_to_speech():
    try:
        data = request.json
        text = data.get("text", "")
        if not text:
            return jsonify({"error": "No text provided"}), 400

        # Generate speech
        speech = tts(text)

        # Convert audio bytes to a file-like object
        audio_bytes = io.BytesIO(speech["audio"])
        audio_bytes.seek(0)

        # Return as an audio file
        return send_file(
            audio_bytes,
            mimetype="audio/wav",
            as_attachment=True,
            download_name="output.wav"
        )
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
