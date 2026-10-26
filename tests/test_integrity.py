"""Offline source/artifact checks; these do not replace Android or browser QA."""
from pathlib import Path
import hashlib
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]

def test_fonts_identical():
    web = (ROOT / 'web/public/fonts/Pyidaungsu-Regular.ttf').read_bytes()
    android = (ROOT / 'android/app/src/main/res/font/pyidaungsu_regular.ttf').read_bytes()
    assert web == android and len(web) > 100_000

def test_developer_photo_identical_in_both_apps():
    assert (ROOT/'web/public/developer-avatar.jpg').read_bytes() == (ROOT/'android/app/src/main/res/drawable-nodpi/developer_avatar.jpg').read_bytes()

def test_manifest_valid():
    tree=ET.parse(ROOT/'android/app/src/main/AndroidManifest.xml')
    assert tree.getroot().tag=='manifest'
    assert 'android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK' in ET.tostring(tree.getroot(), encoding='unicode')

def test_source_control_keeps_audio():
    assert '*.mp3' not in (ROOT/'.gitignore').read_text().splitlines()
    assert len(list((ROOT/'audio').glob('*.mp3'))) == 12

def test_all_tracks_long_and_distinct():
    tracks=sorted((ROOT/'audio').glob('*.mp3'))
    hashes={hashlib.sha256(t.read_bytes()).hexdigest() for t in tracks}
    assert len(hashes)==len(tracks)==12
    for track in tracks:
        result = subprocess.run(['ffprobe','-v','error','-show_entries','format=duration','-of','csv=p=0',str(track)],capture_output=True,text=True,check=True)
        assert float(result.stdout.strip())>600, track.name

def test_security_and_local_profile_flags():
    manifest=(ROOT/'android/app/src/main/AndroidManifest.xml').read_text()
    assert 'usesCleartextTraffic="true"' not in manifest
    assert 'usesCleartextTraffic="true"' in (ROOT/'android/app/src/debug/AndroidManifest.xml').read_text()
    web=(ROOT/'web/app/page.tsx').read_text()
    assert 'profile.avatar||"/developer-avatar.jpg"' not in web
    assert 'showName:false,showStreak:false,showStars:false' in web
