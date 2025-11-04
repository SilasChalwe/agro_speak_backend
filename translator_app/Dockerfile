# Use lightweight Python base image
FROM python:3.10-slim

# Set work directory
WORKDIR /app

# Copy requirement file and install deps
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy all app files
COPY . .

# Expose Flask port
EXPOSE 5000

# Run your Flask app
CMD ["python", "app.py"]
