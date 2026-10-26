from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from pathlib import Path
import os

app = FastAPI(title="စိတ် - Mind API", version="0.8.0")
# For deployed Web clients configure MIND_CORS_ORIGINS to explicit HTTPS origin(s).
CORS_ORIGINS = [origin.strip() for origin in os.getenv("MIND_CORS_ORIGINS", "http://localhost:3000,http://127.0.0.1:3000").split(",") if origin.strip()]
app.add_middleware(CORSMiddleware, allow_origins=CORS_ORIGINS, allow_methods=["GET"], allow_headers=["*"])
AUDIO_DIR = Path(__file__).resolve().parent.parent / "audio"
SESSIONS = [{'id': 1, 'title': 'Forest Ambience', 'category': 'Nature', 'duration_minutes': 12, 'description': 'Soft woodland breeze and distant birdlike tones.', 'audio_url': '/api/audio/forest.mp3', 'color': '#DCE8D5'}, {'id': 2, 'title': 'Deep Calm', 'category': 'Mindfulness', 'duration_minutes': 12, 'description': 'Gentle warm sustained ambient tones.', 'audio_url': '/api/audio/calm.mp3', 'color': '#E4E5D8'}, {'id': 3, 'title': 'Water Drops', 'category': 'Nature', 'duration_minutes': 12, 'description': 'Soft water droplets and delicate ripples.', 'audio_url': '/api/audio/drops.mp3', 'color': '#D5E9EC'}, {'id': 4, 'title': 'Soft Piano', 'category': 'Focus', 'duration_minutes': 12, 'description': 'Original minimal piano-inspired melody.', 'audio_url': '/api/audio/piano.mp3', 'color': '#E5E0EC'}, {'id': 5, 'title': 'Air Waves', 'category': 'Focus', 'duration_minutes': 12, 'description': 'Soft flowing air and breath-like textures.', 'audio_url': '/api/audio/air.mp3', 'color': '#D8E8E4'}, {'id': 6, 'title': 'Ocean Tides', 'category': 'Sleep', 'duration_minutes': 12, 'description': 'Rolling wave-like ambience.', 'audio_url': '/api/audio/tides.mp3', 'color': '#D6E4EE'}, {'id': 7, 'title': 'Meditation Bells', 'category': 'Mindfulness', 'duration_minutes': 12, 'description': 'Soft synthesized meditation bells, no chanting.', 'audio_url': '/api/audio/bell.mp3', 'color': '#ECE4D7'}, {'id': 8, 'title': 'Gentle Rain', 'category': 'Sleep', 'duration_minutes': 12, 'description': 'Layered water and airy ambience.', 'audio_url': '/api/audio/rain.mp3', 'color': '#DCE8D5'}, {'id': 9, 'title': 'Morning Dawn', 'category': 'Mindfulness', 'duration_minutes': 12, 'description': 'Woodland and warm calm tones.', 'audio_url': '/api/audio/dawn.mp3', 'color': '#E4E5D8'}, {'id': 10, 'title': 'Night Drift', 'category': 'Sleep', 'duration_minutes': 12, 'description': 'Slow waves and deep gentle tones.', 'audio_url': '/api/audio/night.mp3', 'color': '#D5E9EC'}, {'id': 11, 'title': 'Quiet Stream', 'category': 'Nature', 'duration_minutes': 12, 'description': 'Soft rippling water and air.', 'audio_url': '/api/audio/stream.mp3', 'color': '#E5E0EC'}, {'id': 12, 'title': 'Zen Garden', 'category': 'Mindfulness', 'duration_minutes': 12, 'description': 'Minimal soft bells and air.', 'audio_url': '/api/audio/zen.mp3', 'color': '#D8E8E4'}]

@app.get("/health")
def health():
    return {"status": "ok", "version": "0.8.0"}

@app.get("/api/sessions")
def sessions(category: str | None = None):
    return [s for s in SESSIONS if not category or s["category"].lower() == category.lower()]

@app.get("/api/sessions/{session_id}")
def session(session_id: int):
    result = next((s for s in SESSIONS if s["id"] == session_id), None)
    if result is None:
        raise HTTPException(status_code=404, detail="Session not found")
    return result

@app.get("/api/audio/{filename}")
def audio(filename: str):
    if filename not in {s["audio_url"].rsplit("/", 1)[-1] for s in SESSIONS}:
        raise HTTPException(404, "Unknown audio")
    path = AUDIO_DIR / filename
    if not path.is_file():
        raise HTTPException(404, "Add licensed MP3 file to audio/ directory")
    return FileResponse(path, media_type="audio/mpeg", filename=filename, content_disposition_type="inline", headers={"Accept-Ranges": "bytes", "X-Content-Type-Options": "nosniff"})
