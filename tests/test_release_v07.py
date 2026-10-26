from pathlib import Path
import json
import hashlib
import xml.etree.ElementTree as ET

R=Path(__file__).resolve().parents[1]
def test_quotes_100_bilingual_parity():
    one=json.loads((R/'web/data/quotes.json').read_text(encoding='utf-8'))
    two=json.loads((R/'android/app/src/main/assets/quotes.json').read_text(encoding='utf-8'))
    assert one==two and len(one)==100
    assert len({q['en'] for q in one})==100
    assert len({q['mm'] for q in one})==100
    assert all(q['en'] and q['mm'] for q in one)

def test_developer_photo_exact_uploaded():
    # The supplied portrait SHA-256; never retouched or AI-generated.
    expected='5e0e66918c1f5eb6dced30b5e7b6f2cd4078382e59bd45414b56f213e26481e7'
    for file in ['web/public/developer-avatar.jpg','android/app/src/main/res/drawable-nodpi/developer_avatar.jpg']:
        assert hashlib.sha256((R/file).read_bytes()).hexdigest()==expected

def test_font_copies_identical_original():
    expected='df7106c15da76f6a24c10821b43da51f54961f6bc6791fb1fcf21c6c60bb2e10'
    for file in ['web/public/fonts/Pyidaungsu-Regular.ttf','android/app/src/main/res/font/pyidaungsu_regular.ttf']:
        assert hashlib.sha256((R/file).read_bytes()).hexdigest()==expected

def test_developer_restricted_to_about():
    web=(R/'web/app/page.tsx').read_text()
    android=(R/'android/app/src/main/java/org/minsithu/stillmind/MainActivity.kt').read_text()
    assert 'displayedView==="about"&&' in web
    assert 'if(screen=="About")' in android
    assert 'Meet the Developer' in web and 'Meet the Developer' in android
    assert 'showName:false,showStreak:false,showStars:false' in web
    assert 'github.com' not in web.lower()
    assert 'developer-avatar.jpg' not in web.split('const themes=')[0] # User default profile is neutral.

def test_png_and_quote_options_exist():
    web=(R/'web/app/page.tsx').read_text()
    renderer=(R/'android/app/src/main/java/org/minsithu/stillmind/MindShareRenderer.kt').read_text()
    for flag in ['story','sunset','ocean','cream']:
        assert flag in web and flag in renderer
    assert 'square' in web and 'forest' in web and 'else->intArrayOf(0xFF153E34' in renderer
    assert 'if(format=="story")1920 else 1080' in renderer
    assert "canvas.height=format==='story'?1920:1080" in web
    assert '1920 else 1080' in renderer
    assert 'quoteForMood' in web and 'QuoteRepository.forMood' in renderer

def test_android_vector_resources_are_valid():
    for item in ['android/app/src/main/res/drawable/ic_launcher_foreground.xml','android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml','android/app/src/main/res/values/colors.xml']:
        assert ET.parse(R/item).getroot() is not None

def test_web_same_origin_proxy_configuration():
    content=(R/'web/next.config.mjs').read_text()
    page=(R/'web/app/page.tsx').read_text()
    assert 'MIND_API_SERVER_URL' in content
    assert "source: '/api/:path*'" in content
    assert 'const API=""' in page

def test_branding_and_release_guide():
    assert 'စိတ် - Mind' in (R/'web/app/page.tsx').read_text()
    assert 'minsithu.org' in (R/'README.md').read_text()
    assert 'do not' in (R/'docs/DEPLOYMENT.md').read_text().lower()
