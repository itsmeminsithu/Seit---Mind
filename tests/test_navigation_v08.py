from pathlib import Path
R=Path(__file__).resolve().parents[1]
def test_android_navigation_isolated():
    s=(R/'android/app/src/main/java/org/minsithu/stillmind/MainActivity.kt').read_text()
    for screen in ('Home','Explore','Progress','Profile','Settings','About','Player','Complete'):
        assert f'if(screen=="{screen}")' in s
    assert 'NavigationBarItem(' in s and 'Scaffold(' in s
    home=s[s.index('if(screen=="Home")'):s.index('if(screen=="Explore")')]
    assert 'OutlinedTextField(' not in home
    settings=s[s.index('if(screen=="Settings")'):s.index('if(screen=="Complete")')]
    assert 'Display name' in settings and 'Daily reminder' in settings and 'Share card privacy' in settings

def test_port_mapping_3100_no_conflict():
    s=(R/'docker-compose.yml').read_text()
    assert '"3100:3000"' in s

def test_web_no_mixed_content():
    s=(R/'web/app/page.tsx').read_text()
    home=s[s.index('displayedView==="home"&&'):s.index('displayedView==="explore"&&')]
    assert 'Daily reminder' not in home and 'type="file"' not in home
    assert 'displayedView==="settings"&&' in s
    assert 'displayedView==="player"&&' in s
    assert 'displayedView==="complete"&&' in s
