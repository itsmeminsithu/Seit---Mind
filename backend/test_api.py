from main import app
from fastapi.testclient import TestClient
c = TestClient(app)
def test_health(): assert c.get("/health").json()["status"] == "ok"
def test_sessions(): assert len(c.get("/api/sessions").json()) == 12
def test_filter(): assert len(c.get("/api/sessions?category=Sleep").json()) == 3
def test_unknown_audio(): assert c.get("/api/audio/secret.mp3").status_code == 404

def test_every_track_available():
    for session in c.get("/api/sessions").json():
        response = c.get(session["audio_url"])
        assert response.status_code == 200, session["title"]
        assert response.headers["content-type"].startswith("audio/mpeg")


def test_long_sessions():
    from main import SESSIONS
    assert len(SESSIONS) >= 12
    assert all(s["duration_minutes"] >= 10 for s in SESSIONS)


def test_session_not_found():
    assert c.get("/api/sessions/999").status_code == 404

def test_audio_byte_range():
    res = c.get("/api/audio/forest.mp3", headers={"Range": "bytes=0-127"})
    assert res.status_code == 206
    assert len(res.content) == 128
    assert res.headers.get("content-range", "").startswith("bytes 0-127/")

def test_no_path_traversal():
    assert c.get("/api/audio/%2e%2e%2Fmain.py").status_code in (404, 422)

def test_no_wildcard_cors():
    res = c.get("/api/sessions", headers={"Origin": "https://untrusted.example"})
    assert res.headers.get("access-control-allow-origin") is None
