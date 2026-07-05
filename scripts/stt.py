import sys
import os
import numpy as np
import sounddevice as sd
import soundfile as sf
from groq import Groq
import signal

def handle_exit(sig, frame):
    sd.stop()
    sys.exit(0)

signal.signal(signal.SIGINT, handle_exit)
signal.signal(signal.SIGTERM, handle_exit)
from env_utils import load_env

load_env()

OUTPUT = os.getenv("MORTIS_SPEECH_AUDIO_PATH", "/home/gabz/output.wav")
samplerate = 16000

if os.path.exists(OUTPUT):
    os.remove(OUTPUT)

duration = 10

audio = sd.rec(
    int(duration * samplerate),
    samplerate=samplerate,
    channels=1,
    dtype="int16"
)

sd.wait()
sd.stop()
if np.abs(audio).mean() < 500:
    with open(os.getenv("MORTIS_SPEECH_TEXT_PATH", "/home/gabz/output.txt"), "w") as f:
        f.write("no input")
    sys.exit(0)

sf.write(OUTPUT, audio, samplerate)

client = Groq(api_key=os.getenv("GROQ_API_KEY", ""))

with open(OUTPUT, "rb") as f:
    transcription = client.audio.transcriptions.create(
        model="whisper-large-v3",
        file=f,
        language="en"
    )

text = transcription.text.strip()

if not text or len(text.split()) < 2:
    with open(os.getenv("MORTIS_SPEECH_TEXT_PATH", "/home/gabz/output.txt"), "w") as f:
        f.write("")
    sys.exit(0)

with open(os.getenv("MORTIS_SPEECH_TEXT_PATH", "/home/gabz/output.txt"), "w") as f:
    f.write(text)

print(text, flush=True)