import sys
import os
import numpy as np
import sounddevice as sd
import soundfile as sf
import webrtcvad
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
TEXT_OUTPUT = os.getenv("MORTIS_SPEECH_TEXT_PATH", "/home/gabz/output.txt")

if os.path.exists(OUTPUT):
    os.remove(OUTPUT)

SAMPLE_RATE = 16000
FRAME_DURATION_MS = 30           
FRAME_SIZE = int(SAMPLE_RATE * FRAME_DURATION_MS / 1000)
SILENCE_TIMEOUT_MS = 1200        
SILENCE_FRAMES = SILENCE_TIMEOUT_MS // FRAME_DURATION_MS
MAX_RECORD_SECONDS = 15         
VAD_AGGRESSIVENESS = 0          

vad = webrtcvad.Vad(VAD_AGGRESSIVENESS)

def record_until_silence():
    frames = []
    silence_count = 0
    triggered = False  # only start counting silence after real speech begins
    max_frames = int(MAX_RECORD_SECONDS * 1000 / FRAME_DURATION_MS)

    with sd.RawInputStream(samplerate=SAMPLE_RATE, blocksize=FRAME_SIZE,
                            dtype="int16", channels=1) as stream:
        for _ in range(max_frames):
            frame, _ = stream.read(FRAME_SIZE)
            frame_bytes = bytes(frame)
            frames.append(frame_bytes)

            is_speech = vad.is_speech(frame_bytes, SAMPLE_RATE)

            if is_speech:
                triggered = True
                silence_count = 0
            elif triggered:
                silence_count += 1

            if triggered and silence_count >= SILENCE_FRAMES:
                break

    return b"".join(frames), triggered

raw_audio, got_speech = record_until_silence()
audio = np.frombuffer(raw_audio, dtype=np.int16)

if not got_speech or np.abs(audio).mean() < 500:
    with open(TEXT_OUTPUT, "w") as f:
        f.write("no input")
    sys.exit(0)

sf.write(OUTPUT, audio, SAMPLE_RATE)

client = Groq(api_key=os.getenv("GROQ_API_KEY", ""))

with open(OUTPUT, "rb") as f:
    transcription = client.audio.transcriptions.create(
        model="whisper-large-v3",
        file=f,
        language="en"
    )

text = transcription.text.strip()

if not text or len(text.split()) < 2:
    with open(TEXT_OUTPUT, "w") as f:
        f.write("")
    sys.exit(0)

with open(TEXT_OUTPUT, "w") as f:
    f.write(text)

print(text, flush=True)