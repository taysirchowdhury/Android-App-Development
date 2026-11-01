"""Static audit only: validates resources, layout IDs, media, and intended source hooks."""
from pathlib import Path
import xml.etree.ElementTree as ET
import re, json, subprocess
P=Path(__file__).resolve().parents[1]
RES=P/'app/src/main/res'; K=P/'app/src/main/java/com/example/cse489assignment2'
results=[]
def check(name,ok):
 results.append({'check':name,'pass':bool(ok)})
 if not ok:print('FAIL:',name)
xml=list(RES.rglob('*.xml'))+[P/'app/src/main/AndroidManifest.xml']
try:
 for f in xml:ET.parse(f)
 check('All XML files are well formed',True)
except Exception as e:check('All XML files are well formed',False);print(e)
texts='\n'.join(p.read_text() for p in K.rglob('*.kt'))+'\n'+'\n'.join(p.read_text() for p in xml)
for typ in ['string','color']:
 defs={el.get('name') for el in ET.parse(RES/f'values/{"strings" if typ=="string" else "colors"}.xml').getroot()}
 refs=set(re.findall(r'@'+typ+r'/([a-zA-Z0-9_]+)',texts))
 refs.update(re.findall(r'R\.'+typ+r'\.([a-zA-Z0-9_]+)',texts))
 check(f'All {typ} references resolve',not(refs-defs))
for name in ['video','audio']:
 source=(K/f'ui/{name.title()}Fragment.kt').read_text()
 ids=set(re.findall(r'R\.id\.([a-zA-Z0-9_]+)',source))
 for f in [RES/f'layout/fragment_{name}.xml'] + ([RES/'layout-land/fragment_audio.xml'] if name=='audio' else []):
  text=f.read_text()
  defs=set(re.findall(r'@\+id/([a-zA-Z0-9_]+)',text))
  check(f'{name} IDs match layout {f.parent.name}',not(ids-defs))
main=(K/'MainActivity.kt').read_text()
check('Main has edge-to-edge inset handling with toolbar-safe area',
      'setOnApplyWindowInsetsListener(mainContent)' in main and 'toolbarBaseHeight + safe.top' in main)
check('Main avoids whole content side inset padding', 'mainContent.setPadding(' not in main)
check('Main still connects drawer and Jetpack nav',main.count('NavigationUI.setupWithNavController')==2)
video=(K/'ui/VideoFragment.kt').read_text()
for n,s in [('Video uses TextureView + MediaPlayer', 'mp.setSurface(surface)'),
            ('Video has seek-to-position listener','onStopTrackingTouch'),
            ('Video tracks completion and resets position','mp.setOnCompletionListener'),
            ('Video saves state on rotation','onSaveInstanceState'),
            ('Video landscape uses filled surface','if (landscape) max(')]:
 check(n,s in video)
audio=(K/'ui/AudioFragment.kt').read_text()
for n,s in [('Audio uses MediaPlayer','MediaPlayer.create'),
            ('Audio has one toggle control','R.id.audio_toggle'),
            ('Audio resets playback position','completed.seekTo(0)'),
            ('Audio releases media player','media?.release()'),
            ('Audio persists rotation state','onSaveInstanceState')]:check(n,s in audio)
check('Video contains single play/pause and seek control',all(x in (RES/'layout/fragment_video.xml').read_text() for x in ['video_toggle','video_seek','video_elapsed','video_duration']))
check('Audio has portrait+landscape UI', (RES/'layout-land/fragment_audio.xml').exists())
check('Image remains online and zoomable','HttpURLConnection' in (K/'ui/ImageFragment.kt').read_text() and 'ScaleGestureDetector' in (K/'ui/ZoomImageView.kt').read_text())
check('Custom broadcast remains real', 'sendBroadcast(' in (K/'CustomReceiverActivity.kt').read_text() and 'override fun onReceive' in (K/'CustomReceiverActivity.kt').read_text())
check('Battery listens to system broadcasts','Intent.ACTION_BATTERY_CHANGED' in (K/'BatteryActivity.kt').read_text())
for filename,codec in [('sample_audio.mp3','mp3'),('sample_video.mp4','h264')]:
 file=RES/'raw'/filename
 try:
  data=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_entries','stream=codec_name','-show_entries','format=duration','-of','json',str(file)],text=True))
  good=any(s['codec_name']==codec for s in data['streams']) and float(data['format']['duration'])>10
 except Exception:good=False
 check(f'{filename} decodes and is longer than 10 seconds',good)
report={'passed':sum(x['pass'] for x in results),'total':len(results),'checks':results,
'scope':'Source XML and media codec checks ONLY; Android SDK build and emulator runtime not available here.'}
(P/'docs/STATIC_AUDIT_REPORT.json').write_text(json.dumps(report,indent=2)+'\n')
print(f"STATIC AUDIT {report['passed']}/{report['total']}")
if report['passed']!=report['total']:raise SystemExit(1)
