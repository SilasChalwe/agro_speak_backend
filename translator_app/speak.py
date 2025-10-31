from flask import Flask, request, jsonify, Response
from flask_wtf import CSRFProtect
from gradio_client import Client
import os
import uuid

# ---------------------------
# Flask app setup
# ---------------------------
app = Flask(__name__)
app.config['SECRET_KEY'] = os.urandom(32)
csrf = CSRFProtect(app)

# ---------------------------
# Gradio client
# ---------------------------
client = Client("victorsconcious/bemba_narrator")

# ---------------------------
# API endpoint to generate audio
# ---------------------------
@app.route("/predict", methods=["POST"])
@csrf.exempt
def predict():
    data = request.get_json()
    
    # Validate input
    if not data:
        return jsonify({"error": "No JSON data provided"}), 400
        
    if "text" not in data:
        return jsonify({"error": "Missing 'text' parameter"}), 400
        
    text_input = data["text"].strip()
    
    # Check for empty text
    if not text_input:
        return jsonify({"error": "Text parameter cannot be empty"}), 400

    try:
        # Call Gradio model
        result = client.predict(
            text=text_input,
            api_name="/predict"
        )

        # Read the audio file directly
        with open(result, 'rb') as audio_file:
            audio_data = audio_file.read()

        # Return audio directly in response
        return Response(
            audio_data,
            mimetype='audio/wav',
            headers={
                'Content-Disposition': 'attachment; filename="generated_audio.wav"'
            }
        )
        
    except Exception as e:
        return jsonify({"error": f"Audio generation failed: {str(e)}"}), 500

# ---------------------------
# Run the app
# ---------------------------
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=False)
