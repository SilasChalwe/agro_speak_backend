from flask import Flask, request, jsonify
from gradio_client import Client

app = Flask(__name__)
client = Client("victorsconcious/Bemba_English_Translator")

# Map source/target to Gradio direction
LANG_DIRECTION_MAP = {
    ("English", "Bemba"): "English → Bemba",
    ("Bemba", "English"): "Bemba → English"
}

@app.route("/translate", methods=["POST"])
def translate():
    """
    Expects JSON payload:
    {
        "text": "Hello!",
        "sourceLang": "English",
        "targetLang": "Bemba"
    }
    """
    data = request.json
    text = data.get("text")
    source = data.get("sourceLang")
    target = data.get("targetLang")

    # Validate input
    if not text or not source or not target:
        return jsonify({"error": "Fields 'text', 'sourceLang', and 'targetLang' are required"}), 400

    direction = LANG_DIRECTION_MAP.get((source, target))
    if not direction:
        return jsonify({"error": f"Translation from {source} to {target} not supported"}), 400

    try:
        translated = client.predict(direction=direction, text=text, api_name="/translate")
        return jsonify({
            "originalText": text,
            "sourceLang": source,
            "targetLang": target,
            "translation": translated
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
